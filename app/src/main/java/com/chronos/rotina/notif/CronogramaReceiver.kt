package com.chronos.rotina.notif

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chronos.rotina.R
import com.chronos.rotina.data.appDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CronogramaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val notifId = intent.getIntExtra("notifId", 900000)
        val titulo = intent.getStringExtra("titulo") ?: "Tarefa"
        val corpo = intent.getStringExtra("corpo") ?: ""
        val tarefaId = intent.getLongExtra("tarefaId", -1L)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tarefa = if (tarefaId > 0) context.appDb().tarefaDao().porId(tarefaId) else null
                if (tarefa == null || tarefa.concluida) return@launch

                Canais.criar(context)

                val builder = NotificationCompat.Builder(context, Canais.NOTIFICACAO)
                    .setSmallIcon(R.drawable.ic_notificacao)
                    .setContentTitle("⏳ $titulo")
                    .setContentText(corpo)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(corpo))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                val permitido = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (permitido) {
                    NotificationManagerCompat.from(context).notify(notifId, builder.build())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
