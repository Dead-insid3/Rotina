package com.chronos.rotina.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.data.NiveisInsistencia
import com.chronos.rotina.data.TarefaEntity
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.concluirTarefa
import com.chronos.rotina.data.nivelPorChave
import com.chronos.rotina.data.removerTarefa
import com.chronos.rotina.data.salvarTarefa
import com.chronos.rotina.data.tarefasTodas
import com.chronos.rotina.notif.CronogramaAgendador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun CronogramaScreen() {
    val ctx = LocalContext.current
    val p = TemaAtivo.paleta
    val escopo = rememberCoroutineScope()

    var tarefas by remember { mutableStateOf(listOf<TarefaEntity>()) }
    var carregando by remember { mutableStateOf(true) }
    var recarregar by remember { mutableStateOf(0) }
    var criando by remember { mutableStateOf(false) }

    fun recarregarDados() { recarregar += 1 }

    LaunchedEffect(recarregar) {
        carregando = true
        tarefas = withContext(Dispatchers.IO) { ctx.appDb().tarefasTodas() }
        carregando = false
    }

    fun sincronizar(bloco: suspend () -> Unit) {
        escopo.launch {
            withContext(Dispatchers.IO) {
                bloco()
                CronogramaAgendador.reagendarTodas(ctx)
            }
            recarregarDados()
        }
    }

    Column(Modifier.fillMaxSize().background(p.base)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Cronograma", color = p.textoP, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Suas demandas com prazo ⏳", color = p.textoS, fontSize = 13.sp)
            }
            TextButton(onClick = { criando = true }) {
                Text("+ Nova", color = p.principal, fontWeight = FontWeight.Bold)
            }
        }

        when {
            carregando -> MachittoCarregando(texto = "Vendo suas demandas…")
            tarefas.isEmpty() -> VazioCronograma(p)
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
            ) {
                items(tarefas, key = { it.id }) { t ->
                    Box(Modifier.animateItem()) {
                        LinhaTarefa(
                            tarefa = t,
                            paleta = p,
                            onConcluir = { v -> sincronizar { ctx.appDb().concluirTarefa(t, v) } },
                            onExcluir = { sincronizar { ctx.appDb().removerTarefa(t) } }
                        )
                    }
                }
            }
        }
    }

    if (criando) {
        DialogoTarefa(
            aoFechar = { criando = false },
            aoSalvar = { nova ->
                criando = false
                sincronizar { ctx.appDb().salvarTarefa(nova) }
            }
        )
    }
}

