package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TechPurple,
    secondary = SleekCyan,
    tertiary = SoftGreyText,
    background = ObsidianBg,
    surface = ObsidianSurface,
    onPrimary = ObsidianBg,
    onSecondary = WhitePure,
    onBackground = WhitePure,
    onSurface = WhitePure,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = SoftGreyText
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = SleekTeal,
    tertiary = DarkGreyText,
    background = LightBg,
    surface = LightSurface,
    onPrimary = WhitePure,
    onSecondary = WhitePure,
    onBackground = DarkGreyText,
    onSurface = DarkGreyText,
    surfaceVariant = LightCard,
    onSurfaceVariant = DarkGreyText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
