package org.rocs.osda.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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
private val OsdaBackgroundDark = Color(0xFF121218)
private val OsdaSurfaceDark = Color(0xFF1B1B22)
private val OsdaBorderDark = Color(0xFF2E2E38)
private val OsdaHeadingDark = Color(0xFFF2F1F7)
private val OsdaMutedDark = Color(0xFFA6A6B5)
private val OsdaNavInactiveDark = Color(0xFF8B8B9C)
private val OsdaAmber = Color(0xFF916515)
private val OsdaAmberBg = Color(0xFFFDF1D6)
private val OsdaAmberDark = Color(0xFFFFC65C)
private val OsdaAmberBgDark = Color(0xFF3A2E10)
private val OsdaGreen = Color(0xFF1A7945)
private val OsdaGreenBg = Color(0xFFDCF5E3)
private val OsdaGreenDark = Color(0xFF6FE3A0)
private val OsdaGreenBgDark = Color(0xFF123822)
private val OsdaRed = Color(0xFFC22B2B)
private val OsdaRedBg = Color(0xFFFBDFDF)
private val OsdaRedDark = Color(0xFFFF8A8A)
private val OsdaRedBgDark = Color(0xFF3A1414)
private val OsdaBlue = Color(0xFF2B5FC2)
private val OsdaBlueBg = Color(0xFFDCE8FB)
private val OsdaBlueDark = Color(0xFF7FAFFF)
private val OsdaBlueBgDark = Color(0xFF16233A)

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

private val DarkColors = darkColorScheme(
    primary = OsdaPrimary,
    onPrimary = OsdaSurface,
    background = OsdaBackgroundDark,
    onBackground = OsdaHeadingDark,
    surface = OsdaSurfaceDark,
    onSurface = OsdaHeadingDark,
    surfaceVariant = OsdaSurfaceDark,
    onSurfaceVariant = OsdaMutedDark,
    outline = OsdaBorderDark,
    error = OsdaRed
)

private val LocalOsdaDarkTheme = staticCompositionLocalOf { false }

@Composable
fun OsdaMobileTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOsdaDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
    }
}

object OsdaTokens {
    val cardRadius = 14.dp
    val outerRadius = 24.dp
    val pillRadius = 20.dp
    val buttonRadius = 12.dp
    val inputRadius = 10.dp

    val navInactive: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaNavInactiveDark else OsdaNavInactive
    val primaryMuted = OsdaPrimaryMuted

    val amber: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaAmberDark else OsdaAmber
    val amberBg: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaAmberBgDark else OsdaAmberBg
    val green: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaGreenDark else OsdaGreen
    val greenBg: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaGreenBgDark else OsdaGreenBg
    val red: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaRedDark else OsdaRed
    val redBg: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaRedBgDark else OsdaRedBg
    val blue: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaBlueDark else OsdaBlue
    val blueBg: Color
        @Composable get() = if (LocalOsdaDarkTheme.current) OsdaBlueBgDark else OsdaBlueBg
}