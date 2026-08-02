package com.chronos.rotina.ui

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AjustesScreen() {
    val p = TemaAtivo.paleta
    var tela by remember { mutableStateOf("") }

    if (tela == "tags") OverlayEdicao({ TagsScreen() }) { tela = "" }
    if (tela == "frases") OverlayEdicao({ FrasesScreen() }) { tela = "" }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.base)
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp)
    ) {
        Text("Personalizar", color = p.textoP, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "O Machitto se adapta a você 🐾",
            color = p.textoS,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "🏷️",
                    titulo = "Minhas tags",
                    legenda = "Os tipos de passo da rotina",
                    corFundo = suavizar(p.principal),
                    corTitulo = corTextoSobre(suavizar(p.principal)),
                    corLegenda = corLegendaSobre(suavizar(p.principal)),
                    aoTocar = { tela = "tags" }
                )
            }
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "💬",
                    titulo = "Falas dele",
                    legenda = "O que o gatinho diz",
                    corFundo = suavizar(p.secundaria),
                    corTitulo = corTextoSobre(suavizar(p.secundaria)),
                    corLegenda = corLegendaSobre(suavizar(p.secundaria)),
                    aoTocar = { tela = "frases" }
                )
            }
        }
    }
}

@Composable
fun OverlayEdicao(conteudo: @Composable () -> Unit, aoFechar: () -> Unit) {
    Dialog(onDismissRequest = aoFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(TemaAtivo.paleta.base).safeDrawingPadding()) {
            conteudo()
            Text(
                "Voltar",
                color = TemaAtivo.paleta.amber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(TemaAtivo.paleta.mantle, RoundedCornerShape(TemaAtivo.raio))
                    .clickable { aoFechar() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}
