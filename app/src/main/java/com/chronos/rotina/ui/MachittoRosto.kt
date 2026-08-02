package com.chronos.rotina.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class HumorMachitto { NORMAL, DORMINDO, IRRITADO }

private val Preto = Color(0xFF11111B)
private val PretoSuave = Color(0xFF181825)
private val Branco = Color(0xFFF5F5F5)
private val AmberClaro = Color(0xFFF9E2AF)
private val Amber = Color(0xFFF5A623)
private val RosaOrelha = Color(0xFFE8A0B8)
private val RosaFocinho = Color(0xFFCBA6A0)
private val Bigode = Color(0xFFE8E8F0)
private val TracoBoca = Color(0xFF3A3A4A)

@Composable
fun MachittoRosto(
    tamanho: Dp = 200.dp,
    humor: HumorMachitto = HumorMachitto.NORMAL,
    modifier: Modifier = Modifier
) {
    // Piscar: fica 1 (aberto) quase sempre, breve fecha
    val transicao = rememberInfiniteTransition(label = "machitto")
    val abertura by transicao.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3400
                0.1f at 3600
                1f at 3800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "piscar"
    )

    val fatorOlho = when (humor) {
        HumorMachitto.DORMINDO -> 0f
        HumorMachitto.IRRITADO -> 0.45f
        HumorMachitto.NORMAL -> abertura
    }

    Canvas(modifier = modifier.size(tamanho)) {
        val w = size.width
        val cx = w / 2f
        fun px(fx: Float) = fx / 380f * w
        fun py(fy: Float) = fy / 400f * size.height

        drawPath(tri(px(90f), py(150f), px(72f), py(62f), px(138f), py(108f)), PretoSuave)
        drawPath(tri(px(290f), py(150f), px(308f), py(62f), px(242f), py(108f)), PretoSuave)
        drawPath(tri(px(92f), py(142f), px(80f), py(88f), px(124f), py(116f)), RosaOrelha, alpha = 0.55f)
        drawPath(tri(px(288f), py(142f), px(300f), py(88f), px(256f), py(116f)), RosaOrelha, alpha = 0.55f)

        drawOval(Preto, topLeft = Offset(cx - px(122f), py(118f)), size = Size(px(244f), py(224f)))

        val peito = Path().apply {
            moveTo(px(120f), py(300f))
            quadraticBezierTo(cx, py(360f), px(260f), py(300f))
            quadraticBezierTo(px(258f), py(340f), cx, py(348f))
            quadraticBezierTo(px(122f), py(340f), px(120f), py(300f))
            close()
        }
        drawPath(peito, Branco)
        drawOval(Branco, topLeft = Offset(cx - px(40f), py(296f)), size = Size(px(80f), py(52f)))

        val ry = py(40f) * fatorOlho.coerceIn(0f, 1f)
        if (ry < py(6f)) {
            arcoOlho(Offset(px(142f), py(214f)), px(26f))
            arcoOlho(Offset(px(238f), py(214f)), px(26f))
        } else {
            olho(Offset(px(142f), py(212f)), px(34f), ry, humor == HumorMachitto.IRRITADO)
            olho(Offset(px(238f), py(212f)), px(34f), ry, humor == HumorMachitto.IRRITADO)
        }

        val focinho = Path().apply {
            moveTo(px(178f), py(258f))
            quadraticBezierTo(cx, py(268f), px(202f), py(258f))
            quadraticBezierTo(cx, py(276f), px(178f), py(258f))
            close()
        }
        drawPath(focinho, RosaFocinho)
        drawLine(TracoBoca, Offset(cx, py(268f)), Offset(cx, py(282f)), strokeWidth = px(2f), cap = StrokeCap.Round)
        tracoCurvo(cx, py(282f), px(176f), py(290f), px(168f), py(284f), px(2f))
        tracoCurvo(cx, py(282f), px(204f), py(290f), px(212f), py(284f), px(2f))

        val bigodes = listOf(
            Triple(Offset(px(150f), py(262f)), Offset(px(96f), py(256f)), Offset(px(60f), py(244f))),
            Triple(Offset(px(150f), py(270f)), Offset(px(98f), py(272f)), Offset(px(62f), py(272f))),
            Triple(Offset(px(150f), py(278f)), Offset(px(100f), py(288f)), Offset(px(68f), py(300f))),
            Triple(Offset(px(230f), py(262f)), Offset(px(284f), py(256f)), Offset(px(320f), py(244f))),
            Triple(Offset(px(230f), py(270f)), Offset(px(282f), py(272f)), Offset(px(318f), py(272f))),
            Triple(Offset(px(230f), py(278f)), Offset(px(280f), py(288f)), Offset(px(312f), py(300f)))
        )
        for (b in bigodes) {
            tracoCurvo(b.first.x, b.first.y, b.second.x, b.second.y, b.third.x, b.third.y, px(1.4f), Bigode, 0.8f)
        }
    }
}

private fun DrawScope.olho(centro: Offset, rx: Float, ry: Float, irritado: Boolean) {
    drawOval(AmberClaro, topLeft = Offset(centro.x - rx, centro.y - ry), size = Size(rx * 2, ry * 2))
    val rx2 = rx * 0.88f; val ry2 = ry * 0.9f
    drawOval(Amber, topLeft = Offset(centro.x - rx2, centro.y - ry2), size = Size(rx2 * 2, ry2 * 2))
    val prx = rx * (if (irritado) 0.30f else 0.44f)
    val pry = ry * 0.78f
    drawOval(Preto, topLeft = Offset(centro.x - prx, centro.y - pry), size = Size(prx * 2, pry * 2))
    drawCircle(Color.White.copy(alpha = 0.9f), radius = rx * 0.16f, center = Offset(centro.x + rx * 0.2f, centro.y - ry * 0.35f))
}

private fun DrawScope.arcoOlho(centro: Offset, largura: Float) {
    val p = Path().apply {
        moveTo(centro.x - largura, centro.y)
        quadraticBezierTo(centro.x, centro.y + largura * 0.6f, centro.x + largura, centro.y)
    }
    drawPath(p, TracoBoca, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

private fun DrawScope.tracoCurvo(
    x1: Float, y1: Float, cx: Float, cy: Float, x2: Float, y2: Float,
    largura: Float, cor: Color = TracoBoca, alfa: Float = 1f
) {
    val p = Path().apply { moveTo(x1, y1); quadraticBezierTo(cx, cy, x2, y2) }
    drawPath(p, cor, alpha = alfa, style = Stroke(width = largura, cap = StrokeCap.Round))
}

private fun tri(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Path {
    return Path().apply { moveTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3); close() }
}
