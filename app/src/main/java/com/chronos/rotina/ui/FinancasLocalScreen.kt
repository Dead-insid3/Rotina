package com.chronos.rotina.ui

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.chronos.rotina.data.CategoriaEntity
import com.chronos.rotina.data.ContaEntity
import com.chronos.rotina.data.RendaEntity
import com.chronos.rotina.data.ResumoFinanceiro
import com.chronos.rotina.data.adicionarCategoria
import com.chronos.rotina.data.adicionarConta
import com.chronos.rotina.data.adicionarRenda
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.categoriasTodas
import com.chronos.rotina.data.contasAtivas
import com.chronos.rotina.data.definirModoPagamento
import com.chronos.rotina.data.marcarPaga
import com.chronos.rotina.data.modoPagamento
import com.chronos.rotina.data.removerCategoria
import com.chronos.rotina.data.removerConta
import com.chronos.rotina.data.removerRenda
import com.chronos.rotina.data.rendasAtivas
import com.chronos.rotina.data.resumoDoMes
import com.chronos.rotina.data.descricaoDia
import com.chronos.rotina.notif.LembreteFinanceiro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

private val cBase get() = TemaAtivo.paleta.base
private val cSurface get() = TemaAtivo.paleta.surface
private val cMantle get() = TemaAtivo.paleta.mantle
private val cTextoP get() = TemaAtivo.paleta.textoP
private val cTextoS get() = TemaAtivo.paleta.textoS
private val cLavanda get() = TemaAtivo.paleta.principal
private val cRosa get() = TemaAtivo.paleta.secundaria
private val cVerde get() = TemaAtivo.paleta.verde
private val cVermelho get() = TemaAtivo.paleta.vermelho
private val cAmber get() = TemaAtivo.paleta.amber

private enum class AbaFin(val titulo: String) { SALDO("Saldo"), CONTAS("Contas"), RENDA("Renda") }

private fun moeda(v: Double): String {
    val inteiro = kotlin.math.floor(kotlin.math.abs(v)).toLong()
    val centavos = kotlin.math.round((kotlin.math.abs(v) - inteiro) * 100).toInt()
    val sInt = inteiro.toString().reversed().chunked(3).joinToString(".").reversed()
    val sinal = if (v < 0) "-" else ""
    return "%sR$ %s,%02d".format(sinal, sInt, centavos)
}

private fun parseDinheiro(txt: String): Double? {
    if (txt.isBlank()) return null
    var s = txt.trim().replace(" ", "").replace("R$", "").replace("r$", "")
    s = s.replace(".", "")
    s = s.replace(",", ".")
    s = s.filter { it.isDigit() || it == '.' }
    if (s.isEmpty()) return null
    return s.toDoubleOrNull()
}

