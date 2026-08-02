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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Base get() = TemaAtivo.paleta.base
private val Mantle get() = TemaAtivo.paleta.mantle
private val Surface0 get() = TemaAtivo.paleta.surface
private val TextoP get() = TemaAtivo.paleta.textoP
private val TextoS get() = TemaAtivo.paleta.textoS
private val Lavanda get() = TemaAtivo.paleta.principal
private val Rosa get() = TemaAtivo.paleta.secundaria
private val Amber get() = TemaAtivo.paleta.amber

@Composable
fun BoasVindasScreen(
    aoConcluir: (nome: String, genero: String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("nb") }
    var ajudaAberta by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Base)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MachittoRosto(tamanho = 180.dp)

            Spacer(Modifier.height(20.dp))

            Text(
                text = "E aí! Como você se chama?",
                color = TextoP,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Seu nome", color = TextoS) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lavanda,
                    unfocusedBorderColor = Surface0,
                    focusedTextColor = TextoP,
                    unfocusedTextColor = TextoP,
                    cursorColor = Lavanda
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Como o Machitto te chama?",
                color = TextoS,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OpcaoGenero("Homem", genero == "m", Modifier.weight(1f)) { genero = "m" }
                OpcaoGenero("Mulher", genero == "f", Modifier.weight(1f)) { genero = "f" }
                OpcaoGenero("Não-binário", genero == "nb", Modifier.weight(1f)) { genero = "nb" }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { aoConcluir(nome.trim(), genero) },
                enabled = nome.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lavanda,
                    contentColor = Mantle,
                    disabledContainerColor = Surface0,
                    disabledContentColor = TextoS
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vamos lá!", fontSize = 16.sp)
            }

            TextButton(onClick = { aoConcluir("", genero) }) {
                Text("Pular por agora", color = TextoS)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            if (ajudaAberta) {
                BalaoAjuda(aoFechar = { ajudaAberta = false })
            } else {
                Dorminhoco(aoTocar = { ajudaAberta = true })
            }
        }
    }
}

@Composable
private fun OpcaoGenero(
    texto: String,
    selecionado: Boolean,
    modifier: Modifier = Modifier,
    aoTocar: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (selecionado) Rosa else Surface0,
                RoundedCornerShape(10.dp)
            )
            .clickable { aoTocar() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = if (selecionado) Mantle else TextoS,
            fontSize = 13.sp,
            fontWeight = if (selecionado) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Dorminhoco(aoTocar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Mantle, RoundedCornerShape(14.dp))
            .clickable { aoTocar() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        MachittoRosto(tamanho = 32.dp, humor = HumorMachitto.DORMINDO)
        Spacer(Modifier.width(8.dp))
        Text("zzz…", color = TextoS, fontSize = 12.sp)
    }
}

@Composable
private fun BalaoAjuda(aoFechar: () -> Unit) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .background(Mantle, RoundedCornerShape(14.dp))
            .border(1.dp, Surface0, RoundedCornerShape(14.dp))
            .clickable { aoFechar() }
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MachittoRosto(tamanho = 36.dp)
                Spacer(Modifier.width(10.dp))
                Text("Oi! Acordei 🐾", color = Amber, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Sou o Machitto, seu gatinho assistente. Vou te ajudar a lembrar da sua rotina e das suas contas.",
                color = TextoP,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Machitto é apenas um Gato sem IA. Não julgue se ele for meio Burrinho.",
                color = TextoS,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(8.dp))
            Text("(toque para eu voltar a dormir)", color = TextoS, fontSize = 10.sp)
        }
    }
}
