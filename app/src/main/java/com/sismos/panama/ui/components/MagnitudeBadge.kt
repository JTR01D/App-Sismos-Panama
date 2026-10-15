package com.sismos.panama.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.ui.theme.MagGreen
import com.sismos.panama.ui.theme.MagOrange
import com.sismos.panama.ui.theme.MagRed
import com.sismos.panama.ui.theme.MagYellow
import java.util.Locale

@Composable
fun MagnitudeBadge(
    magnitude: Double,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val mainColor = when {
        magnitude >= 6.0 -> MagRed
        magnitude >= 5.0 -> MagOrange
        magnitude >= 4.0 -> MagYellow
        else -> MagGreen
    }

    val glowHaloBrush = Brush.radialGradient(
        colors = listOf(
            mainColor.copy(alpha = 0.35f),
            mainColor.copy(alpha = 0.08f),
            Color.Transparent
        )
    )

    val badgeGradient = Brush.verticalGradient(
        colors = listOf(
            mainColor,
            mainColor.copy(alpha = 0.85f)
        )
    )

    val textColor = if (magnitude in 4.0..4.9) Color(0xFF0F172A) else Color.White

    Box(
        modifier = modifier
            .size(size)
            .background(glowHaloBrush, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.88f)
                .shadow(elevation = 10.dp, shape = CircleShape, spotColor = mainColor)
                .clip(CircleShape)
                .background(badgeGradient)
                .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", magnitude),
                fontSize = if (size > 50.dp) 18.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
        }
    }
}
