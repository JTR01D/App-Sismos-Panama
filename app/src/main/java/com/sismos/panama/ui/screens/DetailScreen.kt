package com.sismos.panama.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.components.GlassCard
import com.sismos.panama.ui.components.MagnitudeBadge
import com.sismos.panama.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

private data class CityArrival(
    val name: String,
    val distanceKm: Double,
    val timeP: Double,
    val timeS: Double,
    val relativeOffset: Offset
)

private fun calcDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@Composable
fun DetailScreen(
    earthquake: Earthquake,
    onBack: () -> Unit,
    onOpenMap: () -> Unit = {}
) {
    val context = LocalContext.current

    // Estado del simulador de ondas
    var isSimulating by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0f) }

    // Ciudades clave y distancias calculadas
    val cities = remember(earthquake) {
        listOf(
            "David" to Pair(8.427, -82.431),
            "Santiago" to Pair(8.100, -80.983),
            "Chitré" to Pair(7.962, -80.428),
            "Penonomé" to Pair(8.519, -80.357),
            "Ciudad de Panamá" to Pair(8.982, -79.520),
            "Colón" to Pair(9.359, -79.901)
        ).map { (name, coords) ->
            val dist = calcDistance(earthquake.latitude, earthquake.longitude, coords.first, coords.second)
            val dLon = coords.second - earthquake.longitude
            val dLat = coords.first - earthquake.latitude
            CityArrival(
                name = name,
                distanceKm = dist,
                timeP = dist / 6.0,
                timeS = dist / 3.5,
                relativeOffset = Offset(dLon.toFloat(), -dLat.toFloat())
            )
        }.sortedBy { it.distanceKm }
    }

    LaunchedEffect(isSimulating) {
        if (isSimulating) {
            elapsedSeconds = 0f
            while (isSimulating) {
                delay(60)
                elapsedSeconds += 0.3f
                if (elapsedSeconds > 80f) {
                    elapsedSeconds = 0f // Bucle interactivo
                }
            }
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131B2A))
                    .statusBarsPadding()
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

            // SIMULADOR DE PROPAGACIÓN DE ONDAS EN EL DETALLE
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SIMULADOR DE PROPAGACIÓN DE ONDAS",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                            Text(
                                text = if (isSimulating) "Propagación en tiempo real (${elapsedSeconds.toInt()}s)" else "Simulación de ondas P y S",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { isSimulating = !isSimulating },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSimulating) Color(0xFFFF5252) else Color(0x333186FF)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSimulating) "Detener" else "Simular")
                        }
                    }

                    // Radar de propagación interactivo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF070B13))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = min(size.width, size.height) / 2.1f
                            val pxPerKm = maxRadius / 250f // 250 km de radio visible

                            // 1. Anillos de distancia de referencia (50, 100, 150, 200 km)
                            listOf(50f, 100f, 150f, 200f).forEach { rKm ->
                                val rPx = rKm * pxPerKm
                                drawCircle(
                                    color = Color(0x18FFFFFF),
                                    radius = rPx,
                                    center = center,
                                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                                )
                            }

                            // 2. Ondas en expansión
                            if (isSimulating && elapsedSeconds > 0) {
                                val distP_km = elapsedSeconds * 6.0f
                                val distS_km = elapsedSeconds * 3.5f

                                val radP_px = distP_km * pxPerKm
                                val radS_px = distS_km * pxPerKm

                                // Onda P (Cyan / Compresiva)
                                drawCircle(
                                    color = Color(0x3300E5FF),
                                    radius = radP_px,
                                    center = center,
                                    style = Stroke(width = 3.5f)
                                )
                                drawCircle(
                                    color = Color(0x1100E5FF),
                                    radius = radP_px,
                                    center = center
                                )

                                // Onda S (Roja / Destructiva)
                                drawCircle(
                                    color = Color(0x88FF5252),
                                    radius = radS_px,
                                    center = center,
                                    style = Stroke(width = 4.5f)
                                )
                                drawCircle(
                                    color = Color(0x22FF5252),
                                    radius = radS_px,
                                    center = center
                                )
                            }

                            // 3. Ciudades proyectadas relativamente
                            cities.forEach { c ->
                                val factor = pxPerKm * (c.distanceKm.toFloat() / sqrt(c.relativeOffset.x.pow(2) + c.relativeOffset.y.pow(2)).coerceAtLeast(0.01f))
                                val cPos = Offset(
                                    center.x + c.relativeOffset.x * factor * 0.4f,
                                    center.y + c.relativeOffset.y * factor * 0.4f
                                )
                                val reachedByS = isSimulating && (elapsedSeconds * 3.5f >= c.distanceKm)
                                val reachedByP = isSimulating && (elapsedSeconds * 6.0f >= c.distanceKm)

                                val dotColor = when {
                                    reachedByS -> Color(0xFFFF5252)
                                    reachedByP -> Color(0xFFFFEB3B)
                                    else -> Color(0xFF64748B)
                                }

                                drawCircle(
                                    color = dotColor,
                                    radius = 4.5f,
                                    center = cPos
                                )
                            }

                            // 4. Epicentro (Punto de Ruptura)
                            drawCircle(
                                color = Color(0xFFFF5252),
                                radius = 7f,
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 14f,
                                center = center,
                                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                            )
                        }

                        // Leyenda de Ondas en esquina inferior
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC0D1320))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Onda P (~6 km/s)", fontSize = 10.sp, color = Color(0xFF93C5FD))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFF5252)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Onda S (~3.5 km/s)", fontSize = 10.sp, color = Color(0xFFFCA5A5))
                            }
                        }
                    }

                    // Tiempos estimados de arribo a ciudades
                    Text(
                        text = "TIEMPOS DE ARRIBO ESTIMADOS A CIUDADES",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        cities.take(4).forEach { city ->
                            val reachedP = isSimulating && (elapsedSeconds >= city.timeP)
                            val reachedS = isSimulating && (elapsedSeconds >= city.timeS)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x15FFFFFF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = city.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Distancia: ${city.distanceKm.toInt()} km",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "P: ${city.timeP.toInt()}s • S: ${city.timeS.toInt()}s",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF60A5FA)
                                    )

                                    val statusText = when {
                                        reachedS -> "⚠️ Onda S arribó"
                                        reachedP -> "⚡ Onda P arribó"
                                        isSimulating -> "${(city.timeP - elapsedSeconds).toInt()}s"
                                        else -> "En reposo"
                                    }
                                    val statusColor = when {
                                        reachedS -> Color(0xFFFF5252)
                                        reachedP -> Color(0xFFFFEB3B)
                                        else -> Color(0xFF64748B)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusColor.copy(alpha = 0.2f))
                                            .border(0.8.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botón para ver en el mapa nacional completo
                    OutlinedButton(
                        onClick = onOpenMap,
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x553186FF))
                    ) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver en Mapa Tectónico Nacional", color = Color(0xFF00E5FF), fontSize = 12.sp)
                    }
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
                    DetailRow("Coordenadas Epicentro", "${String.format(java.util.Locale.US, "%.3f", earthquake.latitude)}° N, ${String.format(java.util.Locale.US, "%.3f", earthquake.longitude)}° O")
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

            Spacer(modifier = Modifier.height(36.dp))
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
