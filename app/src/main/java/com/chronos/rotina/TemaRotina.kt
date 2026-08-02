package com.chronos.rotina

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VermelhoFoco = Color(0xFFE11D2E)
private val VermelhoQuenteClaro = Color(0xFFC70F1F)
private val VermelhoQuenteEscuro = Color(0xFFFF3B4E)

private val EsquemaEscuro = darkColorScheme(
    primary = VermelhoFoco,
    onPrimary = Color(0xFFFFFFFF),
    secondary = VermelhoQuenteEscuro,
    background = Color(0xFF0A0507),
    onBackground = Color(0xFFF3E9EA),
    surface = Color(0xFF140A0D),
    onSurface = Color(0xFFF3E9EA),
    surfaceVariant = Color(0xFF1F1215),
    onSurfaceVariant = Color(0xFFA78E91),
    outline = Color(0xFF3A2A2E),
    error = VermelhoQuenteEscuro,
)

private val EsquemaClaro = lightColorScheme(
    primary = VermelhoFoco,
    onPrimary = Color(0xFFFFFFFF),
    secondary = VermelhoQuenteClaro,
    background = Color(0xFFF7F2F3),
    onBackground = Color(0xFF1A1114),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1114),
    surfaceVariant = Color(0xFFEDE4E6),
    onSurfaceVariant = Color(0xFF6B575B),
    outline = Color(0xFFD9C8CC),
    error = VermelhoQuenteClaro,
)

@Composable
fun TemaRotina(
    escuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (escuro) EsquemaEscuro else EsquemaClaro,
        content = content
    )
}
