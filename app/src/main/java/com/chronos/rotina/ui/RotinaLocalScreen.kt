package com.chronos.rotina.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.MachittoApp
import com.chronos.rotina.data.PassoEntity
import com.chronos.rotina.data.RespostasRotina
import com.chronos.rotina.data.TagEntity
import com.chronos.rotina.data.adicionarPasso
import com.chronos.rotina.data.alternarAlarme
import com.chronos.rotina.data.alternarAtivoPasso
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.atualizarHorarioPasso
import com.chronos.rotina.data.definirPrioridade
import com.chronos.rotina.data.excluirPasso
import com.chronos.rotina.data.gerarRotina
import com.chronos.rotina.data.limparRotina
import com.chronos.rotina.data.passosOrdenados
import com.chronos.rotina.data.tagsParaEscolha
import com.chronos.rotina.notif.Agendador
import com.chronos.rotina.notif.PermissaoAlarme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val cBase get() = TemaAtivo.paleta.base
private val cSurface get() = TemaAtivo.paleta.surface
private val cMantle get() = TemaAtivo.paleta.mantle
private val cTextoP get() = TemaAtivo.paleta.textoP
private val cTextoS get() = TemaAtivo.paleta.textoS
private val cLavanda get() = TemaAtivo.paleta.principal
private val cRosa get() = TemaAtivo.paleta.secundaria
private val cAmber get() = TemaAtivo.paleta.amber
private val cVermelho get() = TemaAtivo.paleta.vermelho

private val horaRe = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

private fun normalizarHora(txt: String): String? {
    val d = txt.filter { it.isDigit() }
    if (d.isEmpty()) return null
    val h: Int
    val m: Int
    when {
        d.length <= 2 -> { h = d.toInt(); m = 0 }
        d.length == 3 -> { h = d.substring(0, 1).toInt(); m = d.substring(1).toInt() }
        else -> { h = d.substring(0, 2).toInt(); m = d.substring(2, 4).toInt() }
    }
    if (h > 23 || m > 59) return null
    return "%02d:%02d".format(h, m)
}

