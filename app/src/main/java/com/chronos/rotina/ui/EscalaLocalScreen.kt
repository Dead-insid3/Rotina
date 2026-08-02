package com.chronos.rotina.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.MachittoApp
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.escalaNoMes
import com.chronos.rotina.data.salvarEscalaMes
import com.chronos.rotina.data.PadraoEscala
import com.chronos.rotina.data.PadroesEscala
import com.chronos.rotina.data.gerarEscalaPorPadrao
import com.chronos.rotina.notif.Agendador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

private val cBase get() = TemaAtivo.paleta.base
private val cSurface get() = TemaAtivo.paleta.surface
private val cTextoP get() = TemaAtivo.paleta.textoP
private val cTextoS get() = TemaAtivo.paleta.textoS
private val cLavanda get() = TemaAtivo.paleta.principal
private val cMantle get() = TemaAtivo.paleta.mantle
private val cVerde get() = TemaAtivo.paleta.verde
private val cAmber get() = TemaAtivo.paleta.amber
private val cVermelho get() = TemaAtivo.paleta.vermelho

private val nomesMes = arrayOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)
private val diasSemana = arrayOf("D", "S", "T", "Q", "Q", "S", "S")

private fun d2(n: Int) = if (n < 10) "0$n" else "$n"
private fun dataStr(a: Int, m: Int, d: Int) = "$a-${d2(m)}-${d2(d)}"

private fun diasNoMes(ano: Int, mes: Int): Int {
    val c = Calendar.getInstance(); c.set(ano, mes - 1, 1)
    return c.getActualMaximum(Calendar.DAY_OF_MONTH)
}
private fun primeiroDiaSemana(ano: Int, mes: Int): Int {
    val c = Calendar.getInstance(); c.set(ano, mes - 1, 1)
    return c.get(Calendar.DAY_OF_WEEK) - 1
}

