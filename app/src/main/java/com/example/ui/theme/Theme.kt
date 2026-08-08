package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassDarkColorScheme = darkColorScheme(
    primary = iOSBlue,
    secondary = iOSPurple,
    tertiary = iOSPink,
    background = GlassDarkBackground,
    surface = GlassDarkSurface,
    surfaceContainer = GlassDarkCard,
    outline = GlassDarkBorder,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF0F6FC),
    onSurface = Color(0xFFF0F6FC),
    onSurfaceVariant = Color(0xFF9AA2AC)
)

private val GlassLightColorScheme = lightColorScheme(
    primary = iOSBlue,
    secondary = iOSPurple,
    tertiary = iOSPink,
    background = GlassLightBackground,
    surface = GlassLightSurface,
    surfaceContainer = GlassLightCard,
    outline = GlassLightBorder,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1E21),
    onSurface = Color(0xFF1C1E21),
    onSurfaceVariant = Color(0xFF656D76)
)

@Composable
fun SaveFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GlassDarkColorScheme else GlassLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ReelsSaverTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    SaveFlowTheme(darkTheme = darkTheme, content = content)
}