@Composable
fun FinancasLocalScreen() {
    val ctx = LocalContext.current
    var subaba by remember { mutableStateOf(AbaFin.SALDO) }
    var recarregar by remember { mutableStateOf(0) }
    var carregando by remember { mutableStateOf(true) }

    var resumo by remember { mutableStateOf<ResumoFinanceiro?>(null) }
    var contas by remember { mutableStateOf(listOf<ContaEntity>()) }
    var rendas by remember { mutableStateOf(listOf<RendaEntity>()) }
    var categorias by remember { mutableStateOf(listOf<CategoriaEntity>()) }
    var modo by remember { mutableStateOf("vencimento") }

    fun recarregarDados() { recarregar += 1 }
    fun scope() = (ctx.applicationContext as MachittoApp).scopeApp
    fun aplicarUi(bloco: () -> Unit) { (ctx as? Activity)?.runOnUiThread(bloco) }

    LaunchedEffect(recarregar) {
        carregando = true
        val db = ctx.appDb()
        val diaAtual = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val r = withContext(Dispatchers.IO) { db.resumoDoMes(diaAtual) }
        val cs = withContext(Dispatchers.IO) { db.contasAtivas() }
        val rs = withContext(Dispatchers.IO) { db.rendasAtivas() }
        val cats = withContext(Dispatchers.IO) { db.categoriasTodas() }
        val m = withContext(Dispatchers.IO) { db.modoPagamento() }
        resumo = r; contas = cs; rendas = rs; categorias = cats; modo = m
        carregando = false
    }

    var addConta by remember { mutableStateOf(false) }
    var addRenda by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(cBase)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AbaFin.values().forEach { a ->
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(TemaAtivo.raio))
                        .background(if (subaba == a) cLavanda else cSurface)
                        .clickable { subaba = a }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(a.titulo, color = if (subaba == a) cMantle else cTextoS, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (carregando) {
            MachittoCarregando(texto = "Contando as moedas…")
        } else {
            Crossfade(targetState = subaba, animationSpec = tween(250), label = "subaba") { atual ->
            when (atual) {
                AbaFin.SALDO -> AbaSaldo(resumo, modo, onModo = { novo ->
                    scope().launch(Dispatchers.IO) { ctx.appDb().definirModoPagamento(novo); LembreteFinanceiro.reagendarTodos(ctx); aplicarUi { recarregarDados() } }
                })
                AbaFin.CONTAS -> AbaContas(
                    contas = contas, modo = modo,
                    onAdicionar = { addConta = true },
                    onPaga = { c, v -> scope().launch(Dispatchers.IO) { ctx.appDb().marcarPaga(c, v); LembreteFinanceiro.reagendarTodos(ctx); aplicarUi { recarregarDados() } } },
                    onExcluir = { c -> scope().launch(Dispatchers.IO) { ctx.appDb().removerConta(c); LembreteFinanceiro.reagendarTodos(ctx); aplicarUi { recarregarDados() } } }
                )
                AbaFin.RENDA -> AbaRenda(
                    rendas = rendas,
                    onAdicionar = { addRenda = true },
                    onExcluir = { r -> scope().launch(Dispatchers.IO) { ctx.appDb().removerRenda(r); aplicarUi { recarregarDados() } } }
                )
            }
            }
        }
    }

    if (addConta) {
        DialogoConta(categorias, aoFechar = { addConta = false }, aoAdicionarCategoria = { nome ->
            scope().launch(Dispatchers.IO) { ctx.appDb().adicionarCategoria(nome); aplicarUi { recarregarDados() } }
        }, aoSalvar = { conta ->
            scope().launch(Dispatchers.IO) { ctx.appDb().adicionarConta(conta); LembreteFinanceiro.reagendarTodos(ctx); aplicarUi { recarregarDados() } }
            addConta = false
        })
    }

    if (addRenda) {
        DialogoRenda(aoFechar = { addRenda = false }, aoSalvar = { renda ->
            scope().launch(Dispatchers.IO) { ctx.appDb().adicionarRenda(renda); aplicarUi { recarregarDados() } }
            addRenda = false
        })
    }
}

