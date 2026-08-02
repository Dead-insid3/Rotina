package com.chronos.rotina.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val cBase get() = TemaAtivo.paleta.base
private val cTextoS get() = TemaAtivo.paleta.textoS
private val Preto = Color(0xFF11111B)

@Composable
fun MachittoCarregando(
    texto: String = "Só um instante…",
    modifier: Modifier = Modifier
) {
    val transicao = rememberInfiniteTransition(label = "carregando")
    val anguloRabo by transicao.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rabo"
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) {
                // corpo do gatinho (rostinho) por cima
                MachittoRosto(tamanho = 110.dp, humor = HumorMachitto.NORMAL)
                // rabinho balançando, atrás/ao lado
                Canvas(modifier = Modifier.size(140.dp)) {
                    val baseX = size.width * 0.82f
                    val baseY = size.height * 0.72f
                    rotate(degrees = anguloRabo, pivot = Offset(baseX, baseY)) {
                        val rabo = Path().apply {
                            moveTo(baseX, baseY)
                            quadraticBezierTo(
                                baseX + size.width * 0.18f, baseY - size.height * 0.10f,
                                baseX + size.width * 0.10f, baseY - size.height * 0.34f
                            )
                        }
                        drawPath(rabo, Preto, style = Stroke(width = size.width * 0.09f, cap = StrokeCap.Round))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(texto, color = cTextoS, fontSize = 14.sp)
        }
    }
}
