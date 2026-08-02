package com.chronos.rotina.notif

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chronos.rotina.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val passoId = intent.getLongExtra("passoId", -1L)
        val titulo = intent.getStringExtra("titulo") ?: "Machitto"
        val corpo = intent.getStringExtra("corpo") ?: ""
        val ehAlarme = intent.getBooleanExtra("alarme", false)

        Canais.criar(context)

        if (ehAlarme) {
            AlarmeService.iniciar(context, titulo, corpo)
            val telaIntent = Intent(context, AlarmeActivity::class.java).apply {
                putExtra("titulo", titulo)
                putExtra("corpo", corpo)
                putExtra("passoId", passoId)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
            }
            try {
                context.startActivity(telaIntent)
            } catch (_: Exception) {
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Agendador.reagendarPasso(context, passoId)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val canal = Canais.NOTIFICACAO
        val prioridade = NotificationCompat.PRIORITY_HIGH

        val builder = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(corpo))
            .setPriority(prioridade)
            .setAutoCancel(true)

        val permitido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (permitido) {
            NotificationManagerCompat.from(context).notify(passoId.toInt().coerceAtLeast(1), builder.build())
        }

        // Reagenda o próximo disparo deste passo
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Agendador.reagendarPasso(context, passoId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
