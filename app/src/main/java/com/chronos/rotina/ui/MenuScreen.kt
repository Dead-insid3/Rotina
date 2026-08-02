package com.chronos.rotina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen() {
    val p = TemaAtivo.paleta
    var destino by remember { mutableStateOf("") }

    if (destino == "temas") OverlayEdicao({ TemasScreen() }) { destino = "" }
    if (destino == "personalizacao") OverlayEdicao({ AjustesScreen() }) { destino = "" }
    if (destino == "sobre") OverlayEdicao({ SobreScreen() }) { destino = "" }
    if (destino == "ajuda") OverlayEdicao({ AjudaScreen() }) { destino = "" }
    if (destino == "sono") OverlayEdicao({ SonoScreen() }) { destino = "" }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        Text("Menu", color = p.textoP, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "Deixe o Machitto com a sua cara 🐾",
            color = p.textoS,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "🎨",
                    titulo = "Temas",
                    legenda = "Cores, fontes e formatos",
                    corFundo = suavizar(p.principal),
                    corTitulo = corTextoSobre(suavizar(p.principal)),
                    corLegenda = corLegendaSobre(suavizar(p.principal)),
                    aoTocar = { destino = "temas" }
                )
            }
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "🏷️",
                    titulo = "Personalizar",
                    legenda = "Suas tags e as falas dele",
                    corFundo = suavizar(p.secundaria),
                    corTitulo = corTextoSobre(suavizar(p.secundaria)),
                    corLegenda = corLegendaSobre(suavizar(p.secundaria)),
                    aoTocar = { destino = "personalizacao" }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "😴",
                    titulo = "Sono",
                    legenda = "Lembrete de dormir na folga",
                    corFundo = suavizar(p.verde),
                    corTitulo = corTextoSobre(suavizar(p.verde)),
                    corLegenda = corLegendaSobre(suavizar(p.verde)),
                    aoTocar = { destino = "sono" }
                )
            }
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "📖",
                    titulo = "Como usar",
                    legenda = "Um guia rapidinho",
                    corFundo = suavizar(p.amber),
                    corTitulo = corTextoSobre(suavizar(p.amber)),
                    corLegenda = corLegendaSobre(suavizar(p.amber)),
                    aoTocar = { destino = "ajuda" }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                CartaoMachitto(
                    emoji = "🤍",
                    titulo = "Sobre",
                    legenda = "A história do Machitto",
                    corFundo = suavizar(p.secundaria),
                    corTitulo = corTextoSobre(suavizar(p.secundaria)),
                    corLegenda = corLegendaSobre(suavizar(p.secundaria)),
                    aoTocar = { destino = "sobre" }
                )
            }
            Box(Modifier.weight(1f)) {}
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Machitto é apenas um Gato sem IA.\nNão julgue se ele for meio Burrinho. 🐾",
            color = p.textoS,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
