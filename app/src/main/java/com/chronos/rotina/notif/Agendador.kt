package com.chronos.rotina.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.rotina.data.PassoEntity
import com.chronos.rotina.data.appDb
import java.util.Calendar

object Agendador {

    private fun d2(n: Int) = if (n < 10) "0$n" else "$n"
    private fun dataStr(a: Int, m: Int, d: Int) = "$a-${d2(m)}-${d2(d)}"

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun intentDoPasso(context: Context, passo: PassoEntity, titulo: String, corpo: String): PendingIntent {
        val intent = Intent(context, AlarmeReceiver::class.java).apply {
            putExtra("passoId", passo.id)
            putExtra("titulo", titulo)
            putExtra("corpo", corpo)
            putExtra("alarme", passo.alarme)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, passo.id.toInt(), intent, flags)
    }

    private suspend fun proximoDisparo(context: Context, passo: PassoEntity): Calendar? {
        val partes = passo.fireTime.split(":")
        if (partes.size != 2) return null
        val fh = partes[0].toIntOrNull() ?: return null
        val fm = partes[1].toIntOrNull() ?: return null
        val db = context.appDb()
        val agora = Calendar.getInstance()

        for (i in 0 until 60) {
            val d = Calendar.getInstance()
            d.add(Calendar.DAY_OF_YEAR, i)
            val dstr = dataStr(d.get(Calendar.YEAR), d.get(Calendar.MONTH) + 1, d.get(Calendar.DAY_OF_MONTH))
            val ehTrabalho = db.escalaDao().ehDiaDeTrabalho(dstr) > 0
            if (!ehTrabalho) continue

            val disparo = Calendar.getInstance()
            disparo.time = d.time
            disparo.add(Calendar.DAY_OF_YEAR, -passo.dayOffset)
            disparo.set(Calendar.HOUR_OF_DAY, fh)
            disparo.set(Calendar.MINUTE, fm)
            disparo.set(Calendar.SECOND, 0)
            disparo.set(Calendar.MILLISECOND, 0)

            if (disparo.timeInMillis > agora.timeInMillis) return disparo
        }
        return null
    }

    private suspend fun montarTexto(context: Context, passo: PassoEntity): Pair<String, String> {
        val db = context.appDb()
        val perfil = db.perfilDao().obter()
        val nome = perfil?.nome?.ifBlank { "humano" } ?: "humano"
        val genero = perfil?.genero ?: "nb"

        val tag = db.tagDao().obter(passo.tag)
        val categoria = tag?.categoria ?: "generico"
        val frases = db.fraseDao().daCategoria(categoria)
        val fraseEnt = frases.randomOrNull()

        val texto = when {
            fraseEnt == null -> "Ó, tá na hora: ${passo.label}"
            genero == "m" -> fraseEnt.textoM
            genero == "f" -> fraseEnt.textoF
            else -> fraseEnt.textoN
        }.replace("{nome}", nome).replace("{detalhe}", passo.label)

        return passo.label to texto
    }

    suspend fun reagendarPasso(context: Context, passoId: Long) {
        val db = context.appDb()
        val passo = db.passoDao().todos().firstOrNull { it.id == passoId } ?: return
        if (!passo.ativo) {
            cancelarPasso(context, passo)
            return
        }
        val quando = proximoDisparo(context, passo) ?: return
        val (titulo, corpo) = montarTexto(context, passo)
        val pi = intentDoPasso(context, passo, titulo, corpo)
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

    fun cancelarPasso(context: Context, passo: PassoEntity) {
        val intent = Intent(context, AlarmeReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, passo.id.toInt(), intent, flags)
        alarmManager(context).cancel(pi)
    }

    suspend fun reagendarTodos(context: Context) {
        Canais.criar(context)
        val db = context.appDb()
        val passos = db.passoDao().todos()
        for (p in passos) {
            if (p.ativo) reagendarPasso(context, p.id) else cancelarPasso(context, p)
        }
        SonoAgendador.reagendar(context)
    }

    fun dispararTeste(context: Context, segundos: Int, comoAlarme: Boolean) {
        Canais.criar(context)
        val intent = Intent(context, AlarmeReceiver::class.java).apply {
            putExtra("passoId", 999999L)
            putExtra("titulo", if (comoAlarme) "Teste de ALARME 🐾" else "Teste de notificação 🐾")
            putExtra("corpo", if (comoAlarme) "Se você ouviu som e vibração forte, o alarme funciona!" else "Se apareceu no topo, a notificação funciona!")
            putExtra("alarme", comoAlarme)
            putExtra("teste", true)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, 999999, intent, flags)
        val am = alarmManager(context)
        val quando = System.currentTimeMillis() + segundos * 1000L
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.set(AlarmManager.RTC_WAKEUP, quando, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pi)
            }
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, quando, pi)
        }
    }
}
