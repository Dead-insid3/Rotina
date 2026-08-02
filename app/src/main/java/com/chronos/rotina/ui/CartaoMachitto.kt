package com.chronos.rotina.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CartaoMachitto(
    emoji: String,
    titulo: String,
    legenda: String,
    corFundo: Color,
    corTitulo: Color,
    corLegenda: Color,
    fonteTitulo: FontFamily? = null,
    selecionado: Boolean = false,
    alturaMinima: Int = 148,
    modifier: Modifier = Modifier,
    aoTocar: () -> Unit
) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()

    val escala by animateFloatAsState(
        targetValue = if (pressionado) 0.96f else 1f,
        animationSpec = tween(160),
        label = "escala"
    )
    val elevacao by animateDpAsState(
        targetValue = when {
            pressionado -> 2.dp
            selecionado -> 12.dp
            else -> 7.dp
        },
        animationSpec = tween(200),
        label = "elevacao"
    )

    val forma = RoundedCornerShape(TemaAtivo.raio + 10.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = alturaMinima.dp)
            .scale(escala)
            .shadow(
                elevation = elevacao,
                shape = forma,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(forma)
            .background(corFundo)
            .clickable(
                interactionSource = interacao,
                indication = null,
                onClick = aoTocar
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(veuSobre(corFundo)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 22.sp)
        }

        Spacer(Modifier.height(10.dp))

        Column {
            Text(
                text = titulo,
                color = corTitulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fonteTitulo,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = legenda,
                color = corLegenda,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            if (selecionado) {
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(veuSobre(corFundo))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text("em uso", color = corTitulo, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