@Composable
fun EscalaLocalScreen() {
    val ctx = LocalContext.current
    val agora = remember { Calendar.getInstance() }
    var ano by remember { mutableStateOf(agora.get(Calendar.YEAR)) }
    var mes by remember { mutableStateOf(agora.get(Calendar.MONTH) + 1) }
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var marcados by remember { mutableStateOf(setOf<String>()) }
    var escolhendoPadrao by remember { mutableStateOf(false) }

    val hojeStr = dataStr(
        agora.get(Calendar.YEAR), agora.get(Calendar.MONTH) + 1, agora.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(ano, mes) {
        carregando = true
        aviso = null
        val set = withContext(Dispatchers.IO) { ctx.appDb().escalaNoMes(ano, mes) }
        marcados = set
        carregando = false
    }

    fun salvar() {
        salvando = true
        aviso = null
        val prefixo = "$ano-${d2(mes)}-"
        val doMes = marcados.filter { it.startsWith(prefixo) }.toSet()
        (ctx.applicationContext as MachittoApp).scopeApp.launch(Dispatchers.IO) {
            ctx.appDb().salvarEscalaMes(ano, mes, doMes)
            Agendador.reagendarTodos(ctx)
            val act = ctx as? Activity
            act?.runOnUiThread { salvando = false; aviso = "Escala salva! 🐾" }
        }
    }

    fun aplicarPadrao(padrao: PadraoEscala, diaInicio: Int, meses: Int) {
        salvando = true
        aviso = null
        (ctx.applicationContext as MachittoApp).scopeApp.launch(Dispatchers.IO) {
            ctx.appDb().gerarEscalaPorPadrao(padrao, ano, mes, diaInicio, meses)
            Agendador.reagendarTodos(ctx)
            val novo = ctx.appDb().escalaNoMes(ano, mes)
            val act = ctx as? Activity
            act?.runOnUiThread {
                marcados = novo
                salvando = false
                aviso = "Escala ${padrao.nome} aplicada! 🐾"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(cBase).padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.55f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(cSurface.copy(alpha = 0.7f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { if (mes == 1) { mes = 12; ano -= 1 } else mes -= 1 }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior", tint = cLavanda)
            }
            Text("${nomesMes[mes - 1]} $ano", color = cTextoP, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = { if (mes == 12) { mes = 1; ano += 1 } else mes += 1 }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês", tint = cLavanda)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            diasSemana.forEach {
                Text(it, Modifier.weight(1f), color = cTextoS, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (carregando) {
                MachittoCarregando(texto = "Buscando sua escala…")
            } else {
                val chaveMes = ano * 100 + mes
                AnimatedContent(
                    targetState = chaveMes,
                    transitionSpec = {
                        val indo = targetState > initialState
                        val entra = slideInHorizontally(tween(300)) { w -> if (indo) w else -w } + fadeIn(tween(300))
                        val sai = slideOutHorizontally(tween(300)) { w -> if (indo) -w else w } + fadeOut(tween(300))
                        entra togetherWith sai
                    },
                    label = "mes"
                ) { _ ->
                    Grade(ano, mes, marcados, hojeStr) { data ->
                        marcados = if (marcados.contains(data)) marcados - data else marcados + data
                    }
                }
            }
        }

        if (aviso != null) {
            Text(aviso!!, color = cVerde, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), textAlign = TextAlign.Center)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { escolhendoPadrao = true },
                enabled = !salvando && !carregando,
                colors = ButtonDefaults.buttonColors(containerColor = cSurface, contentColor = cTextoP),
                modifier = Modifier.weight(1f)
            ) {
                Text("Usar padrão", fontSize = 14.sp)
            }
            Button(
                onClick = { salvar() },
                enabled = !salvando && !carregando,
                colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (salvando) "Salvando…" else "Salvar mês", fontSize = 14.sp)
            }
        }
    }

    if (escolhendoPadrao) {
        DialogoPadraoEscala(
            aoFechar = { escolhendoPadrao = false },
            aoAplicar = { padrao, dia, meses ->
                escolhendoPadrao = false
                aplicarPadrao(padrao, dia, meses)
            }
        )
    }
}

@Composable
private fun DialogoPadraoEscala(
    aoFechar: () -> Unit,
    aoAplicar: (PadraoEscala, Int, Int) -> Unit
) {
    var selecionado by remember { mutableStateOf<PadraoEscala?>(null) }
    var diaInicio by remember { mutableStateOf("1") }
    var meses by remember { mutableStateOf("6") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = { Text("Padrão de escala", color = cTextoP) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Escolha seu padrão que eu preencho os próximos meses sozinho.",
                    color = cTextoS, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                PadroesEscala.forEach { p ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(TemaAtivo.raio))
                            .background(if (p == selecionado) cLavanda else cMantle)
                            .clickable { selecionado = p }
                            .padding(10.dp)
                    ) {
                        Text(p.nome, color = if (p == selecionado) cMantle else cTextoP, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(p.descricao, color = if (p == selecionado) cMantle else cTextoS, fontSize = 11.sp)
                    }
                }
                OutlinedTextField(
                    value = diaInicio, onValueChange = { diaInicio = it },
                    label = { Text("Começa no dia (deste mês)", color = cTextoS) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cLavanda, unfocusedBorderColor = cMantle,
                        focusedTextColor = cTextoP, unfocusedTextColor = cTextoP, cursorColor = cLavanda
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = meses, onValueChange = { meses = it },
                    label = { Text("Preencher quantos meses", color = cTextoS) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cLavanda, unfocusedBorderColor = cMantle,
                        focusedTextColor = cTextoP, unfocusedTextColor = cTextoP, cursorColor = cLavanda
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                Text(
                    "Isso substitui o que já estiver marcado nesses meses.",
                    color = cAmber, fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (erro != null) Text(erro!!, color = cVermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = selecionado
                    val d = diaInicio.toIntOrNull()
                    val m = meses.toIntOrNull()
                    when {
                        p == null -> erro = "Escolha um padrão"
                        d == null || d !in 1..31 -> erro = "Dia entre 1 e 31"
                        m == null || m !in 1..24 -> erro = "Entre 1 e 24 meses"
                        else -> aoAplicar(p, d, m)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle)
            ) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = cTextoS) } }
    )
}

@Composable
private fun Grade(ano: Int, mes: Int, marcados: Set<String>, hoje: String, aoTocar: (String) -> Unit) {
    val total = diasNoMes(ano, mes)
    val offset = primeiroDiaSemana(ano, mes)
    val linhas = (offset + total + 6) / 7
    Column(Modifier.fillMaxWidth()) {
        var dia = 1
        for (linha in 0 until linhas) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val indice = linha * 7 + col
                    if (indice < offset || dia > total) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val data = dataStr(ano, mes, dia)
                        Celula(dia, marcados.contains(data), data == hoje, Modifier.weight(1f)) { aoTocar(data) }
                        dia++
                    }
                }
            }
        }
    }
}

@Composable
private fun Celula(numero: Int, marcado: Boolean, hoje: Boolean, modifier: Modifier, aoTocar: () -> Unit) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()

    val escala by animateFloatAsState(
        targetValue = if (pressionado) 0.88f else if (marcado) 1f else 0.97f,
        animationSpec = tween(170),
        label = "celula"
    )
    val elevacao by animateDpAsState(
        targetValue = if (marcado) 6.dp else 0.dp,
        animationSpec = tween(200),
        label = "elevCelula"
    )
    val corFundo by animateColorAsState(
        targetValue = if (marcado) cLavanda else cSurface.copy(alpha = 0.55f),
        animationSpec = tween(220),
        label = "corCelula"
    )

    val forma = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .scale(escala)
            .shadow(
                elevation = elevacao,
                shape = forma,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(forma)
            .background(corFundo)
            .then(
                if (hoje && !marcado) Modifier.border(1.5.dp, cLavanda.copy(alpha = 0.7f), forma)
                else Modifier
            )
            .clickable(
                interactionSource = interacao,
                indication = null,
                onClick = aoTocar
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$numero",
            color = if (marcado) cMantle else if (hoje) cLavanda else cTextoP,
            fontWeight = if (hoje || marcado) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (hoje) 16.sp else 14.sp
        )
    }
}
