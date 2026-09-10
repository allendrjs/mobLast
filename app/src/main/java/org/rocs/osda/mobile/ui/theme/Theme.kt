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

// Primary/onPrimary stay constant between themes -- primary is only ever
// used as a solid-fill background (the Dashboard welcome card, chat bubbles,
// buttons) with onPrimary as the text on top of it, so it doesn't need a
// dark variant.
private val OsdaBackgroundDark = Color(0xFF121218)
private val OsdaSurfaceDark = Color(0xFF1B1B22)
private val OsdaBorderDark = Color(0xFF2E2E38)
private val OsdaHeadingDark = Color(0xFFF2F1F7)
private val OsdaMutedDark = Color(0xFFA6A6B5)
private val OsdaNavInactiveDark = Color(0xFF8B8B9C)

// The status colors below DO need dark variants, unlike primary -- they're
// used two different ways: as a chip's fg+bg pair (StatusPill), but also as
// a bare foreground color painted directly on the page's own surface
// (StatCard's colored number, e.g. "1 Pending Appeals" in amber). The light
// variants are dark, muted tones meant to read as text on a light page;
// used as-is on a dark surface they're nearly invisible. Dark mode gets a
// brighter foreground paired with a correspondingly darker, tinted
// background so the StatusPill chip keeps its own internal contrast too.
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

// Distinct from OsdaAmber so a record that's actively under appeal reads
// differently at a glance from one that's merely still PENDING -- both used
// to share OsdaAmber, which made the two statuses indistinguishable by
// color in list views (see StatusColors.forRecord).
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

// Deliberately NOT sourced from isSystemInDarkTheme() -- dark mode here is
// an explicit, app-only preference (the Appearance switch on Profile,
// backed by ThemePreferences/DataStore), independent of whatever the
// device itself is set to. OsdaMobileTheme provides this so anything
// further down the tree (like OsdaTokens.navInactive below) can read the
// *app's* dark-mode state without reaching for the system setting.
private val LocalOsdaDarkTheme = staticCompositionLocalOf { false }

/**
 * darkTheme is caller-supplied (from ThemePreferences), not derived from
 * the system. Every screen already pulls its colors from
 * MaterialTheme.colorScheme (background/surface/onBackground/
 * onSurfaceVariant/outline) rather than hardcoded hex, so switching the
 * active ColorScheme here is enough to re-theme the whole app -- no
 * per-screen changes needed.
 */
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