@Composable
fun RotinaLocalScreen() {
    val ctx = LocalContext.current
    var carregando by remember { mutableStateOf(true) }
    var passos by remember { mutableStateOf(listOf<PassoEntity>()) }
    var tags by remember { mutableStateOf(listOf<TagEntity>()) }
    var recarregar by remember { mutableStateOf(0) }
    var editandoHorario by remember { mutableStateOf<PassoEntity?>(null) }
    var editandoPrioridade by remember { mutableStateOf<PassoEntity?>(null) }
    var adicionando by remember { mutableStateOf(false) }
    var confirmandoRefazer by remember { mutableStateOf(false) }

    fun recarregarDados() { recarregar += 1 }
    fun scope() = (ctx.applicationContext as MachittoApp).scopeApp
    fun aplicarUi(bloco: () -> Unit) { (ctx as? Activity)?.runOnUiThread(bloco) }

    LaunchedEffect(recarregar) {
        carregando = true
        val db = ctx.appDb()
        val ps = withContext(Dispatchers.IO) { db.passosOrdenados() }
        val ts = withContext(Dispatchers.IO) { db.tagsParaEscolha() }
        passos = ps; tags = ts; carregando = false
    }

    Box(Modifier.fillMaxSize().background(cBase)) {
        when {
            carregando -> MachittoCarregando(texto = "Preparando sua rotina…")
            passos.isEmpty() -> Assistente { r ->
                scope().launch(Dispatchers.IO) {
                    ctx.appDb().gerarRotina(r)
                    Agendador.reagendarTodos(ctx)
                    aplicarUi { recarregarDados() }
                }
            }
            else -> ListaPassos(
                passos = passos,
                onEditarHorario = { editandoHorario = it },
                onAlternarAtivo = { p -> scope().launch(Dispatchers.IO) { ctx.appDb().alternarAtivoPasso(p); Agendador.reagendarTodos(ctx); aplicarUi { recarregarDados() } } },
                onAlternarAlarme = { p -> scope().launch(Dispatchers.IO) { ctx.appDb().alternarAlarme(p); Agendador.reagendarTodos(ctx); aplicarUi { recarregarDados() } } },
                onPrioridade = { editandoPrioridade = it },
                onExcluir = { p -> scope().launch(Dispatchers.IO) { ctx.appDb().excluirPasso(p); Agendador.reagendarTodos(ctx); aplicarUi { recarregarDados() } } },
                onAdicionar = { adicionando = true },
                onRefazer = { confirmandoRefazer = true }
            )
        }
    }

    editandoHorario?.let { p ->
        DialogoHorario(p, aoFechar = { editandoHorario = null }, aoSalvar = { novo ->
            scope().launch(Dispatchers.IO) { ctx.appDb().atualizarHorarioPasso(p, novo); Agendador.reagendarTodos(ctx); aplicarUi { recarregarDados() } }
            editandoHorario = null
        })
    }

    editandoPrioridade?.let { p ->
        DialogoPrioridade(p, aoFechar = { editandoPrioridade = null }, aoEscolher = { pri ->
            scope().launch(Dispatchers.IO) { ctx.appDb().definirPrioridade(p, pri); aplicarUi { recarregarDados() } }
            editandoPrioridade = null
        })
    }

    if (confirmandoRefazer) {
        DialogoConfirmarRefazer(
            aoFechar = { confirmandoRefazer = false },
            aoConfirmar = {
                confirmandoRefazer = false
                scope().launch(Dispatchers.IO) {
                    ctx.appDb().limparRotina()
                    Agendador.reagendarTodos(ctx)
                    aplicarUi { recarregarDados() }
                }
            }
        )
    }

    if (adicionando) {
        DialogoAdicionar(tags, aoFechar = { adicionando = false }, aoAdicionar = { tag, hora ->
            scope().launch(Dispatchers.IO) { ctx.appDb().adicionarPasso(tag, hora); Agendador.reagendarTodos(ctx); aplicarUi { recarregarDados() } }
            adicionando = false
        })
    }
}

