package com.example.techsmart.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Por ahora la app usa solo el tema claro del diseño (sin colores dinámicos del sistema)
private val EsquemaTechsmart = lightColorScheme(
    primary = TsPrimario,
    onPrimary = Color.White,
    primaryContainer = TsAzulSuave,
    onPrimaryContainer = TsPrimarioOscuro,
    secondary = TsSecundario,
    onSecondary = Color.White,
    secondaryContainer = TsAzulSuave,
    onSecondaryContainer = TsPrimarioOscuro,
    tertiary = TsTerciario,
    onTertiary = TsSecundario,
    background = TsFondo,
    onBackground = TsSecundario,
    surface = TsSuperficie,
    onSurface = TsSecundario,
    surfaceVariant = TsSuperficieSuave,
    onSurfaceVariant = TsNeutro,
    surfaceContainerLowest = TsSuperficie,
    surfaceContainerLow = TsSuperficie,
    surfaceContainer = TsSuperficie,
    surfaceContainerHigh = TsSuperficieSuave,
    surfaceContainerHighest = TsSuperficieSuave,
    outline = TsBorde,
    outlineVariant = TsBorde,
    error = TsErrorTexto,
    errorContainer = TsErrorFondo
)

private val FormasTechsmart = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TechsmartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaTechsmart,
        typography = Typography,
        shapes = FormasTechsmart,
        content = content
    )
}
