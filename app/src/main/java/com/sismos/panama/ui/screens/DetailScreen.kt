package com.sismos.panama.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.components.GlassCard
import com.sismos.panama.ui.components.MagnitudeBadge
import com.sismos.panama.ui.theme.BgDark
import com.sismos.panama.ui.theme.PrimaryBlue
import com.sismos.panama.ui.theme.StrokeDark
import com.sismos.panama.ui.theme.SurfaceDark
import com.sismos.panama.ui.theme.TsunamiWarningColor

@Composable
fun DetailScreen(
    earthquake: Earthquake,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
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
                Text(
                    text = "Detalle del Evento Sísmico",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
            // Header Magnitud y Lugar en GlassCard
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MagnitudeBadge(magnitude = earthquake.magnitude, size = 68.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Magnitud ${earthquake.magnitude} Mw",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Nivel: ${earthquake.alertLevel.label}",
                                fontSize = 14.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${earthquake.getTimeAgo()} • ${earthquake.sourceName}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = earthquake.place,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            if (earthquake.isTsunamiWarning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TsunamiWarningColor.copy(alpha = 0.2f))
                        .border(1.dp, TsunamiWarningColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Tsunami",
                        tint = TsunamiWarningColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Aviso marítimo preventivo generado para costas del litoral.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // Parámetros Físicos en GlassCard
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PARÁMETROS FÍSICOS Y LOCALIZACIÓN",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )

                    DetailRow("Fecha y Hora (Panamá)", earthquake.getFormattedTime())
                    DetailRow("Profundidad Hipocentral", "${earthquake.depthKm.toInt()} km (${if (earthquake.depthKm < 25) "Somero" else "Intermedio"})")
                    DetailRow("Coordenadas Epicentro", "${String.format("%.3f", earthquake.latitude)}° N, ${String.format("%.3f", earthquake.longitude)}° O")
                    DetailRow("Ciudad más Cercana", "${earthquake.nearestCity} (~${earthquake.distanceToNearestCityKm} km)")
                    DetailRow("Fuente de Registro", earthquake.sourceName)
                }
            }

            // Geodinámica y Fallas en GlassCard
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CONTEXTO GEODINÁMICO EN PANAMÁ",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )

                    DetailRow("Zona Sismotectónica", earthquake.panamaRegion)
                    DetailRow("Falla Activa Cercana", earthquake.nearestFault)
                    DetailRow("Mecanismo Focal", earthquake.faultMechanism)
                }
            }

            // Acciones: Ver en Navegador / Compartir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(earthquake.officialUrl))
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Oficial")
                }

                OutlinedButton(
                    onClick = {
                        val shareText = "Sismo en Panamá: M${earthquake.magnitude} en ${earthquake.place} a ${earthquake.getFormattedTime()}. Fuente: Red Sísmica Panamá"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir sismo"))
                    },
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StrokeDark)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
