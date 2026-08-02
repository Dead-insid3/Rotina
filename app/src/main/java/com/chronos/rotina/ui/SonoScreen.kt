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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.chronos.rotina.data.ConfigSono
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.calcularHoraDormir
import com.chronos.rotina.data.configSono
import com.chronos.rotina.data.salvarConfigSono
import com.chronos.rotina.notif.SonoAgendador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SonoScreen() {
    val ctx = LocalContext.current
    val p = TemaAtivo.paleta
    val escopo = rememberCoroutineScope()

    var carregando by remember { mutableStateOf(true) }
    var ativo by remember { mutableStateOf(false) }
    var acordar by remember { mutableStateOf("08:00") }
    var horas by remember { mutableStateOf("8") }
    var antes by remember { mutableStateOf("30") }
    var aviso by remember { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val c = withContext(Dispatchers.IO) { ctx.appDb().configSono() }
        ativo = c.ativo
        acordar = c.horaAcordar
        horas = c.horasSono.toString()
        antes = c.minutosAntes.toString()
        carregando = false
    }

    fun salvar() {
        val a = normalizarHoraSono(acordar)
        val h = horas.toIntOrNull()
        val m = antes.toIntOrNull()
        when {
            a == null -> { erro = "Horário inválido (ex: 08:00)"; return }
            h == null || h !in 1..14 -> { erro = "Horas de sono entre 1 e 14"; return }
            m == null || m !in 0..180 -> { erro = "Aviso entre 0 e 180 minutos"; return }
            else -> {
                erro = null
                acordar = a
                val cfg = ConfigSono(ativo, a, h, m)
                escopo.launch {
                    withContext(Dispatchers.IO) {
                        ctx.appDb().salvarConfigSono(cfg)
                        SonoAgendador.reagendar(ctx)
                    }
                    aviso = if (ativo) "Beleza, vou te lembrar 🐾" else "Tá bom, fico quieto."
                }
            }
        }
    }

    if (carregando) {
        MachittoCarregando(texto = "Vendo seu sono…")
        return
    }

    val horaDormir = calcularHoraDormir(
        normalizarHoraSono(acordar) ?: "08:00",
        horas.toIntOrNull() ?: 8
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(p.base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Text("Qualidade de Sono", color = p.textoP, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "Nos dias sem rotina de trabalho, eu te lembro de dormir 😴",
            color = p.textoS,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TemaAtivo.raio + 6.dp))
                .background(p.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Lembrete de dormir", color = p.textoP, fontWeight = FontWeight.Medium)
                Text(
                    if (ativo) "Ligado" else "Desligado",
                    color = if (ativo) p.verde else p.textoS,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = ativo,
                onCheckedChange = { ativo = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = p.mantle, checkedTrackColor = p.principal,
                    uncheckedThumbColor = p.textoS, uncheckedTrackColor = p.mantle
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        Text("Que horas pretende acordar?", color = p.textoP, fontSize = 14.sp)
        Text(
            "Nos dias de folga, sem rotina definida",
            color = p.textoS,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        CampoSono(acordar, "ex: 08:00", p) { acordar = it }

        Spacer(Modifier.height(14.dp))

        Text("Quantas horas quer dormir?", color = p.textoP, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        CampoSono(horas, "ex: 8", p, numerico = true) { horas = it }

        Spacer(Modifier.height(14.dp))

        Text("Avisar quantos minutos antes?", color = p.textoP, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        CampoSono(antes, "ex: 30", p, numerico = true) { antes = it }

        Spacer(Modifier.height(22.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TemaAtivo.raio + 6.dp))
                .background(p.principal.copy(alpha = 0.18f))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(veuSobre(p.surface)),
                    contentAlignment = Alignment.Center
                ) { Text("🌙", fontSize = 20.sp) }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Sua hora de deitar", color = p.textoS, fontSize = 12.sp)
                    Text(horaDormir, color = p.principal, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (erro != null) {
            Spacer(Modifier.height(10.dp))
            Text(erro!!, color = p.vermelho, fontSize = 12.sp)
        }
        if (aviso != null) {
            Spacer(Modifier.height(10.dp))
            Text(aviso!!, color = p.verde, fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { salvar() },
            colors = ButtonDefaults.buttonColors(containerColor = p.principal, contentColor = corTextoSobre(p.principal)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Salvar", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(16.dp))

        Text(
            "Se a sua rotina de trabalho já tem um passo de dormir naquela noite, eu fico quieto e deixo ela mandar. Sem aviso duplicado. 🐾",
            color = p.textoS,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun CampoSono(
    valor: String,
    dica: String,
    p: PaletaMachitto,
    numerico: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(dica, color = p.textoS) },
        singleLine = true,
        keyboardOptions = if (numerico) {
            KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        } else {
            KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.principal, unfocusedBorderColor = p.surface,
            focusedTextColor = p.textoP, unfocusedTextColor = p.textoP, cursorColor = p.principal
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun normalizarHoraSono(txt: String): String? {
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
