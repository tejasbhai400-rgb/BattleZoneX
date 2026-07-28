package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BattlixColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    primaryContainer = RedContainer,
    onPrimaryContainer = RedNeon,
    secondary = RedNeon,
    onSecondary = Color.White,
    secondaryContainer = RedContainer,
    onSecondaryContainer = RedNeon,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = BlackBackground,
    onBackground = TextPrimary,
    surface = BlackSurface,
    onSurface = TextPrimary,
    surfaceVariant = BlackSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = DividerDark,
    error = RedNeon,
    onError = Color.White
)

@Composable
fun BattlixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BattlixColorScheme,
        typography = Typography,
        content = content
    )
}

