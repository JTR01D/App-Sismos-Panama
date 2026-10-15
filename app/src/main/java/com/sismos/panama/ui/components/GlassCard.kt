package com.sismos.panama.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contenedor estilizado con efecto Liquid Glass / Acrylic moderno:
 * Gradiente traslúcido, borde sutil reflectante y sombra suave.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color = Color(0xFF3186FF).copy(alpha = 0.08f),
    content: @Composable BoxScope.() -> Unit
) {
    val glassShape = RoundedCornerShape(cornerRadius)

    val glassBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x2E253A5A), // Superficie acrílica traslúcida superior
            Color(0x1C121D2F), // Base ahumada oscura
            Color(0x280B1320)
        )
    )

    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x55FFFFFF), // Reflejo de luz en el borde superior
            Color(0x18FFFFFF),
            Color(0x0A3186FF),
            Color(0x283186FF)  // Sutil halo azul inferior
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = glassShape,
                ambientColor = glowColor,
                spotColor = Color(0xFF3186FF)
            )
            .clip(glassShape)
            .background(glassBackgroundBrush)
            .border(borderWidth, glassBorderBrush, glassShape)
            .padding(1.dp)
    ) {
        content()
    }
}
