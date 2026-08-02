package com.chronos.rotina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.salvarTemaNome
import com.chronos.rotina.data.salvarTemaPersonalizado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TemasScreen() {
    val ctx = LocalContext.current
    val p = TemaAtivo.paleta
    val escopo = rememberCoroutineScope()

    var corFundo by remember { mutableStateOf(PaletaCatppuccin.base) }
    var corPrincipal by remember { mutableStateOf(PaletaCatppuccin.principal) }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Text("Temas", color = p.textoP, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "Cada tema muda cor, fonte e formato. A carinha dele não muda 🐾",
            color = p.textoS,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        val emPares = TemasCurados.chunked(2)
        emPares.forEach { par ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                par.forEach { tema ->
                    Box(Modifier.weight(1f)) {
                        CartaoMachitto(
                            emoji = tema.emoji,
                            titulo = tema.nome,
                            legenda = tema.legenda,
                            corFundo = suavizar(tema.paleta.principal),
                            corTitulo = corTextoSobre(suavizar(tema.paleta.principal)),
                            corLegenda = corLegendaSobre(suavizar(tema.paleta.principal)),
                            fonteTitulo = tema.fonte.familia(),
                            selecionado = tema.nome == TemaAtivo.tema.nome,
                            aoTocar = {
                                TemaAtivo.aplicar(tema)
                                escopo.launch {
                                    withContext(Dispatchers.IO) { ctx.appDb().salvarTemaNome(tema.nome) }
                                }
                            }
                        )
                    }
                }
                if (par.size == 1) Box(Modifier.weight(1f)) {}
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(12.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TemaAtivo.raio + 6.dp))
                .background(p.surface)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(veuSobre(p.surface)),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", fontSize = 24.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("Do seu jeito", color = p.textoP, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "Escolha a cor de fundo e a principal",
                color = p.textoS,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            Text("Fundo", color = p.textoS, fontSize = 12.sp)
            LinhaCores(fundos, corFundo) { corFundo = it }

            Spacer(Modifier.height(10.dp))
            Text("Principal", color = p.textoS, fontSize = 12.sp)
            LinhaCores(principais, corPrincipal) { corPrincipal = it }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(TemaAtivo.raio))
                    .background(corPrincipal)
                    .clickable {
                        TemaAtivo.aplicarPersonalizado(corFundo, corPrincipal)
                        escopo.launch {
                            withContext(Dispatchers.IO) {
                                ctx.appDb().salvarTemaPersonalizado(
                                    corFundo.value.toLong(),
                                    corPrincipal.value.toLong()
                                )
                            }
                        }
                    }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aplicar meu tema", color = corTextoSobre(corPrincipal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LinhaCores(cores: List<Color>, selecionada: Color, aoEscolher: (Color) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cores.forEach { cor ->
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(cor)
                    .border(
                        width = if (cor == selecionada) 3.dp else 1.dp,
                        color = if (cor == selecionada) Color.White else Color(0x33FFFFFF),
                        shape = CircleShape
                    )
                    .clickable { aoEscolher(cor) }
            )
        }
    }
}

private val fundos = listOf(
    Color(0xFF1E1E2E), Color(0xFF05010D), Color(0xFF0B0F0A),
    Color(0xFF160A05), Color(0xFF120620), Color(0xFF080E1A), Color(0xFF101010)
)
private val principais = listOf(
    Color(0xFFB4BEFE), Color(0xFFFF2A6D), Color(0xFF39FF14), Color(0xFFFF6A13),
    Color(0xFFC77DFF), Color(0xFF05D9E8), Color(0xFFFF6AD5), Color(0xFFA6E3A1)
)
