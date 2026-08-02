package com.chronos.rotina.ui

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
import com.chronos.rotina.data.TagEntity
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.removerTag
import com.chronos.rotina.data.salvarTag
import com.chronos.rotina.data.todasAsTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TagsScreen() {
    val ctx = LocalContext.current
    val p = TemaAtivo.paleta
    val raio = TemaAtivo.raio
    var tags by remember { mutableStateOf(listOf<TagEntity>()) }
    var recarregar by remember { mutableStateOf(0) }
    var editando by remember { mutableStateOf<TagEntity?>(null) }
    var criando by remember { mutableStateOf(false) }

    val escopo = rememberCoroutineScope()
    fun recarregarDados() { recarregar += 1 }

    LaunchedEffect(recarregar) {
        tags = withContext(Dispatchers.IO) { ctx.appDb().todasAsTags() }
    }

    Column(Modifier.fillMaxSize().background(p.base).padding(16.dp).padding(top = 48.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Minhas tags", color = p.textoP, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { criando = true }) { Text("+ Nova", color = p.principal) }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(tags, key = { it.tag }) { t ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(raio)).background(p.surface).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (t.emoji.isNotBlank()) t.emoji else "🏷️", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.label, color = p.textoP, fontWeight = FontWeight.Medium)
                        Text(t.categoria, color = p.textoS, fontSize = 12.sp)
                    }
                    IconButton(onClick = { editando = t }) { Icon(Icons.Filled.Edit, "Editar", tint = p.principal) }
                    IconButton(onClick = {
                        escopo.launch {
                            withContext(Dispatchers.IO) { ctx.appDb().removerTag(t) }
                            recarregarDados()
                        }
                    }) { Icon(Icons.Filled.Delete, "Excluir", tint = p.vermelho) }
                }
            }
        }
    }

    if (criando) {
        DialogoTag(null, aoFechar = { criando = false }, aoSalvar = { nova ->
            escopo.launch {
                withContext(Dispatchers.IO) { ctx.appDb().salvarTag(nova) }
                recarregarDados()
            }
            criando = false
        })
    }
    editando?.let { t ->
        DialogoTag(t, aoFechar = { editando = null }, aoSalvar = { edit ->
            escopo.launch {
                withContext(Dispatchers.IO) { ctx.appDb().salvarTag(edit) }
                recarregarDados()
            }
            editando = null
        })
    }
}

@Composable
private fun DialogoTag(tag: TagEntity?, aoFechar: () -> Unit, aoSalvar: (TagEntity) -> Unit) {
    val p = TemaAtivo.paleta
    var label by remember { mutableStateOf(tag?.label ?: "") }
    var emoji by remember { mutableStateOf(tag?.emoji ?: "") }
    var categoria by remember { mutableStateOf(tag?.categoria ?: "generico") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        containerColor = p.surface,
        onDismissRequest = aoFechar,
        title = { Text(if (tag == null) "Nova tag" else "Editar tag", color = p.textoP) },
        text = {
            Column {
                CampoDiy("Nome (ex: Academia)", label, p) { label = it }
                CampoDiy("Emoji (ex: 🏋️)", emoji, p) { emoji = it }
                CampoDiy("Categoria (ex: exercicio)", categoria, p) { categoria = it }
                if (erro != null) Text(erro!!, color = p.vermelho, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (label.isBlank()) { erro = "Dê um nome"; return@Button }
                    val chave = tag?.tag ?: ("tag_" + label.trim().lowercase().replace(" ", "_") + "_" + System.currentTimeMillis() % 100000)
                    aoSalvar(
                        TagEntity(
                            tag = chave, label = label.trim(),
                            emoji = emoji.trim(), categoria = categoria.trim().ifBlank { "generico" },
                            padrao = tag?.padrao ?: false, ativa = true
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
private fun CampoDiy(rotulo: String, valor: String, p: com.chronos.rotina.ui.PaletaMachitto, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(rotulo, color = p.textoS) }, singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.principal, unfocusedBorderColor = p.mantle,
            focusedTextColor = p.textoP, unfocusedTextColor = p.textoP, cursorColor = p.principal
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    )
}
