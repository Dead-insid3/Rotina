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

class LembreteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Conta chegando 🐾"
        val corpo = intent.getStringExtra("corpo") ?: ""
        val notifId = intent.getIntExtra("notifId", 500000)

        Canais.criar(context)

        val builder = NotificationCompat.Builder(context, Canais.NOTIFICACAO)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(titulo)
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
    }
}
