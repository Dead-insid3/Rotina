package com.chronos.rotina.notif

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.ui.MachittoAlarme

private val Base = Color(0xFF1E1E2E)
private val TextoP = Color(0xFFCDD6F4)
private val TextoS = Color(0xFFA6ADC8)
private val Amber = Color(0xFFF9E2AF)
private val Mantle = Color(0xFF181825)
private val Surface = Color(0xFF313244)
private val Lavanda = Color(0xFFB4BEFE)

class AlarmeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aplicarFlagsTelaCheia()

        val titulo = intent.getStringExtra("titulo") ?: "Hora de acordar!"
        val corpo = intent.getStringExtra("corpo") ?: ""
        val passoId = intent.getLongExtra("passoId", -1L)

        setContent {
            TelaAlarme(
                titulo, corpo,
                aoDesligar = {
                    AlarmeService.parar(this)
                    finish()
                },
                aoSoneca = { minutos ->
                    AlarmeService.parar(this)
                    Soneca.agendar(this, titulo, corpo, passoId, minutos)
                    finish()
                }
            )
        }
    }

    private fun aplicarFlagsTelaCheia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    override fun onBackPressed() {
        // Não sai pelo voltar sem desligar
    }
}

@Composable
private fun TelaAlarme(
    titulo: String,
    corpo: String,
    aoDesligar: () -> Unit,
    aoSoneca: (Int) -> Unit
) {
    var escolhendoSoneca by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Base).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MachittoAlarme(tamanho = 190.dp)
        Spacer(Modifier.height(28.dp))
        Text(titulo, color = TextoP, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (corpo.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(corpo, color = TextoS, fontSize = 16.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(44.dp))

        if (!escolhendoSoneca) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { escolhendoSoneca = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = TextoP),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) { Text("Soneca", fontSize = 16.sp, fontWeight = FontWeight.Medium) }

                Button(
                    onClick = aoDesligar,
                    colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Mantle),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) { Text("Desligar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }
        } else {
            Text("Soneca de quanto tempo?", color = TextoS, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20, 30, 60).forEach { min ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Lavanda)
                            .clickable { aoSoneca(min) }.padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$min min", color = Mantle, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Cancelar", color = TextoS, fontSize = 13.sp, modifier = Modifier.clickable { escolhendoSoneca = false }.padding(8.dp))
        }
    }
}