@Composable
private fun VazioCronograma(p: PaletaMachitto) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MachittoRosto(tamanho = 110.dp, humor = HumorMachitto.NORMAL)
        Spacer(Modifier.height(18.dp))
        Text("Nada na agenda", color = p.textoP, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Tem alguma demanda com prazo? Me conta que eu te cutuco até você fazer.",
            color = p.textoS,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun LinhaTarefa(
    tarefa: TarefaEntity,
    paleta: PaletaMachitto,
    onConcluir: (Boolean) -> Unit,
    onExcluir: () -> Unit
) {
    val agora = System.currentTimeMillis()
    val venceu = !tarefa.concluida && tarefa.prazoMillis <= agora
    val nivel = nivelPorChave(tarefa.insistencia)

    val corBase = when {
        tarefa.concluida -> paleta.surface.copy(alpha = 0.5f)
        venceu -> paleta.vermelho.copy(alpha = 0.18f)
        else -> paleta.surface
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(
                elevation = if (tarefa.concluida) 0.dp else 5.dp,
                shape = RoundedCornerShape(TemaAtivo.raio + 6.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(TemaAtivo.raio + 6.dp))
            .background(corBase)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = tarefa.concluida,
            onCheckedChange = { onConcluir(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = paleta.verde,
                uncheckedColor = paleta.textoS,
                checkmarkColor = paleta.mantle
            )
        )
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text(
                tarefa.titulo,
                color = if (tarefa.concluida) paleta.textoS else paleta.textoP,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                textDecoration = if (tarefa.concluida) TextDecoration.LineThrough else null
            )
            Spacer(Modifier.height(2.dp))
            Text(
                textoPrazo(tarefa.prazoMillis, tarefa.concluida, venceu),
                color = when {
                    tarefa.concluida -> paleta.textoS
                    venceu -> paleta.vermelho
                    else -> paleta.textoS
                },
                fontSize = 12.sp
            )
            if (!tarefa.concluida) {
                Text(
                    "${nivel.emoji} ${nivel.nome}",
                    color = paleta.principal,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        IconButton(onClick = onExcluir) {
            Icon(Icons.Filled.Delete, "Excluir", tint = paleta.vermelho)
        }
    }
}

private fun textoPrazo(prazoMillis: Long, concluida: Boolean, venceu: Boolean): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = prazoMillis
    val dia = cal.get(Calendar.DAY_OF_MONTH)
    val mes = cal.get(Calendar.MONTH) + 1
    val hora = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    val quando = "%02d/%02d às %02d:%02d".format(dia, mes, hora, min)

    if (concluida) return "Feito • era $quando"
    if (venceu) return "Venceu • era $quando"

    val faltamMin = ((prazoMillis - System.currentTimeMillis()) / 60_000L).toInt()
    val restante = when {
        faltamMin < 60 -> "faltam $faltamMin min"
        faltamMin < 1440 -> "faltam ${faltamMin / 60}h"
        else -> "faltam ${faltamMin / 1440} dias"
    }
    return "$quando • $restante"
}

@Composable
private fun DialogoTarefa(aoFechar: () -> Unit, aoSalvar: (TarefaEntity) -> Unit) {
    val ctx = LocalContext.current
    val p = TemaAtivo.paleta

    var titulo by remember { mutableStateOf("") }
    var detalhe by remember { mutableStateOf("") }
    var nivel by remember { mutableStateOf("presente") }
    var erro by remember { mutableStateOf<String?>(null) }

    val prazo = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    var prazoTexto by remember { mutableStateOf(formatarPrazo(prazo)) }

    fun abrirData() {
        DatePickerDialog(
            ctx,
            { _, ano, mes, dia ->
                prazo.set(Calendar.YEAR, ano)
                prazo.set(Calendar.MONTH, mes)
                prazo.set(Calendar.DAY_OF_MONTH, dia)
                prazoTexto = formatarPrazo(prazo)
            },
            prazo.get(Calendar.YEAR),
            prazo.get(Calendar.MONTH),
            prazo.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun abrirHora() {
        TimePickerDialog(
            ctx,
            { _, h, m ->
                prazo.set(Calendar.HOUR_OF_DAY, h)
                prazo.set(Calendar.MINUTE, m)
                prazoTexto = formatarPrazo(prazo)
            },
            prazo.get(Calendar.HOUR_OF_DAY),
            prazo.get(Calendar.MINUTE),
            true
        ).show()
    }

    AlertDialog(
        containerColor = p.surface,
        onDismissRequest = aoFechar,
        title = { Text("Nova demanda", color = p.textoP) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CampoTarefa("O que precisa fazer?", titulo, p) { titulo = it }
                Spacer(Modifier.height(6.dp))
                CampoTarefa("Detalhe (opcional)", detalhe, p) { detalhe = it }

                Spacer(Modifier.height(14.dp))
                Text("Até quando?", color = p.textoS, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(TemaAtivo.raio))
                            .background(p.mantle)
                            .clickable { abrirData() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(prazoTexto.first, color = p.textoP, fontSize = 13.sp) }
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(TemaAtivo.raio))
                            .background(p.mantle)
                            .clickable { abrirHora() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(prazoTexto.second, color = p.textoP, fontSize = 13.sp) }
                }

                Spacer(Modifier.height(14.dp))
                Text("O quão chato eu devo ser?", color = p.textoS, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    NiveisInsistencia.forEach { n ->
                        val sel = n.chave == nivel
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TemaAtivo.raio))
                                .background(if (sel) p.principal else p.mantle)
                                .clickable { nivel = n.chave }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(30.dp).clip(CircleShape)
                                    .background(veuSobre(if (sel) p.principal else p.mantle)),
                                contentAlignment = Alignment.Center
                            ) { Text(n.emoji, fontSize = 15.sp) }
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(
                                    n.nome,
                                    color = if (sel) corTextoSobre(p.principal) else p.textoP,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    n.descricao,
                                    color = if (sel) corLegendaSobre(p.principal) else p.textoS,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                if (erro != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(erro!!, color = p.vermelho, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        titulo.isBlank() -> erro = "Me diz o que precisa fazer"
                        prazo.timeInMillis <= System.currentTimeMillis() ->
                            erro = "Esse prazo já passou, escolhe um futuro"
                        else -> aoSalvar(
                            TarefaEntity(
                                titulo = titulo.trim(),
                                detalhe = detalhe.trim(),
                                prazoMillis = prazo.timeInMillis,
                                insistencia = nivel
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = p.principal,
                    contentColor = corTextoSobre(p.principal)
                )
            ) { Text("Criar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = p.textoS) } }
    )
}

private fun formatarPrazo(cal: Calendar): Pair<String, String> {
    val data = "%02d/%02d/%d".format(
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.YEAR)
    )
    val hora = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    return data to hora
}

@Composable
private fun CampoTarefa(rotulo: String, valor: String, p: PaletaMachitto, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(rotulo, color = p.textoS) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.principal, unfocusedBorderColor = p.mantle,
            focusedTextColor = p.textoP, unfocusedTextColor = p.textoP, cursorColor = p.principal
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
