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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SonoReceiver : BroadcastReceiver() {

    private val falas = listOf(
        "Ó, tá na hora de deitar. Amanhã você me agradece.",
        "Psiu. Cama. Agora. Não tô pedindo.",
        "Gato dorme 16h por dia. Você não faz nem metade. Vai dormir.",
        "Já pensou em dormir? É tipo cochilar, mas de verdade.",
        "A cama tá ali, quentinha, esperando. Vai.",
        "Se eu durmo, você também pode. Boa noite. 🐾"
    )

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val horaDormir = intent.getStringExtra("horaDormir") ?: ""
        val horas = intent.getIntExtra("horas", 8)

        Canais.criar(context)

        val corpo = buildString {
            append(falas.random())
            if (horaDormir.isNotBlank()) {
                append("\nDeitando às $horaDormir você dorme suas ${horas}h.")
            }
        }

        val builder = NotificationCompat.Builder(context, Canais.NOTIFICACAO)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Hora de dormir 😴")
            .setContentText(corpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(corpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val permitido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (permitido) {
            NotificationManagerCompat.from(context).notify(700001, builder.build())
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                SonoAgendador.reagendar(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