@Composable
private fun Assistente(aoGerar: (RespostasRotina) -> Unit) {
    var entrada by remember { mutableStateOf("") }
    var saida by remember { mutableStateOf("") }
    var desloc by remember { mutableStateOf("") }
    var preparo by remember { mutableStateOf("") }
    var sono by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Bora montar sua rotina!", color = cTextoP, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Text(
            "Me conta uns detalhes que eu calculo tudo pra você. Depois é só ajustar.",
            color = cTextoS, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )

        CampoHora("Que horas você entra no trabalho?", entrada) { entrada = it }
        CampoHora("Que horas você sai?", saida) { saida = it }
        CampoNumero("Quanto tempo de deslocamento? (minutos)", desloc) { desloc = it }
        CampoNumero("Tempo pra se arrumar? (minutos: banho, café...)", preparo) { preparo = it }
        CampoNumero("Quantas horas quer dormir?", sono) { sono = it }

        if (erro != null) {
            Text(erro!!, color = cVermelho, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val entradaN = normalizarHora(entrada)
                val saidaN = normalizarHora(saida)
                when {
                    entradaN == null -> erro = "Horário de entrada inválido (ex: 3 ou 0300)"
                    saidaN == null -> erro = "Horário de saída inválido (ex: 1233)"
                    desloc.toIntOrNull() == null -> erro = "Deslocamento em minutos (ex: 80)"
                    preparo.toIntOrNull() == null -> erro = "Preparo em minutos (ex: 60)"
                    sono.toIntOrNull() == null -> erro = "Horas de sono (ex: 7)"
                    else -> {
                        erro = null
                        aoGerar(
                            RespostasRotina(
                                entrada = entradaN, saida = saidaN,
                                deslocMin = desloc.toInt(),
                                preparoMin = preparo.toInt(),
                                sonoMin = sono.toInt() * 60
                            )
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) { Text("Gerar minha rotina 🐾") }
    }
}

@Composable
private fun CampoHora(rotulo: String, valor: String, onChange: (String) -> Unit) {
    Text(rotulo, color = cTextoP, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        placeholder = { Text("ex: 03:00 ou 0300", color = cTextoS) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = campoCores(), modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CampoNumero(rotulo: String, valor: String, onChange: (String) -> Unit) {
    Text(rotulo, color = cTextoP, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = campoCores(), modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun campoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = cLavanda, unfocusedBorderColor = cSurface,
    focusedTextColor = cTextoP, unfocusedTextColor = cTextoP, cursorColor = cLavanda
)

@Composable
private fun ListaPassos(
    passos: List<PassoEntity>,
    onEditarHorario: (PassoEntity) -> Unit,
    onAlternarAtivo: (PassoEntity) -> Unit,
    onAlternarAlarme: (PassoEntity) -> Unit,
    onPrioridade: (PassoEntity) -> Unit,
    onExcluir: (PassoEntity) -> Unit,
    onAdicionar: () -> Unit,
    onRefazer: () -> Unit
) {
    val ctx = LocalContext.current
    var semPermissao by remember { mutableStateOf(!PermissaoAlarme.temPermissaoExata(ctx)) }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        if (semPermissao) {
            Box(
                Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(TemaAtivo.raio))
                    .background(cAmber).clickable {
                        PermissaoAlarme.abrirConfigAlarmeExato(ctx)
                    }.padding(12.dp)
            ) {
                Text(
                    "⚠️ Os alarmes precisam de permissão. Toque aqui para liberar 'Alarmes e lembretes'.",
                    color = cMantle, fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sua rotina", color = cTextoP, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Row {
                TextButton(onClick = onAdicionar) { Text("+ Passo", color = cLavanda) }
                TextButton(onClick = onRefazer) { Text("Refazer", color = cRosa) }
            }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(passos, key = { it.id }) { p ->
                androidx.compose.foundation.layout.Box(Modifier.animateItem()) {
                    LinhaPasso(
                        p,
                        onTocar = { onEditarHorario(p) },
                        onAtivo = { onAlternarAtivo(p) },
                        onAlarme = { onAlternarAlarme(p) },
                        onPrioridade = { onPrioridade(p) },
                        onExcluir = { onExcluir(p) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinhaPasso(
    p: PassoEntity,
    onTocar: () -> Unit,
    onAtivo: () -> Unit,
    onAlarme: () -> Unit,
    onPrioridade: () -> Unit,
    onExcluir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(TemaAtivo.raio))
            .background(cSurface)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (p.emoji.isNotBlank()) p.emoji else "⏰", modifier = Modifier.padding(end = 10.dp), fontSize = 20.sp)
            Column(Modifier.weight(1f).clickable { onTocar() }) {
                Text(p.label, color = if (p.ativo) cTextoP else cTextoS, fontWeight = FontWeight.Medium)
                val sufixo = if (p.dayOffset >= 1) " • véspera" else ""
                Text(p.fireTime + sufixo, color = cTextoS, fontSize = 13.sp)
            }
            Switch(
                checked = p.ativo, onCheckedChange = { onAtivo() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = cMantle, checkedTrackColor = cLavanda,
                    uncheckedThumbColor = cTextoS, uncheckedTrackColor = cMantle
                )
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAlarme) {
                Icon(
                    if (p.alarme) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                    contentDescription = "Alarme",
                    tint = if (p.alarme) cAmber else cTextoS
                )
            }
            Text(if (p.alarme) "Alarme" else "Notificação", color = cTextoS, fontSize = 12.sp)
            TextButton(onClick = onPrioridade) {
                Text("Prioridade: ${p.prioridade}", color = cLavanda, fontSize = 12.sp)
            }
            Box(Modifier.weight(1f))
            IconButton(onClick = onExcluir) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = cVermelho)
            }
        }
    }
}

@Composable
private fun DialogoHorario(passo: PassoEntity, aoFechar: () -> Unit, aoSalvar: (String) -> Unit) {
    var hora by remember { mutableStateOf(passo.fireTime) }
    var erro by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = { Text(passo.label, color = cTextoP) },
        text = {
            Column {
                OutlinedTextField(
                    value = hora, onValueChange = { hora = it },
                    label = { Text("Horário (ex: 06:30)", color = cTextoS) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = campoCores(), modifier = Modifier.fillMaxWidth()
                )
                if (passo.dayOffset >= 1) Text("Cai na véspera do dia de trabalho.", color = cTextoS, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                if (erro != null) Text(erro!!, color = cVermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { val n = normalizarHora(hora); if (n != null) aoSalvar(n) else erro = "Horário inválido (ex: 630 = 06:30)" },
                colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = cTextoS) } }
    )
}

@Composable
private fun DialogoPrioridade(passo: PassoEntity, aoFechar: () -> Unit, aoEscolher: (String) -> Unit) {
    val opcoes = listOf("baixa", "normal", "alta")
    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = { Text("Prioridade — ${passo.label}", color = cTextoP) },
        text = {
            Column {
                opcoes.forEach { o ->
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(TemaAtivo.raio))
                            .background(if (o == passo.prioridade) cLavanda else cMantle)
                            .clickable { aoEscolher(o) }.padding(12.dp)
                    ) {
                        Text(o.replaceFirstChar { it.uppercase() }, color = if (o == passo.prioridade) cMantle else cTextoP)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = aoFechar) { Text("Fechar", color = cTextoS) } }
    )
}

@Composable
private fun DialogoAdicionar(tags: List<TagEntity>, aoFechar: () -> Unit, aoAdicionar: (TagEntity, String) -> Unit) {
    var tagSel by remember { mutableStateOf<TagEntity?>(null) }
    var hora by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = { Text("Adicionar passo", color = cTextoP) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("O que é?", color = cTextoS, fontSize = 13.sp)
                Column(
                    Modifier
                        .padding(vertical = 6.dp)
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    tags.forEach { t ->
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(TemaAtivo.raio))
                                .background(if (t == tagSel) cLavanda else cMantle)
                                .clickable { tagSel = t }.padding(10.dp)
                        ) {
                            Text("${t.emoji} ${t.label}", color = if (t == tagSel) cMantle else cTextoP, fontSize = 13.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = hora, onValueChange = { hora = it },
                    label = { Text("Horário (ex: 15:00)", color = cTextoS) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = campoCores(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                if (erro != null) Text(erro!!, color = cVermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = tagSel
                    when {
                        t == null -> erro = "Escolha o tipo"
                        normalizarHora(hora) == null -> erro = "Horário inválido (ex: 1500 = 15:00)"
                        else -> aoAdicionar(t, normalizarHora(hora)!!)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle)
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = cTextoS) } }
    )
}

@Composable
private fun DialogoConfirmarRefazer(aoFechar: () -> Unit, aoConfirmar: () -> Unit) {
    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MachittoRosto(tamanho = 36.dp, humor = HumorMachitto.IRRITADO)
                Spacer(Modifier.width(10.dp))
                Text("Opa, calma aí!", color = cTextoP, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Deseja realmente refazer sua rotina?",
                    color = cTextoP,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Isso vai apagar todos os passos criados até agora, incluindo os que você ajustou na mão. Não dá pra desfazer.",
                    color = cTextoS,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = aoConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = cVermelho, contentColor = cMantle)
            ) { Text("Sim, refazer", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) { Text("Deixa quieto", color = cTextoS) }
        }
    )
}