package com.chronos.rotina.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val PretoGato = Color(0xFF11111B)

@Composable
fun MachittoAlarme(tamanho: Dp = 180.dp, modifier: Modifier = Modifier) {
    val transicao = rememberInfiniteTransition(label = "alarme")
    val anguloRabo by transicao.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "rabo"
    )

    Box(modifier = modifier.size(tamanho), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(tamanho)) {
            val baseX = size.width * 0.80f
            val baseY = size.height * 0.70f
            rotate(degrees = anguloRabo, pivot = Offset(baseX, baseY)) {
                val rabo = Path().apply {
                    moveTo(baseX, baseY)
                    quadraticBezierTo(
                        baseX + size.width * 0.20f, baseY - size.height * 0.12f,
                        baseX + size.width * 0.12f, baseY - size.height * 0.38f
                    )
                }
                drawPath(rabo, PretoGato, style = Stroke(width = size.width * 0.10f, cap = StrokeCap.Round))
            }
        }
        MachittoRosto(tamanho = tamanho * 0.85f, humor = HumorMachitto.NORMAL)
    }
}
