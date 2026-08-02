package com.chronos.rotina.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.rotina.R

@Composable
fun SobreScreen() {
    val p = TemaAtivo.paleta
    val raio = TemaAtivo.raio

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.base)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Image(
            painter = painterResource(R.drawable.machitto_foto),
            contentDescription = "Foto do Machitto",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(raio))
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Machitto",
            color = p.textoP,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "em memória 🤍",
            color = p.textoS,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Esse app foi desenvolvido por mim, Ricky, em homenagem ao meu gatinho que faleceu em meados de julho.",
            color = p.textoP,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Ele foi desenvolvido com um único propósito: me lembrar de dar atenção às pequenas coisas das nossas vidas. Organizando tudo para que, no final das contas, possamos aproveitar quem e o que amamos. Sem nunca nos sentirmos insuficientes quando não os tivermos mais.",
            color = p.textoP,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(raio))
                .background(p.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Machitto é apenas um Gato sem IA.",
                color = p.textoS,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Não julgue se ele for meio Burrinho. 🐾",
                color = p.textoS,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
