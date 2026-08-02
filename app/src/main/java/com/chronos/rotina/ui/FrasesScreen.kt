package com.chronos.rotina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.data.FraseEntity
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.removerFrase
import com.chronos.rotina.data.salvarFrase
import com.chronos.rotina.data.todasAsFrases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FrasesScreen() {
    val ctx = LocalContext.current
    val p = TemaAtivo.paleta
    val raio = TemaAtivo.raio
    var frases by remember { mutableStateOf(listOf<FraseEntity>()) }
    var recarregar by remember { mutableStateOf(0) }
    var editando by remember { mutableStateOf<FraseEntity?>(null) }
    var criando by remember { mutableStateOf(false) }

    val escopo = rememberCoroutineScope()
    fun recarregarDados() { recarregar += 1 }

    LaunchedEffect(recarregar) {
        frases = withContext(Dispatchers.IO) { ctx.appDb().todasAsFrases() }
    }

    Column(Modifier.fillMaxSize().background(p.base).padding(16.dp).padding(top = 48.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Falas do Machitto", color = p.textoP, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { criando = true }) { Text("+ Nova", color = p.principal) }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(frases, key = { it.id }) { fr ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(raio)).background(p.surface).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(fr.categoria, color = p.principal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(fr.textoN.ifBlank { fr.textoM }, color = p.textoP, fontSize = 13.sp)
                    }
                    IconButton(onClick = { editando = fr }) { Icon(Icons.Filled.Edit, "Editar", tint = p.principal) }
                    IconButton(onClick = {
                        escopo.launch {
                            withContext(Dispatchers.IO) { ctx.appDb().removerFrase(fr) }
                            recarregarDados()
                        }
                    }) { Icon(Icons.Filled.Delete, "Excluir", tint = p.vermelho) }
                }
            }
        }
    }

    if (criando) {
        DialogoFrase(null, aoFechar = { criando = false }, aoSalvar = { nova ->
            escopo.launch {
                withContext(Dispatchers.IO) { ctx.appDb().salvarFrase(nova) }
                recarregarDados()
            }
            criando = false
        })
    }
    editando?.let { fr ->
        DialogoFrase(fr, aoFechar = { editando = null }, aoSalvar = { edit ->
            escopo.launch {
                withContext(Dispatchers.IO) { ctx.appDb().salvarFrase(edit) }
                recarregarDados()
            }
            editando = null
        })
    }
}

@Composable
private fun DialogoFrase(frase: FraseEntity?, aoFechar: () -> Unit, aoSalvar: (FraseEntity) -> Unit) {
    val p = TemaAtivo.paleta
    var categoria by remember { mutableStateOf(frase?.categoria ?: "generico") }
    var textoM by remember { mutableStateOf(frase?.textoM ?: "") }
    var textoF by remember { mutableStateOf(frase?.textoF ?: "") }
    var textoN by remember { mutableStateOf(frase?.textoN ?: "") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        containerColor = p.surface,
        onDismissRequest = aoFechar,
        title = { Text(if (frase == null) "Nova fala" else "Editar fala", color = p.textoP) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CampoFrase("Categoria (ex: acordar)", categoria, p) { categoria = it }
                CampoFrase("Fala (masculino)", textoM, p) { textoM = it }
                CampoFrase("Fala (feminino)", textoF, p) { textoF = it }
                CampoFrase("Fala (neutro)", textoN, p) { textoN = it }
                Text("Dica: use {nome} e {detalhe} que o Machitto troca sozinho.", color = p.textoS, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                if (erro != null) Text(erro!!, color = p.vermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoria.isBlank() || (textoM.isBlank() && textoF.isBlank() && textoN.isBlank())) {
                        erro = "Categoria e ao menos uma fala"; return@Button
                    }
                    val m = textoM.ifBlank { textoN.ifBlank { textoF } }
                    val fem = textoF.ifBlank { textoN.ifBlank { textoM } }
                    val neu = textoN.ifBlank { textoM.ifBlank { textoF } }
                    aoSalvar(
                        FraseEntity(
                            id = frase?.id ?: 0L,
                            categoria = categoria.trim(),
                            textoM = m, textoF = fem, textoN = neu,
                            padrao = frase?.padrao ?: false, ativa = true
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = p.principal, contentColor = p.mantle)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar", color = p.textoS) } }
    )
}

@Composable
private fun CampoFrase(rotulo: String, valor: String, p: com.chronos.rotina.ui.PaletaMachitto, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(rotulo, color = p.textoS) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.principal, unfocusedBorderColor = p.mantle,
            focusedTextColor = p.textoP, unfocusedTextColor = p.textoP, cursorColor = p.principal
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    )
}
