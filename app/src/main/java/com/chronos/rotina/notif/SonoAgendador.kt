package com.chronos.rotina.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.calcularHoraDormir
import com.chronos.rotina.data.configSono
import com.chronos.rotina.data.passosDeDormirAtivos
import java.util.Calendar

object SonoAgendador {

    private const val ID_SONO = 700001
    private const val DIAS_A_FRENTE = 14

    private fun d2(n: Int) = if (n < 10) "0$n" else "$n"
    private fun dataStr(a: Int, m: Int, d: Int) = "$a-${d2(m)}-${d2(d)}"

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private suspend fun rotinaCobreNoite(context: Context, noite: Calendar): Boolean {
        val db = context.appDb()
        val passos = db.passosDeDormirAtivos()
        if (passos.isEmpty()) return false

        for (p in passos) {
            val alvo = Calendar.getInstance()
            alvo.time = noite.time
            alvo.add(Calendar.DAY_OF_YEAR, p.dayOffset)
            val chave = dataStr(
                alvo.get(Calendar.YEAR),
                alvo.get(Calendar.MONTH) + 1,
                alvo.get(Calendar.DAY_OF_MONTH)
            )
            if (db.escalaDao().ehDiaDeTrabalho(chave) > 0) return true
        }
        return false
    }

    private fun cancelar(context: Context) {
        val intent = Intent(context, SonoReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, ID_SONO, intent, flags)
        alarmManager(context).cancel(pi)
    }

    suspend fun reagendar(context: Context) {
        cancelar(context)

        val db = context.appDb()
        val config = db.configSono()
        if (!config.ativo) return

        val horaDormir = calcularHoraDormir(config.horaAcordar, config.horasSono)
        val partes = horaDormir.split(":")
        val hd = partes.getOrNull(0)?.toIntOrNull() ?: return
        val md = partes.getOrNull(1)?.toIntOrNull() ?: return

        val agora = Calendar.getInstance()

        for (i in 0 until DIAS_A_FRENTE) {
            val noite = Calendar.getInstance()
            noite.add(Calendar.DAY_OF_YEAR, i)

            val disparo = Calendar.getInstance()
            disparo.time = noite.time
            disparo.set(Calendar.HOUR_OF_DAY, hd)
            disparo.set(Calendar.MINUTE, md)
            disparo.set(Calendar.SECOND, 0)
            disparo.set(Calendar.MILLISECOND, 0)
            disparo.add(Calendar.MINUTE, -config.minutosAntes)

            if (disparo.timeInMillis <= agora.timeInMillis) continue
            if (rotinaCobreNoite(context, noite)) continue

            agendarUm(context, disparo, horaDormir, config.horasSono)
            return
        }
    }

    private fun agendarUm(context: Context, quando: Calendar, horaDormir: String, horas: Int) {
        Canais.criar(context)
        val intent = Intent(context, SonoReceiver::class.java).apply {
            putExtra("horaDormir", horaDormir)
            putExtra("horas", horas)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, ID_SONO, intent, flags)
        val am = alarmManager(context)
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
