package com.chronos.rotina.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.chronos.rotina.R
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PaletaMachitto(
    val base: Color,
    val mantle: Color,
    val surface: Color,
    val textoP: Color,
    val textoS: Color,
    val principal: Color,
    val secundaria: Color,
    val verde: Color,
    val vermelho: Color,
    val amber: Color
)

enum class FonteTema { PADRAO, MONO, SERIF, CURSIVE, PIXEL, ORBITRON, CINZEL, MONOTON, BEBAS, PLAYFAIR, EXO }

data class TemaCompleto(
    val nome: String,
    val paleta: PaletaMachitto,
    val raioCanto: Dp,
    val fonte: FonteTema,
    val emoji: String = "🎨",
    val legenda: String = ""
)

val FontePixel = FontFamily(Font(R.font.pixelify_sans))
val FonteOrbitron = FontFamily(Font(R.font.orbitron))
val FonteCinzel = FontFamily(Font(R.font.cinzel))
val FonteMonoton = FontFamily(Font(R.font.monoton))
val FonteBebas = FontFamily(Font(R.font.bebas_neue))
val FontePlayfair = FontFamily(Font(R.font.playfair_display))
val FonteExo = FontFamily(Font(R.font.exo_2))

fun FonteTema.familia(): FontFamily = when (this) {
    FonteTema.PADRAO -> FontFamily.SansSerif
    FonteTema.MONO -> FontFamily.Monospace
    FonteTema.SERIF -> FontFamily.Serif
    FonteTema.CURSIVE -> FontFamily.Cursive
    FonteTema.PIXEL -> FontePixel
    FonteTema.ORBITRON -> FonteOrbitron
    FonteTema.CINZEL -> FonteCinzel
    FonteTema.MONOTON -> FonteMonoton
    FonteTema.BEBAS -> FonteBebas
    FonteTema.PLAYFAIR -> FontePlayfair
    FonteTema.EXO -> FonteExo
}

object TemaAtivo {
    var tema by mutableStateOf(TemaCatppuccin)
        private set

    val paleta: PaletaMachitto get() = tema.paleta
    val raio: Dp get() = tema.raioCanto
    val familiaFonte: FontFamily get() = tema.fonte.familia()

    fun aplicar(t: TemaCompleto) { tema = t }
    fun aplicarPorNome(nome: String) { tema = temaPorNome(nome) }
    fun aplicarPersonalizado(fundo: Color, principal: Color) {
        tema = TemaCompleto("Personalizado", paletaPersonalizada(fundo, principal), 14.dp, FonteTema.PADRAO)
    }
}

val PaletaCatppuccin = PaletaMachitto(
    base = Color(0xFF1E1E2E), mantle = Color(0xFF181825), surface = Color(0xFF313244),
    textoP = Color(0xFFCDD6F4), textoS = Color(0xFFA6ADC8),
    principal = Color(0xFFB4BEFE), secundaria = Color(0xFFF5C2E7),
    verde = Color(0xFFA6E3A1), vermelho = Color(0xFFF38BA8), amber = Color(0xFFF9E2AF)
)

private fun ajusta(fundo: Color, principal: Color, secundaria: Color) = PaletaMachitto(
    base = fundo,
    mantle = escurece(fundo, 0.18f),
    surface = clareiaOuEscurece(fundo, 0.10f),
    textoP = contrasteTexto(fundo, true),
    textoS = contrasteTexto(fundo, false),
    principal = principal, secundaria = secundaria,
    verde = Color(0xFFA6E3A1), vermelho = Color(0xFFF38BA8), amber = Color(0xFFF9E2AF)
)

val PaletaDiaClaro = PaletaMachitto(
    base = Color(0xFFF7F5F2),
    mantle = Color(0xFFFFFFFF),
    surface = Color(0xFFEBE7E1),
    textoP = Color(0xFF1F1E24),
    textoS = Color(0xFF6B6873),
    principal = Color(0xFF6C5CE7),
    secundaria = Color(0xFFE8899B),
    verde = Color(0xFF4CAF7D),
    vermelho = Color(0xFFE05B6B),
    amber = Color(0xFFE0A32E)
)

val TemaCatppuccin = TemaCompleto("Catppuccin", PaletaCatppuccin, 14.dp, FonteTema.PADRAO, "☕", "Aconchego em lavanda")

