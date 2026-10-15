package com.sismos.panama.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    primaryContainer = PrimaryContainer,
    background = BgDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkCard,
    onPrimary = OnSurface,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = StrokeDark
)

@Composable
fun SismosPanamaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
