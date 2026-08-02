package com.chronos.rotina.notif

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chronos.rotina.R

object NotificacaoAlarmeBuilder {

    fun construir(context: Context, titulo: String, corpo: String): Notification {
        val telaIntent = Intent(context, AlarmeActivity::class.java).apply {
            putExtra("titulo", titulo)
            putExtra("corpo", corpo)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPi = PendingIntent.getActivity(context, 7001, telaIntent, flags)

        val pararIntent = Intent(context, AlarmeService::class.java).apply {
            action = AlarmeService.ACAO_PARAR
        }
        val pararPi = PendingIntent.getService(context, 7002, pararIntent, flags)

        return NotificationCompat.Builder(context, Canais.ALARME)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPi, true)
            .setContentIntent(fullScreenPi)
            .addAction(R.mipmap.ic_launcher, "Desligar", pararPi)
            .build()
    }
}
