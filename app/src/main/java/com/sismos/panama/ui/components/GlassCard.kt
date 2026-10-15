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
 * Base sólida oscura de alta fidelidad, gradiente de brillo superior,
 * y borde biselado reflectante sin artefactos de renderizado.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color = Color(0xFF3186FF),
    content: @Composable BoxScope.() -> Unit
) {
    val glassShape = RoundedCornerShape(cornerRadius)

    val glassBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF162133), // Superficie azul noche suave y reflectante
            Color(0xFF111827), // Transición ahumada
            Color(0xFF0D1320)  // Base profunda de alto contraste
        )
    )

    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x6693C5FD), // Reflejo de luz en el borde superior (cristal pulido)
            Color(0x223B82F6),
            Color(0x101E3A8A),
            Color(0x333186FF)  // Sutil halo azul inferior
        )
    )

    Box(
        modifier = modifier
            .clip(glassShape)
            .background(glassBackgroundBrush)
            .border(borderWidth, glassBorderBrush, glassShape)
    ) {
        content()
    }
}
