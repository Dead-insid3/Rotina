package com.chronos.rotina.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object Soneca {
    private const val ID_SONECA = 888888

    fun agendar(context: Context, titulo: String, corpo: String, passoId: Long, minutos: Int) {
        val intent = Intent(context, AlarmeReceiver::class.java).apply {
            putExtra("passoId", passoId)
            putExtra("titulo", titulo)
            putExtra("corpo", "(Soneca) $corpo")
            putExtra("alarme", true)
            putExtra("soneca", true)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, ID_SONECA, intent, flags)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val quando = System.currentTimeMillis() + minutos * 60_000L
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
