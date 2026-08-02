package com.chronos.rotina.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MachittoTemaWrapper(content: @Composable () -> Unit) {
    val fonte = TemaAtivo.familiaFonte
    val estiloBase = LocalTextStyle.current.copy(fontFamily = fonte)
    CompositionLocalProvider(LocalTextStyle provides estiloBase) {
        content()
    }
}