@Composable
private fun AbaSaldo(resumo: ResumoFinanceiro?, modo: String, onModo: (String) -> Unit) {
    if (resumo == null) return
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(TemaAtivo.raio)).background(cSurface).padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Livre este mês", color = cTextoS, fontSize = 14.sp)
                Text(
                    moeda(resumo.livre),
                    color = if (resumo.livre >= 0) cVerde else cVermelho,
                    fontSize = 34.sp, fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Renda", color = cTextoS, fontSize = 12.sp)
                        Text(moeda(resumo.totalRenda), color = cVerde, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gastos", color = cTextoS, fontSize = 12.sp)
                        Text(moeda(resumo.totalGastos), color = cVermelho, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Text("Como contar as contas?", color = cTextoP, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
        OpcaoModo("Só pelo vencimento", "Conta sai do cálculo quando passa o dia de vencimento.", modo == "vencimento") { onModo("vencimento") }
        OpcaoModo("Exigir pagamento", "Conta só sai quando você marca como paga (o Machitto insiste).", modo == "pago") { onModo("pago") }

        if (resumo.contasEmAberto.isNotEmpty()) {
            Text("Em aberto", color = cAmber, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
            resumo.contasEmAberto.take(5).forEach { c ->
                Text("• ${c.label} — ${moeda(c.valor)} (vence ${c.descricaoDia()})", color = cTextoS, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun OpcaoModo(titulo: String, desc: String, sel: Boolean, onTocar: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(TemaAtivo.raio))
            .background(if (sel) cLavanda else cSurface).clickable { onTocar() }.padding(12.dp)
    ) {
        Column {
            Text(titulo, color = if (sel) cMantle else cTextoP, fontWeight = FontWeight.Medium)
            Text(desc, color = if (sel) cMantle else cTextoS, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AbaContas(
    contas: List<ContaEntity>, modo: String,
    onAdicionar: () -> Unit, onPaga: (ContaEntity, Boolean) -> Unit, onExcluir: (ContaEntity) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Suas contas", color = cTextoP, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            TextButton(onClick = onAdicionar) { Text("+ Conta", color = cLavanda) }
        }
        if (contas.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Nenhuma conta ainda.", color = cTextoS) }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(contas, key = { it.id }) { c ->
                    Box(Modifier.animateItem()) {
                        LinhaConta(c, modo, onPaga = { v -> onPaga(c, v) }, onExcluir = { onExcluir(c) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaConta(c: ContaEntity, modo: String, onPaga: (Boolean) -> Unit, onExcluir: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(TemaAtivo.raio)).background(cSurface).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (modo == "pago") {
            Checkbox(
                checked = c.paga, onCheckedChange = { onPaga(it) },
                colors = CheckboxDefaults.colors(checkedColor = cVerde, uncheckedColor = cTextoS, checkmarkColor = cMantle)
            )
        }
        Column(Modifier.weight(1f)) {
            val tipoTxt = when (c.tipo) {
                "parcelada" -> "Parcelada ${c.parcelaAtual}/${c.parcelaTotal}"
                "recorrente" -> "Recorrente"
                else -> "Pontual"
            }
            Text(c.label, color = cTextoP, fontWeight = FontWeight.Medium)
            Text("$tipoTxt • vence ${c.descricaoDia()}${if (c.categoria.isNotBlank()) " • ${c.categoria}" else ""}", color = cTextoS, fontSize = 12.sp)
        }
        Text(moeda(c.valor), color = cVermelho, fontWeight = FontWeight.Medium, modifier = Modifier.padding(end = 4.dp))
        IconButton(onClick = onExcluir) { Icon(Icons.Filled.Delete, "Excluir", tint = cVermelho) }
    }
}

@Composable
private fun AbaRenda(rendas: List<RendaEntity>, onAdicionar: () -> Unit, onExcluir: (RendaEntity) -> Unit) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Suas rendas", color = cTextoP, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            TextButton(onClick = onAdicionar) { Text("+ Renda", color = cLavanda) }
        }
        if (rendas.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Nenhuma renda ainda.", color = cTextoS) }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(rendas, key = { it.id }) { r ->
                    Row(
                        Modifier.animateItem().fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(TemaAtivo.raio)).background(cSurface).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            val tipoTxt = when (r.tipo) {
                                "fixa" -> "Fixa"
                                "recorrente_variavel" -> "Recorrente (valor varia)"
                                else -> "Pontual"
                            }
                            Text(r.label, color = cTextoP, fontWeight = FontWeight.Medium)
                            Text("$tipoTxt • ${r.descricaoDia()}", color = cTextoS, fontSize = 12.sp)
                        }
                        Text(moeda(r.valor), color = cVerde, fontWeight = FontWeight.Medium, modifier = Modifier.padding(end = 4.dp))
                        IconButton(onClick = { onExcluir(r) }) { Icon(Icons.Filled.Delete, "Excluir", tint = cVermelho) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoConta(
    categorias: List<CategoriaEntity>,
    aoFechar: () -> Unit,
    aoAdicionarCategoria: (String) -> Unit,
    aoSalvar: (ContaEntity) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var porDiaUtil by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf("recorrente") }
    var parcelas by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var novaCat by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = { Text("Nova conta", color = cTextoP) },
        text = {
            Column {
                CampoTxt("Nome (ex: Luz)", label) { label = it }
                CampoNum("Valor (ex: 150)", valor) { valor = it }
                Text("Quando vence?", color = cTextoS, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipTipo("Dia fixo", !porDiaUtil) { porDiaUtil = false }
                    ChipTipo("Dia útil", porDiaUtil) { porDiaUtil = true }
                }
                CampoNum(
                    if (porDiaUtil) "Qual dia útil? (ex: 5 = 5º dia útil)" else "Dia do vencimento (1-31)",
                    dia
                ) { dia = it }
                if (porDiaUtil) {
                    Text(
                        "O Machitto calcula a data certa todo mês, pulando fins de semana e feriados nacionais.",
                        color = cTextoS, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text("Tipo", color = cTextoS, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipTipo("Recorrente", tipo == "recorrente") { tipo = "recorrente" }
                    ChipTipo("Parcelada", tipo == "parcelada") { tipo = "parcelada" }
                    ChipTipo("Pontual", tipo == "pontual") { tipo = "pontual" }
                }
                if (tipo == "parcelada") {
                    CampoNum("Quantas parcelas?", parcelas) { parcelas = it }
                }
                if (categorias.isNotEmpty()) {
                    Text("Categoria", color = cTextoS, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categorias.take(4).forEach { cat ->
                            ChipTipo(cat.nome, categoria == cat.nome) { categoria = cat.nome }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    OutlinedTextField(
                        value = novaCat, onValueChange = { novaCat = it },
                        label = { Text("Nova categoria", color = cTextoS) }, singleLine = true,
                        colors = campoCoresFin(), modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { if (novaCat.isNotBlank()) { aoAdicionarCategoria(novaCat); categoria = novaCat.trim(); novaCat = "" } }) {
                        Text("+", color = cLavanda, fontSize = 20.sp)
                    }
                }
                if (erro != null) Text(erro!!, color = cVermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = parseDinheiro(valor)
                    val d = dia.toIntOrNull()
                    when {
                        label.isBlank() -> erro = "Dê um nome"
                        v == null -> erro = "Valor inválido"
                        d == null -> erro = "Informe o dia"
                        porDiaUtil && d !in 1..23 -> erro = "Dia útil entre 1 e 23"
                        !porDiaUtil && d !in 1..31 -> erro = "Dia entre 1 e 31"
                        tipo == "parcelada" && (parcelas.toIntOrNull() == null) -> erro = "Informe as parcelas"
                        else -> aoSalvar(
                            ContaEntity(
                                label = label.trim(), valor = v, diaVencimento = d, tipo = tipo,
                                porDiaUtil = porDiaUtil,
                                categoria = categoria,
                                parcelaAtual = 1,
                                parcelaTotal = if (tipo == "parcelada") parcelas.toInt() else 1
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = cTextoS) } }
    )
}

@Composable
private fun DialogoRenda(aoFechar: () -> Unit, aoSalvar: (RendaEntity) -> Unit) {
    var label by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var porDiaUtil by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf("fixa") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        containerColor = cSurface,
        onDismissRequest = aoFechar,
        title = { Text("Nova renda", color = cTextoP) },
        text = {
            Column {
                CampoTxt("Nome (ex: Salário)", label) { label = it }
                CampoNum("Valor (ex: 2000)", valor) { valor = it }
                Text("Quando recebe?", color = cTextoS, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipTipo("Dia fixo", !porDiaUtil) { porDiaUtil = false }
                    ChipTipo("Dia útil", porDiaUtil) { porDiaUtil = true }
                }
                CampoNum(
                    if (porDiaUtil) "Qual dia útil? (ex: 5 = 5º dia útil)" else "Dia do recebimento (1-31)",
                    dia
                ) { dia = it }
                if (porDiaUtil) {
                    Text(
                        "Muita empresa paga no 5º dia útil. O Machitto calcula a data certa todo mês.",
                        color = cTextoS, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text("Tipo", color = cTextoS, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ChipTipoLargo("Fixa (salário, todo mês igual)", tipo == "fixa") { tipo = "fixa" }
                    ChipTipoLargo("Recorrente com valor variável (freela)", tipo == "recorrente_variavel") { tipo = "recorrente_variavel" }
                    ChipTipoLargo("Pontual (extra único)", tipo == "pontual") { tipo = "pontual" }
                }
                if (erro != null) Text(erro!!, color = cVermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = parseDinheiro(valor)
                    val d = dia.toIntOrNull()
                    when {
                        label.isBlank() -> erro = "Dê um nome"
                        v == null -> erro = "Valor inválido"
                        d == null -> erro = "Informe o dia"
                        porDiaUtil && d !in 1..23 -> erro = "Dia útil entre 1 e 23"
                        !porDiaUtil && d !in 1..31 -> erro = "Dia entre 1 e 31"
                        else -> aoSalvar(RendaEntity(label = label.trim(), valor = v, diaPagamento = d, tipo = tipo, porDiaUtil = porDiaUtil))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = cLavanda, contentColor = cMantle)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = cTextoS) } }
    )
}

@Composable
private fun ChipTipo(texto: String, sel: Boolean, onTocar: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(TemaAtivo.raio)).background(if (sel) cLavanda else cMantle).clickable { onTocar() }.padding(horizontal = 12.dp, vertical = 8.dp)
    ) { Text(texto, color = if (sel) cMantle else cTextoP, fontSize = 12.sp) }
}

@Composable
private fun ChipTipoLargo(texto: String, sel: Boolean, onTocar: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(TemaAtivo.raio)).background(if (sel) cLavanda else cMantle).clickable { onTocar() }.padding(12.dp)
    ) { Text(texto, color = if (sel) cMantle else cTextoP, fontSize = 13.sp) }
}

@Composable
private fun CampoTxt(rotulo: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(rotulo, color = cTextoS) }, singleLine = true,
        colors = campoCoresFin(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    )
}

@Composable
private fun CampoNum(rotulo: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(rotulo, color = cTextoS) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = campoCoresFin(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    )
}

@Composable
private fun campoCoresFin() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = cLavanda, unfocusedBorderColor = cMantle,
    focusedTextColor = cTextoP, unfocusedTextColor = cTextoP, cursorColor = cLavanda
)