val TemasCurados: List<TemaCompleto> = listOf(
    TemaCatppuccin,
    TemaCompleto("Cyberpunk", ajusta(Color(0xFF05010D), Color(0xFFFF2A6D), Color(0xFF05D9E8)), 0.dp, FonteTema.ORBITRON, "⚡", "Neon e sombras"),
    TemaCompleto("Darkwave", ajusta(Color(0xFF120A1F), Color(0xFFB14EFF), Color(0xFF6C3FA8)), 6.dp, FonteTema.CINZEL, "🌙", "Melancolia elegante"),
    TemaCompleto("Pixel Art", ajusta(Color(0xFF0B0F0A), Color(0xFF39FF14), Color(0xFFFFB000)), 0.dp, FonteTema.PIXEL, "👾", "Verde fósforo retrô"),
    TemaCompleto("Vaporwave", ajusta(Color(0xFF2A0E4F), Color(0xFFFF6AD5), Color(0xFF05FFE1)), 24.dp, FonteTema.MONOTON, "🌴", "Sonho anos 80"),
    TemaCompleto("Brasa", ajusta(Color(0xFF160A05), Color(0xFFFF6A13), Color(0xFFFFC24B)), 14.dp, FonteTema.BEBAS, "🔥", "Calor de fogueira"),
    TemaCompleto("Ametista", ajusta(Color(0xFF120620), Color(0xFFC77DFF), Color(0xFFFF5FA2)), 12.dp, FonteTema.PLAYFAIR, "💜", "Roxo sofisticado"),
    TemaCompleto("Meia-noite", ajusta(Color(0xFF080E1A), Color(0xFF7FB2FF), Color(0xFFB8C6DC)), 8.dp, FonteTema.EXO, "🌌", "Azul silencioso"),
    TemaCompleto("Dia Claro", PaletaDiaClaro, 16.dp, FonteTema.PADRAO, "☀️", "Leve como a manhã")
)

fun corTextoSobre(fundo: Color): Color =
    if (fundo.luminance() > 0.55f) Color(0xFF141419) else Color(0xFFF5F5F7)

fun corLegendaSobre(fundo: Color): Color =
    if (fundo.luminance() > 0.55f) Color(0xFF141419).copy(alpha = 0.68f)
    else Color(0xFFF5F5F7).copy(alpha = 0.78f)

fun veuSobre(fundo: Color): Color =
    if (fundo.luminance() > 0.55f) Color.Black.copy(alpha = 0.13f)
    else Color.White.copy(alpha = 0.26f)

fun suavizar(c: Color, forca: Float = 0.16f): Color {
    val f = if (c.luminance() < 0.28f) forca * 0.45f else forca
    return Color(
        red = (c.red + (1f - c.red) * f).coerceIn(0f, 1f),
        green = (c.green + (1f - c.green) * f).coerceIn(0f, 1f),
        blue = (c.blue + (1f - c.blue) * f).coerceIn(0f, 1f),
        alpha = 1f
    )
}

fun temaPorNome(nome: String): TemaCompleto =
    TemasCurados.firstOrNull { it.nome == nome } ?: TemaCatppuccin

fun paletaPersonalizada(fundo: Color, principal: Color): PaletaMachitto =
    ajusta(fundo, principal, principal)

private fun escurece(c: Color, f: Float) = Color(
    (c.red * (1 - f)).coerceIn(0f, 1f), (c.green * (1 - f)).coerceIn(0f, 1f), (c.blue * (1 - f)).coerceIn(0f, 1f), 1f
)
private fun clareia(c: Color, f: Float) = Color(
    (c.red + (1 - c.red) * f).coerceIn(0f, 1f), (c.green + (1 - c.green) * f).coerceIn(0f, 1f), (c.blue + (1 - c.blue) * f).coerceIn(0f, 1f), 1f
)
private fun clareiaOuEscurece(c: Color, f: Float) = if (c.luminance() < 0.5f) clareia(c, f + 0.06f) else escurece(c, f)
private fun contrasteTexto(fundo: Color, forte: Boolean): Color {
    val claro = fundo.luminance() < 0.5f
    return if (claro) { if (forte) Color(0xFFECECEC) else Color(0xFFB8B8C4) }
    else { if (forte) Color(0xFF1C1C22) else Color(0xFF4A4A55) }
}
