package com.chronos.rotina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chronos.rotina.ui.EscalaLocalScreen
import com.chronos.rotina.ui.CronogramaScreen
import com.chronos.rotina.ui.FinancasLocalScreen
import com.chronos.rotina.ui.MachittoDorminhoco
import com.chronos.rotina.ui.MachittoTemaWrapper
import com.chronos.rotina.ui.MenuScreen
import com.chronos.rotina.ui.RotinaLocalScreen
import com.chronos.rotina.ui.TemaAtivo

private val Base get() = TemaAtivo.paleta.base
private val Mantle get() = TemaAtivo.paleta.mantle
private val TextoS get() = TemaAtivo.paleta.textoS
private val Principal get() = TemaAtivo.paleta.principal

class PrincipalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MachittoTemaWrapper { TelaPrincipal() } }
    }
}

private enum class Aba(val titulo: String) {
    ESCALA("Escala"), ROTINA("Rotina"), CRONOGRAMA("Cronograma"), FINANCAS("Financeiro"), MENU("Menu")
}

private fun iconeDa(aba: Aba): ImageVector = when (aba) {
    Aba.ESCALA -> Icons.Filled.CalendarMonth
    Aba.ROTINA -> Icons.Filled.Checklist
    Aba.CRONOGRAMA -> Icons.Filled.HourglassBottom
    Aba.FINANCAS -> Icons.Filled.AttachMoney
    Aba.MENU -> Icons.Filled.Menu
}

@Composable
private fun TelaPrincipal() {
    var aba by remember { mutableStateOf(Aba.ESCALA) }

    Scaffold(
        containerColor = Base,
        bottomBar = { BarraFlutuante(aba) { aba = it } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Base)) {
            Crossfade(targetState = aba, animationSpec = tween(280), label = "aba") { atual ->
                Box(Modifier.fillMaxSize()) {
                    when (atual) {
                        Aba.ESCALA -> EscalaLocalScreen()
                        Aba.ROTINA -> RotinaLocalScreen()
                        Aba.CRONOGRAMA -> CronogramaScreen()
                        Aba.FINANCAS -> FinancasLocalScreen()
                        Aba.MENU -> MenuScreen()
                    }
                }
            }

            MachittoDorminhoco(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun BarraFlutuante(atual: Aba, aoTrocar: (Aba) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                )
                .clip(RoundedCornerShape(50))
                .background(Mantle)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Aba.values().forEach { a ->
                ItemBarra(
                    icone = iconeDa(a),
                    descricao = a.titulo,
                    selecionado = atual == a,
                    aoTocar = { aoTrocar(a) }
                )
            }
        }
    }
}

@Composable
private fun ItemBarra(
    icone: ImageVector,
    descricao: String,
    selecionado: Boolean,
    aoTocar: () -> Unit
) {
    val tamanhoBolha by animateDpAsState(
        targetValue = if (selecionado) 46.dp else 42.dp,
        animationSpec = tween(220),
        label = "bolha"
    )
    val escalaIcone by animateFloatAsState(
        targetValue = if (selecionado) 1.08f else 1f,
        animationSpec = tween(220),
        label = "icone"
    )
    val fundo = if (selecionado) Principal.copy(alpha = 0.22f) else Color.Transparent
    val corIcone = if (selecionado) Principal else TextoS
    val interacao = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(tamanhoBolha)
            .clip(CircleShape)
            .background(fundo)
            .clickable(
                interactionSource = interacao,
                indication = null,
                onClick = aoTocar
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icone,
            contentDescription = descricao,
            tint = corIcone,
            modifier = Modifier.size(22.dp).scale(escalaIcone)
        )
    }
}
