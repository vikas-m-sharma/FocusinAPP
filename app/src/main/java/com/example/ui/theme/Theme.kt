package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MutedGoldDark,             // #C2A36B - Classic Muted Gold accent
    onPrimary = BackgroundDark,          // #101722
    primaryContainer = ElevatedCardDark, // #24324A
    onPrimaryContainer = TextPrimaryDark,// #F7F5EF
    secondary = SuccessDark,             // #5B9B78
    onSecondary = BackgroundDark,        // #101722
    secondaryContainer = SurfaceDark,    // #172033
    onSecondaryContainer = MutedGoldDark,// #C2A36B
    tertiary = MutedGoldDark,            // #C2A36B
    onTertiary = BackgroundDark,
    background = BackgroundDark,         // #101722
    onBackground = TextPrimaryDark,      // #F7F5EF
    surface = SurfaceDark,               // #172033
    onSurface = TextPrimaryDark,         // #F7F5EF
    surfaceVariant = CardDark,           // #1D2939
    onSurfaceVariant = TextSecondaryDark,// #B8C0CC
    outline = BorderDark,                // #344054
    error = ErrorDark,                   // #D06B6B
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,                  // #172033
    onPrimary = TextPrimaryDark,         // #F7F5EF
    primaryContainer = SecondaryNavy,    // #24324A
    onPrimaryContainer = TextPrimaryDark,// #F7F5EF
    secondary = MutedGold,               // #B08D57
    onSecondary = Color.White,
    secondaryContainer = WarmIvory,      // #F3F0E7
    onSecondaryContainer = DarkGold,     // #8F7042
    tertiary = DarkGold,                 // #8F7042
    onTertiary = Color.White,
    background = SurfaceLight,           // #F7F5EF
    onBackground = TextPrimaryLight,     // #172033
    surface = CardLight,                 // #FFFFFF
    onSurface = TextPrimaryLight,        // #172033
    surfaceVariant = WarmIvory,          // #F3F0E7
    onSurfaceVariant = TextSecondaryLight,// #667085
    outline = BorderLight,               // #D9D5CB
    error = ErrorLight,                  // #B54747
    onError = Color.White
)

/**
 * FOCUSIN Classic UI Color System
 * Provides centralized semantic access to academic, calm, high-focus tokens.
 */
object FocusinColors {
    // Light
    val PrimaryNavy = DeepNavy
    val NavySecondary = SecondaryNavy
    val CanvasLight = SurfaceLight
    val CardSurfaceLight = CardLight
    val Ivory = WarmIvory
    val GoldAccent = MutedGold
    val GoldDark = DarkGold

    // Dark
    val CanvasDark = BackgroundDark
    val SurfaceDarkNavy = SurfaceDark
    val CardDarkNavy = CardDark
    val ElevatedDarkNavy = ElevatedCardDark
    val GoldAccentDark = MutedGoldDark

    // Status
    val Success = SuccessDark
    val Warning = WarningDark
    val Error = ErrorDark
    val Disabled = DisabledGray
}

@Composable
fun FocusinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FocusinTheme(darkTheme = darkTheme, content = content)
}


