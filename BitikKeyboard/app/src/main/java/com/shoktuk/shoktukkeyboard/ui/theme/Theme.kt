package com.shoktuk.shoktukkeyboard.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

// Android 13 Tonal Spot Baseline Light (approximate)
private val DefaultLightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF370B1E),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB3261E),
    onError = Color.White
)

// Android 13 Tonal Spot Baseline Dark (approximate)
private val DefaultDarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun ShoktukKeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DefaultDarkColors else DefaultLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}