package com.sismos.panama.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.theme.TsunamiWarningColor

@Composable
fun EarthquakeCard(
    earthquake: Earthquake,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orderIndex: Int = -1
) {
    val isSwarmEvent = earthquake.latitude in 7.0..8.2 && earthquake.longitude in -82.0..-80.2
    val depthLabel = when {
        earthquake.depthKm <= 20.0 -> "Superficial (${earthquake.depthKm.toInt()} km) • Mayor fuerza local"
        earthquake.depthKm <= 50.0 -> "Intermedio (${earthquake.depthKm.toInt()} km)"
        else -> "Profundo (${earthquake.depthKm.toInt()} km) • Energía dispersa"
    }

    val citizenEffect = when {
        earthquake.magnitude >= 6.0 -> "⚠️ Severo • Peligro destructivo"
        earthquake.magnitude >= 5.0 -> "🟠 Fuerte • Sacudida notable"
        earthquake.magnitude >= 4.0 -> "🟡 Moderado • Sentido por la población"
        earthquake.magnitude >= 3.0 -> "🔵 Leve • Sentido en reposo"
        else -> "🟢 Instrumental • Sin peligro"
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila de encabezado cronológico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (orderIndex == 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33FFB300))
                                .border(0.8.dp, Color(0xFFFFB300), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ #1 MÁS RECIENTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    } else if (orderIndex > 0) {
                        Text(
                            text = "#${orderIndex + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (isSwarmEvent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22F59E0B))
                                .border(0.8.dp, Color(0x66F59E0B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Enjambre Azuero",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }
                }

                Text(
                    text = citizenEffect,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MagnitudeBadge(magnitude = earthquake.magnitude)

                    Spacer(modifier = Modifier.width(14.dp))

                    val cleanPlace = earthquake.place
                        .replace(Regex("^Localizado en Panamá a\\s*", RegexOption.IGNORE_CASE), "A ")
                        .replace(Regex("(\\d+)(km)", RegexOption.IGNORE_CASE), "$1 km")
                    val sourceTag = if (earthquake.sourceName.contains("IGC", ignoreCase = true)) "IGC-UP" else "USGS"

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanPlace,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${earthquake.getTimeAgo()} • ${earthquake.getFormattedTime()} • $sourceTag",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalles",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata con etiquetas acrílicas traslúcidas explicadas para no geólogos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AcrylicInfoChip(label = "Prof", value = depthLabel, modifier = Modifier.weight(1.1f))
                AcrylicInfoChip(
                    label = "Falla",
                    value = earthquake.nearestFault,
                    modifier = Modifier.weight(1f)
                )
            }

            if (earthquake.isTsunamiWarning) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TsunamiWarningColor.copy(alpha = 0.18f))
                        .border(1.dp, TsunamiWarningColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Aviso Tsunami",
                        tint = TsunamiWarningColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aviso preventivo generado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFCA5A5)
                    )
                }
            }
        }
    }
}

@Composable
fun AcrylicInfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141F30))
            .border(0.8.dp, Color(0x3393C5FD), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE2E8F0),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
