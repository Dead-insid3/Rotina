package com.chronos.rotina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AjudaScreen() {
    val p = TemaAtivo.paleta
    val raio = TemaAtivo.raio

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.base)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(top = 40.dp)
    ) {
        Text("Como usar o Machitto", color = p.textoP, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Um guia rapidinho de cada parte 🐾", color = p.textoS, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))

        CartaoAjuda(
            "📅  Escala",
            "Marque no calendário os dias em que você trabalha. O Machitto só te lembra da rotina nesses dias. Toque num dia pra marcar/desmarcar e salve o mês.",
            p.surface, p.textoP, p.textoS, raio
        )
        CartaoAjuda(
            "✅  Rotina",
            "Na primeira vez, responda umas perguntas (horário de entrada, saída, sono...) e o Machitto monta sua rotina calculada. Depois é só ajustar: toque num passo pra mudar o horário, ligue o sino pra virar alarme, escolha a prioridade, adicione ou remova passos.",
            p.surface, p.textoP, p.textoS, raio
        )
        CartaoAjuda(
            "🔔  Notificação × Alarme",
            "Cada passo pode ser só uma notificação gentil (aparece no topo) ou um alarme de verdade (tela cheia, som e vibração até você desligar). Use o alarme pro que não pode perder, tipo acordar.",
            p.surface, p.textoP, p.textoS, raio
        )
        CartaoAjuda(
            "💰  Finanças",
            "Cadastre suas contas (recorrentes, parceladas ou pontuais) e rendas (fixa, pontual ou freela). O Machitto calcula quanto sobra no mês e te lembra das contas 3 dias antes de vencer. Você escolhe se ele cobra por vencimento ou só quando marca como paga.",
            p.surface, p.textoP, p.textoS, raio
        )
        CartaoAjuda(
            "🎨  Temas",
            "Deixe o Machitto com a sua cara. Escolha um tema pronto ou monte o seu com cor de fundo e principal. A carinha dele nunca muda 🐾",
            p.surface, p.textoP, p.textoS, raio
        )
        CartaoAjuda(
            "🐈‍⬛  O gatinho no canto",
            "É o Machitto tirando um cochilo. Toque nele pra abrir este guia ou conhecer a história dele. Segure e arraste pra tirar da frente. Ele volta a dormir sozinho depois de um tempo.",
            p.surface, p.textoP, p.textoS, raio
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CartaoAjuda(
    titulo: String, texto: String,
    fundo: androidx.compose.ui.graphics.Color,
    corTitulo: androidx.compose.ui.graphics.Color,
    corTexto: androidx.compose.ui.graphics.Color,
    raio: androidx.compose.ui.unit.Dp
) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(raio)).background(fundo).padding(16.dp)
    ) {
        Text(titulo, color = corTitulo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(texto, color = corTexto, fontSize = 14.sp, lineHeight = 20.sp)
    }
}
