package com.chronos.rotina.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager

object Canais {
    const val NOTIFICACAO = "machitto_notificacao"
    const val ALARME = "machitto_alarme"

    fun criar(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (nm.getNotificationChannel(NOTIFICACAO) == null) {
            val canal = NotificationChannel(
                NOTIFICACAO,
                "Lembretes da rotina",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos do Machitto"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200)
            }
            nm.createNotificationChannel(canal)
        }

        if (nm.getNotificationChannel(ALARME) == null) {
            val som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val canal = NotificationChannel(
                ALARME,
                "Alarmes (acordar)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarmes que furam o silêncio"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(som, attrs)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(canal)
        }
    }
}
