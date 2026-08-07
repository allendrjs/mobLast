package org.rocs.osda.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val OsdaBackground = Color(0xFFF7F5F2)
private val OsdaSurface = Color(0xFFFFFFFF)
private val OsdaBorder = Color(0xFFEAE8E4)
private val OsdaPrimary = Color(0xFF14123A)
private val OsdaPrimaryMuted = Color(0xFFBFBFE5)
private val OsdaHeading = Color(0xFF1A1A2E)

private val OsdaMuted = Color(0xFF6D6D7F)
private val OsdaNavInactive = Color(0xFF6E6E7D)

private val OsdaAmber = Color(0xFF916515)
private val OsdaAmberBg = Color(0xFFFDF1D6)
private val OsdaGreen = Color(0xFF1A7945)
private val OsdaGreenBg = Color(0xFFDCF5E3)
private val OsdaRed = Color(0xFFC22B2B)
private val OsdaRedBg = Color(0xFFFBDFDF)

private val LightColors = lightColorScheme(
    primary = OsdaPrimary,
    onPrimary = OsdaSurface,
    background = OsdaBackground,
    onBackground = OsdaHeading,
    surface = OsdaSurface,
    onSurface = OsdaHeading,
    surfaceVariant = OsdaSurface,
    onSurfaceVariant = OsdaMuted,
    outline = OsdaBorder,
    error = OsdaRed
)

@Composable
fun OsdaMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}

object OsdaTokens {
    val cardRadius = 14.dp
    val outerRadius = 24.dp
    val pillRadius = 20.dp
    val buttonRadius = 12.dp
    val inputRadius = 10.dp

    val navInactive = OsdaNavInactive
    val primaryMuted = OsdaPrimaryMuted

    val amber = OsdaAmber
    val amberBg = OsdaAmberBg
    val green = OsdaGreen
    val greenBg = OsdaGreenBg
    val red = OsdaRed
    val redBg = OsdaRedBg
}