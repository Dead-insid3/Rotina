package com.chronos.rotina.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.rotina.data.TarefaEntity
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.horariosDeAviso
import com.chronos.rotina.data.tarefasTodas

object CronogramaAgendador {

    private const val BASE_ID = 900000
    private const val MAX_AVISOS_POR_TAREFA = 20

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun idDoAviso(tarefaId: Long, indice: Int): Int =
        BASE_ID + (tarefaId.toInt() % 4000) * MAX_AVISOS_POR_TAREFA + indice

    private fun pendingIntent(
        context: Context,
        id: Int,
        titulo: String,
        corpo: String,
        tarefaId: Long
    ): PendingIntent {
        val intent = Intent(context, CronogramaReceiver::class.java).apply {
            putExtra("notifId", id)
            putExtra("titulo", titulo)
            putExtra("corpo", corpo)
            putExtra("tarefaId", tarefaId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id, intent, flags)
    }

    private fun cancelarAviso(context: Context, id: Int) {
        val intent = Intent(context, CronogramaReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, id, intent, flags)
        alarmManager(context).cancel(pi)
    }

    fun cancelarTarefa(context: Context, tarefaId: Long) {
        for (i in 0 until MAX_AVISOS_POR_TAREFA) {
            cancelarAviso(context, idDoAviso(tarefaId, i))
        }
    }

    private fun textoRestante(prazoMillis: Long, agora: Long): String {
        val faltamMin = ((prazoMillis - agora) / 60_000L).toInt()
        return when {
            faltamMin <= 1 -> "É agora!"
            faltamMin < 60 -> "Faltam $faltamMin minutos."
            faltamMin < 120 -> "Falta pouco mais de 1 hora."
            else -> "Faltam ${faltamMin / 60} horas."
        }
    }

    suspend fun reagendarTodas(context: Context) {
        Canais.criar(context)
        val db = context.appDb()
        val tarefas = db.tarefasTodas()
        val agora = System.currentTimeMillis()

        for (t in tarefas) {
            cancelarTarefa(context, t.id)
            if (t.concluida) continue
            if (t.prazoMillis <= agora) continue
            agendarTarefa(context, t, agora)
        }
    }

    private fun agendarTarefa(context: Context, tarefa: TarefaEntity, agora: Long) {
        val horarios = horariosDeAviso(tarefa.prazoMillis, agora, tarefa.insistencia)
            .take(MAX_AVISOS_POR_TAREFA)
        val am = alarmManager(context)

        horarios.forEachIndexed { indice, quando ->
            val corpo = buildString {
                append(textoRestante(tarefa.prazoMillis, quando))
                if (tarefa.detalhe.isNotBlank()) {
                    append(" ")
                    append(tarefa.detalhe)
                }
            }
            val pi = pendingIntent(
                context,
                idDoAviso(tarefa.id, indice),
                tarefa.titulo,
                corpo,
                tarefa.id
            )
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
}
