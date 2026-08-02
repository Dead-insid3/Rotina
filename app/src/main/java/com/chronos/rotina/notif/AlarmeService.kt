package com.chronos.rotina.notif

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmeService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_PARAR) {
            parar()
            return START_NOT_STICKY
        }

        val titulo = intent?.getStringExtra("titulo") ?: "Hora de acordar!"
        val corpo = intent?.getStringExtra("corpo") ?: ""

        Canais.criar(this)
        val notif = NotificacaoAlarmeBuilder.construir(this, titulo, corpo)
        startForeground(ID_NOTIF, notif)

        tocar()
        vibrar()
        return START_STICKY
    }

    private fun tocar() {
        try {
            var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            player = MediaPlayer().apply {
                setDataSource(this@AlarmeService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
        }
    }

    private fun vibrar() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val padrao = longArrayOf(0, 600, 400, 600, 400)
        vibrator?.vibrate(VibrationEffect.createWaveform(padrao, 0))
    }

    private fun parar() {
        try { player?.stop(); player?.release() } catch (_: Exception) {}
        player = null
        vibrator?.cancel()
        vibrator = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        parar()
        super.onDestroy()
    }

    companion object {
        const val ACAO_PARAR = "com.chronos.rotina.PARAR_ALARME"
        const val ID_NOTIF = 424242

        fun iniciar(context: Context, titulo: String, corpo: String) {
            val intent = Intent(context, AlarmeService::class.java).apply {
                putExtra("titulo", titulo)
                putExtra("corpo", corpo)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun parar(context: Context) {
            val intent = Intent(context, AlarmeService::class.java).apply { action = ACAO_PARAR }
            context.startService(intent)
        }
    }
}
