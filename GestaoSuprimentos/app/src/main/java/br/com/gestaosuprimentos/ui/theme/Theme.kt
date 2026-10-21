package br.com.gestaosuprimentos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cores tiradas do protótipo no Figma
val VerdeEscuro = Color(0xFF0E3B38)
val Laranja = Color(0xFFF2A93B)
val CinzaFundo = Color(0xFFEFEFEF)
val CinzaCampo = Color(0xFFDCDCDC)
val CinzaTexto = Color(0xFF6B6B6B)
val VerdeOk = Color(0xFF2E7D32)
val VermelhoErro = Color(0xFFB3261E)

private val Esquema = lightColorScheme(
    primary = VerdeEscuro,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE3E0),
    onPrimaryContainer = VerdeEscuro,
    secondary = Laranja,
    onSecondary = Color(0xFF2B1A00),
    secondaryContainer = Color(0xFFFFE3B8),
    onSecondaryContainer = Color(0xFF2B1A00),
    background = CinzaFundo,
    onBackground = Color(0xFF1B1B1B),
    surface = CinzaFundo,
    onSurface = Color(0xFF1B1B1B),
    onSurfaceVariant = CinzaTexto,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    error = VermelhoErro
)

@Composable
fun GestaoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}
