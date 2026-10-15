package com.sismos.panama.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.PanamaRegionsCatalog
import com.sismos.panama.ui.components.GlassCard
import com.sismos.panama.ui.theme.BgDark
import com.sismos.panama.ui.theme.MagGreen
import com.sismos.panama.ui.theme.MagOrange
import com.sismos.panama.ui.theme.MagRed
import com.sismos.panama.ui.theme.MagYellow
import com.sismos.panama.ui.theme.PrimaryBlue
import com.sismos.panama.ui.viewmodel.SeismicViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val quakes = state.earthquakes

    val totalEvents = quakes.size
    val maxMag = quakes.maxOfOrNull { it.magnitude } ?: 0.0
    val avgDepth = if (quakes.isNotEmpty()) quakes.map { it.depthKm }.average() else 10.0
    val shallowCount = quakes.count { it.depthKm < 25.0 }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F141C))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Dashboard & Estadísticas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Análisis Geodinámico de Panamá",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiGlassCard(
                    title = "EVENTOS REGISTRADOS",
                    value = "$totalEvents",
                    subtitle = "En catálogo activo",
                    icon = Icons.Default.Equalizer,
                    accentColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                KpiGlassCard(
                    title = "MAGNITUD MÁXIMA",
                    value = String.format(Locale.US, "%.1f Mw", maxMag),
                    subtitle = if (maxMag >= 5.0) "Alerta Moderada" else "Actividad Regular",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = if (maxMag >= 5.0) MagOrange else MagYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiGlassCard(
                    title = "PROFUNDIDAD MEDIA",
                    value = String.format(Locale.US, "%.0f km", avgDepth),
                    subtitle = "$shallowCount someros (<25km)",
                    icon = Icons.Default.Speed,
                    accentColor = MagGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiGlassCard(
                    title = "ZONA MÁS ACTIVA",
                    value = "Burica",
                    subtitle = "8.0°N 82.8°O (ZFP)",
                    icon = Icons.Default.LocationOn,
                    accentColor = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
            }

            // Gráfica de Distribución Regional (Del Widget HTML)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONCENTRACIÓN SÍSMICA POR REGIONES",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )
                    Text(
                        text = "Distribución histórica de eventos por sistemas de fallas",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    PanamaRegionsCatalog.regions.forEach { reg ->
                        RegionBarChartRow(
                            label = reg.shortName,
                            percentage = reg.percentage,
                            fault = reg.mainFault
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Gráfica de Rangos de Magnitud en Panamá
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DESGLOSE DE MAGNITUDES ACTUAL",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val m3Below = quakes.count { it.magnitude < 3.0 }
                    val m3to4 = quakes.count { it.magnitude in 3.0..3.9 }
                    val m4to5 = quakes.count { it.magnitude in 4.0..4.9 }
                    val m5Plus = quakes.count { it.magnitude >= 5.0 }

                    MagnitudeBarRow("M < 3.0 (Microsismos)", m3Below, totalEvents, MagGreen)
                    MagnitudeBarRow("M 3.0 – 3.9 (Leves)", m3to4, totalEvents, MagYellow)
                    MagnitudeBarRow("M 4.0 – 4.9 (Moderados)", m4to5, totalEvents, MagOrange)
                    MagnitudeBarRow("M ≥ 5.0 (Fuertes)", m5Plus, totalEvents, MagRed)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun KpiGlassCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = accentColor
            )
        }
    }
}

@Composable
private fun RegionBarChartRow(
    label: String,
    percentage: Int,
    fault: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "$percentage%",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barra con efecto Liquid Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x221E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1E3A8A),
                                PrimaryBlue,
                                Color(0xFF60A5FA)
                            )
                        )
                    )
            )
        }

        Text(
            text = fault,
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun MagnitudeBarRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val pct = if (total > 0) count.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(durationMillis = 800),
        label = "magProgress"
    )

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, fontSize = 12.sp, color = Color(0xFFE2E8F0))
            }
            Text(
                text = "$count (${String.format(Locale.US, "%.0f%%", pct * 100)})",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x221E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )
        }
    }
}
