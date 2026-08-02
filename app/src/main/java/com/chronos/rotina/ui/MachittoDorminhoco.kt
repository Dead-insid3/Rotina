package com.chronos.rotina.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

private val Mantle = Color(0xFF181825)
private val Surface0 = Color(0xFF313244)
private val TextoP = Color(0xFFCDD6F4)
private val TextoS = Color(0xFFA6ADC8)
private val Amber = Color(0xFFF9E2AF)

private val reclamacoes = listOf(
    "Sai, humano! 😾",
    "Me deixa dormir…",
    "Zzz… ei, para de cutucar.",
    "Eu tava sonhando com atum!",
    "Miau. Tradução: me larga.",
    "De novo você? 🙄",
    "Respeita meu soninho, viu."
)

@Composable
fun MachittoDorminhoco(modifier: Modifier = Modifier) {
    var aberto by remember { mutableStateOf(false) }
    var reclamacao by remember { mutableStateOf("") }
    var mostrarReclamacao by remember { mutableStateOf(false) }
    var mostrarSobre by remember { mutableStateOf(false) }
    var mostrarAjuda by remember { mutableStateOf(false) }

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var arrastou by remember { mutableStateOf(false) }

    // auto-dormir: fecha o menu/reclamação depois de 8s parado
    LaunchedEffect(aberto, mostrarReclamacao) {
        if (aberto || mostrarReclamacao) {
            kotlinx.coroutines.delay(8000)
            aberto = false
            mostrarReclamacao = false
        }
    }

    if (mostrarSobre) {
        OverlayTela(conteudo = { SobreScreen() }, aoFechar = { mostrarSobre = false })
    }
    if (mostrarAjuda) {
        OverlayTela(conteudo = { AjudaScreen() }, aoFechar = { mostrarAjuda = false })
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { arrastou = false },
                    onDrag = { change, drag ->
                        change.consume()
                        offsetX += drag.x
                        offsetY += drag.y
                        arrastou = true
                    }
                )
            }
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            AnimatedVisibility(
                visible = mostrarReclamacao && !aberto,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Text(
                    text = reclamacao,
                    color = Amber,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .background(Mantle, RoundedCornerShape(TemaAtivo.raio))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            AnimatedVisibility(
                visible = aberto,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                MenuGatinho(
                    aoAjuda = { aberto = false; mostrarAjuda = true },
                    aoSobre = { aberto = false; mostrarSobre = true },
                    aoDormir = { aberto = false }
                )
            }

            if (!aberto) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (arrastou) {
                            arrastou = false
                        } else if (mostrarReclamacao) {
                            aberto = true
                            mostrarReclamacao = false
                        } else {
                            reclamacao = reclamacoes.random()
                            mostrarReclamacao = true
                        }
                    }
                ) {
                    MachittoRosto(
                        tamanho = 44.dp,
                        humor = if (mostrarReclamacao) HumorMachitto.IRRITADO else HumorMachitto.DORMINDO
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (mostrarReclamacao) "!" else "ZzZ",
                        color = TextoS,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuGatinho(aoAjuda: () -> Unit, aoSobre: () -> Unit, aoDormir: () -> Unit) {
    Column(
        modifier = Modifier
            .width(210.dp)
            .background(Mantle, RoundedCornerShape(TemaAtivo.raio))
            .border(1.dp, Surface0, RoundedCornerShape(TemaAtivo.raio))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MachittoRosto(tamanho = 34.dp, humor = HumorMachitto.NORMAL)
            Spacer(Modifier.width(8.dp))
            Text("Oi! Acordei 🐾", color = Amber, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(12.dp))
        ItemMenu("📖  Como usar", aoAjuda)
        ItemMenu("🤍  Sobre o Machitto", aoSobre)
        ItemMenu("😴  Voltar a dormir", aoDormir)
    }
}

@Composable
private fun ItemMenu(texto: String, aoTocar: () -> Unit) {
    Text(
        texto,
        color = TextoP,
        fontSize = 14.sp,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { aoTocar() }
    )
}

@Composable
private fun OverlayTela(conteudo: @Composable () -> Unit, aoFechar: () -> Unit) {
    Dialog(
        onDismissRequest = aoFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            conteudo()
            Text(
                "Voltar",
                color = Amber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .background(Mantle, RoundedCornerShape(TemaAtivo.raio))
                    .clickable { aoFechar() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}
