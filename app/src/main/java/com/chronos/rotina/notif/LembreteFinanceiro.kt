package com.chronos.rotina.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.rotina.data.ContaEntity
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.descricaoDia
import com.chronos.rotina.data.diaEfetivo
import java.util.Calendar

object LembreteFinanceiro {

    private val HORAS = intArrayOf(9, 19)
    private const val DIAS_ANTES = 3
    private const val BASE_ID = 500000

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun idLembrete(conta: ContaEntity, offset: Int, hora: Int): Int {
        return BASE_ID + (conta.id.toInt() * 100) + (offset * 10) + (if (hora < 12) 0 else 1)
    }

    private fun pendingIntent(context: Context, id: Int, titulo: String, corpo: String): PendingIntent {
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra("titulo", titulo)
            putExtra("corpo", corpo)
            putExtra("notifId", id)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id, intent, flags)
    }

    private fun cancelar(context: Context, id: Int) {
        val intent = Intent(context, LembreteReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, id, intent, flags)
        alarmManager(context).cancel(pi)
    }

    suspend fun reagendarTodos(context: Context) {
        val db = context.appDb()
        val contas = db.contaDao().ativas()
        val perfil = db.perfilDao().obter()
        val nome = perfil?.nome?.ifBlank { "humano" } ?: "humano"
        val genero = perfil?.genero ?: "nb"
        val modo = db.preferenciaDao().obter("modo_pagamento") ?: "vencimento"

        val agora = Calendar.getInstance()
        val am = alarmManager(context)

        val anoAtual = agora.get(Calendar.YEAR)
        val mesAtual = agora.get(Calendar.MONTH) + 1

        for (conta in contas) {
            val diaVenc = conta.diaEfetivo(anoAtual, mesAtual)
            for (offset in DIAS_ANTES downTo 1) {
                for (hora in HORAS) {
                    val id = idLembrete(conta, offset, hora)
                    cancelar(context, id)

                    // No modo "exige pago", conta paga não lembra
                    if (modo == "pago" && conta.paga) continue

                    val dia = diaVenc - offset
                    if (dia < 1) continue

                    val quando = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, dia)
                        set(Calendar.HOUR_OF_DAY, hora)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (quando.timeInMillis <= agora.timeInMillis) continue

                    val frase = db.fraseDao().daCategoria("conta_vencendo").randomOrNull()
                    val detalhe = "${conta.label} (${formatarValor(conta.valor)}), vence ${conta.descricaoDia()}"
                    val corpo = when {
                        frase == null -> "Ó, não esquece: $detalhe"
                        genero == "m" -> frase.textoM
                        genero == "f" -> frase.textoF
                        else -> frase.textoN
                    }.replace("{nome}", nome).replace("{detalhe}", detalhe)

                    val pi = pendingIntent(context, id, "Conta chegando 🐾", corpo)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                            am.set(AlarmManager.RTC_WAKEUP, quando.timeInMillis, pi)
                        } else {
                            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando.timeInMillis, pi)
                        }
                    } catch (e: SecurityException) {
                        am.set(AlarmManager.RTC_WAKEUP, quando.timeInMillis, pi)
                    }
                }
            }
        }
    }

    private fun formatarValor(v: Double): String {
        val inteiro = kotlin.math.floor(kotlin.math.abs(v)).toLong()
        val centavos = kotlin.math.round((kotlin.math.abs(v) - inteiro) * 100).toInt()
        val sInt = inteiro.toString().reversed().chunked(3).joinToString(".").reversed()
        return "R$ %s,%02d".format(sInt, centavos)
    }
}
