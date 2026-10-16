package com.sismos.panama.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.sismos.panama.data.models.Earthquake
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

enum class TimePeriod(val label: String, val durationMillis: Long?) {
    ALL("Todo el Catálogo", null),
    LAST_24H("Últimas 24 Horas", 24 * 3600 * 1000L),
    LAST_7D("Últimos 7 Días", 7 * 24 * 3600 * 1000L),
    LAST_30D("Últimos 30 Días", 30 * 24 * 3600 * 1000L)
}

enum class RegionMode(val label: String) {
    LIVE("📊 En Vivo (Registros Actuales)"),
    HISTORIC("🏛️ Referencia Histórica (IGC)")
}

@Composable
fun DashboardScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit,
    onEarthquakeSelected: (Earthquake) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val allQuakes = state.earthquakes

    var selectedPeriod by remember { mutableStateOf(TimePeriod.ALL) }
    var selectedRegionMode by remember { mutableStateOf(RegionMode.LIVE) }
    var expandedMagCategory by remember { mutableStateOf<String?>(null) }
    var expandedRegionName by remember { mutableStateOf<String?>(null) }

    // Filtrar sismos según período seleccionado para estadísticas interactivas
    val now = System.currentTimeMillis()
    val quakes = remember(allQuakes, selectedPeriod) {
        val duration = selectedPeriod.durationMillis
        if (duration == null) {
            allQuakes
        } else {
            allQuakes.filter { (now - it.timeMillis) <= duration }
        }
    }

    val totalEvents = quakes.size
    val maxMag = quakes.maxOfOrNull { it.magnitude } ?: 0.0
    val avgDepth = if (quakes.isNotEmpty()) quakes.map { it.depthKm }.average() else 10.0
    val shallowCount = quakes.count { it.depthKm < 20.0 }
    val intermediateCount = quakes.count { it.depthKm in 20.0..70.0 }
    val deepCount = quakes.count { it.depthKm > 70.0 }

    // Zona más activa calculada dinámicamente en tiempo real
    val activeZoneData = remember(quakes) {
        if (quakes.isEmpty()) {
            Pair("Sin Datos", "0 eventos registrados")
        } else {
            val azueroCount = quakes.count { isAzueroRegion(it) }
            val chiriquiCount = quakes.count { isChiriquiRegion(it) }
            val bocasCount = quakes.count { isBocasRegion(it) }
            val canalCount = quakes.count { isCanalRegion(it) }
            val darienCount = quakes.count { isDarienRegion(it) }

            val counts = listOf(
                "Azuero / Coiba" to azueroCount,
                "Chiriquí / Burica" to chiriquiCount,
                "Bocas del Toro" to bocasCount,
                "Canal / Metro" to canalCount,
                "Darién / S. Miguel" to darienCount
            )
            val maxZone = counts.maxByOrNull { it.second }
            if (maxZone != null && maxZone.second > 0) {
                Pair(maxZone.first, "${maxZone.second} eventos (${String.format(Locale.US, "%.0f%%", (maxZone.second.toFloat() / quakes.size) * 100)})")
            } else {
                Pair("General Panamá", "$totalEvents eventos distribuidos")
            }
        }
    }

    // Detección en vivo de enjambre sísmico
    val hasActiveSwarm = quakes.count { isAzueroRegion(it) } >= 10

    Scaffold(
        containerColor = BgDark,
        topBar = {
            val topBarBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xCC0E1726), // 80% opacity dark slate blue
                    Color(0xD90A0F1D)  // 85% opacity deep navy
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarBrush)
                    .statusBarsPadding()
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x5538BDF8))),
                        shape = androidx.compose.ui.graphics.RectangleShape
                    )
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dashboard & Estadísticas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Análisis Geodinámico e Interactivo de Panamá",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                // Badge En Vivo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x2210B981))
                        .border(1.dp, Color(0x6610B981), RoundedCornerShape(99.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EN VIVO",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selector interactivo de período de tiempo
            Column {
                Text(
                    text = "FILTRAR ESTADÍSTICAS POR TIEMPO",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF93C5FD)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimePeriod.values().forEach { period ->
                        val isSelected = selectedPeriod == period
                        val countInPeriod = if (period.durationMillis == null) {
                            allQuakes.size
                        } else {
                            allQuakes.count { (now - it.timeMillis) <= period.durationMillis }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = period },
                            label = {
                                Text(
                                    text = "${period.label} ($countInPeriod)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFCBD5E1)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF60A5FA) else Color(0x3364748B),
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(99.dp)
                        )
                    }
                }
            }

            // 2. Semáforo y Diagnóstico Geodinámico Ciudadano
            CitizenGeodynamicCard(
                hasActiveSwarm = hasActiveSwarm,
                maxMag = maxMag,
                totalQuakes = totalEvents
            )

            // 3. Tarjetas KPI Reactivas (Adaptable: 4 columnas en Tablet/Pantalla ancha, 2x2 en Teléfono)
            val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
            val isTablet = screenWidth >= 600

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiGlassCard(
                        title = "EVENTOS EN PERÍODO",
                        value = "$totalEvents",
                        subtitle = "Sismos registrados",
                        icon = Icons.Default.Equalizer,
                        accentColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    KpiGlassCard(
                        title = "MAGNITUD MÁXIMA",
                        value = String.format(Locale.US, "%.1f Mw", maxMag),
                        subtitle = when {
                            maxMag >= 5.5 -> "Alerta Alta"
                            maxMag >= 4.5 -> "Moderado Fuerte"
                            maxMag >= 3.5 -> "Sacudida Ligera"
                            else -> "Sismicidad Habitual"
                        },
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = when {
                            maxMag >= 5.0 -> MagRed
                            maxMag >= 4.0 -> MagOrange
                            else -> MagYellow
                        },
                        modifier = Modifier.weight(1f)
                    )
                    KpiGlassCard(
                        title = "PROFUNDIDAD MEDIA",
                        value = String.format(Locale.US, "%.0f km", avgDepth),
                        subtitle = "$shallowCount superficiales (<20km)",
                        icon = Icons.Default.Speed,
                        accentColor = MagGreen,
                        modifier = Modifier.weight(1f)
                    )
                    KpiGlassCard(
                        title = "ZONA MÁS ACTIVA",
                        value = activeZoneData.first,
                        subtitle = activeZoneData.second,
                        icon = Icons.Default.LocationOn,
                        accentColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiGlassCard(
                        title = "EVENTOS EN PERÍODO",
                        value = "$totalEvents",
                        subtitle = "Sismos registrados",
                        icon = Icons.Default.Equalizer,
                        accentColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    KpiGlassCard(
                        title = "MAGNITUD MÁXIMA",
                        value = String.format(Locale.US, "%.1f Mw", maxMag),
                        subtitle = when {
                            maxMag >= 5.5 -> "Alerta Alta"
                            maxMag >= 4.5 -> "Moderado Fuerte"
                            maxMag >= 3.5 -> "Sacudida Ligera"
                            else -> "Sismicidad Habitual"
                        },
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = when {
                            maxMag >= 5.0 -> MagRed
                            maxMag >= 4.0 -> MagOrange
                            else -> MagYellow
                        },
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
                        subtitle = "$shallowCount superficiales (<20km)",
                        icon = Icons.Default.Speed,
                        accentColor = MagGreen,
                        modifier = Modifier.weight(1f)
                    )
                    KpiGlassCard(
                        title = "ZONA MÁS ACTIVA",
                        value = activeZoneData.first,
                        subtitle = activeZoneData.second,
                        icon = Icons.Default.LocationOn,
                        accentColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Concentración Sísmica por Regiones (Modos En Vivo e Histórico)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONCENTRACIÓN SÍSMICA POR REGIONES",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD)
                            )
                            Text(
                                text = if (selectedRegionMode == RegionMode.LIVE)
                                    "Cálculo interactivo en tiempo real con los sismos registrados"
                                else
                                    "Promedio geológico e histórico catalogado por el IGC",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selector de Modo (En Vivo vs Histórico)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RegionMode.values().forEach { mode ->
                            val isSelected = selectedRegionMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                    .clickable { selectedRegionMode = mode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedRegionMode == RegionMode.LIVE) {
                        // Conteo dinámico y real de sismos por región
                        val regionStats = remember(quakes) {
                            listOf(
                                Triple(
                                    "Azuero Sur / Coiba",
                                    quakes.filter { isAzueroRegion(it) },
                                    "Falla Soná-Tonosí + Fractura Coiba (ZFP)"
                                ),
                                Triple(
                                    "Golfo de Chiriquí / Burica",
                                    quakes.filter { isChiriquiRegion(it) },
                                    "Zona de Fractura de Panamá (ZFP)"
                                ),
                                Triple(
                                    "Bocas del Toro / Caribe Occ.",
                                    quakes.filter { isBocasRegion(it) },
                                    "Cinturón Deformado Norte de Panamá (CDNP)"
                                ),
                                Triple(
                                    "Cuenca del Canal / Panamá Metro",
                                    quakes.filter { isCanalRegion(it) },
                                    "Fallas Pedro Miguel + Miraflores + Limón"
                                ),
                                Triple(
                                    "Darién / Golfo de San Miguel",
                                    quakes.filter { isDarienRegion(it) },
                                    "Fallas Sansón + Chucunaque"
                                )
                            )
                        }

                        regionStats.forEach { (name, regQuakes, faultInfo) ->
                            val pct = if (totalEvents > 0) (regQuakes.size.toFloat() / totalEvents) * 100 else 0f
                            val isExpanded = expandedRegionName == name

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        expandedRegionName = if (isExpanded) null else name
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                InteractiveRegionBarRow(
                                    label = name,
                                    count = regQuakes.size,
                                    percentage = pct.toInt(),
                                    fault = faultInfo,
                                    isExpanded = isExpanded
                                )

                                AnimatedVisibility(
                                    visible = isExpanded && regQuakes.isNotEmpty(),
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "SISMOS RECIENTES EN ESTA REGIÓN (${regQuakes.size}):",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        regQuakes.take(5).forEach { eq ->
                                            EarthquakeQuickRow(
                                                earthquake = eq,
                                                onClick = { onEarthquakeSelected(eq) }
                                            )
                                        }
                                        if (regQuakes.size > 5) {
                                            Text(
                                                text = "+ ${regQuakes.size - 5} sismos más en esta zona",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        // Vista histórica
                        PanamaRegionsCatalog.regions.forEach { reg ->
                            HistoricRegionBarRow(
                                label = reg.shortName,
                                percentage = reg.percentage,
                                fault = reg.mainFault
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            // 5. Desglose Interactivo de Magnitudes (con toque para desplegar sismos)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DESGLOSE DE MAGNITUDES (TOCA PARA VER SISMOS)",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )
                    Text(
                        text = "Toca cada nivel para explorar la lista de sismos registrados",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    val m3BelowQuakes = quakes.filter { it.magnitude < 3.0 }
                    val m3to4Quakes = quakes.filter { it.magnitude in 3.0..3.9 }
                    val m4to5Quakes = quakes.filter { it.magnitude in 4.0..4.9 }
                    val m5PlusQuakes = quakes.filter { it.magnitude >= 5.0 }

                    val magGroups = listOf(
                        Quad("m5", "M ≥ 5.0 (Fuertes • Sentido Generalizado)", m5PlusQuakes, MagRed),
                        Quad("m4", "M 4.0 – 4.9 (Moderados • Sacudida Clara)", m4to5Quakes, MagOrange),
                        Quad("m3", "M 3.0 – 3.9 (Leves • Sentidos en Reposo)", m3to4Quakes, MagYellow),
                        Quad("m2", "M < 3.0 (Microsismos • Instrumentales)", m3BelowQuakes, MagGreen)
                    )

                    magGroups.forEach { group ->
                        val isExpanded = expandedMagCategory == group.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    expandedMagCategory = if (isExpanded) null else group.id
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            InteractiveMagnitudeBarRow(
                                label = group.label,
                                count = group.quakes.size,
                                total = totalEvents,
                                color = group.color,
                                isExpanded = isExpanded
                            )

                            AnimatedVisibility(
                                visible = isExpanded && group.quakes.isNotEmpty(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, start = 8.dp, end = 8.dp)
                                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "SISMOS EN ESTA CATEGORÍA (${group.quakes.size}):",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = group.color
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    group.quakes.take(5).forEach { eq ->
                                        EarthquakeQuickRow(
                                            earthquake = eq,
                                            onClick = { onEarthquakeSelected(eq) }
                                        )
                                    }
                                    if (group.quakes.size > 5) {
                                        Text(
                                            text = "+ ${group.quakes.size - 5} sismos más en esta categoría",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // 6. Clasificación de Profundidades para Ciudadanos
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANÁLISIS DE PROFUNDIDAD (IMPACTO CIUDADANO)",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )
                    Text(
                        text = "La profundidad define cómo se siente la energía en la superficie",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    DepthImpactRow(
                        title = "Superficiales (< 20 km)",
                        count = shallowCount,
                        total = totalEvents,
                        color = MagOrange,
                        description = "Mayor fuerza local sentida en las poblaciones cercanas al epicentro."
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DepthImpactRow(
                        title = "Intermedios (20 a 70 km)",
                        count = intermediateCount,
                        total = totalEvents,
                        color = PrimaryBlue,
                        description = "Energía amortiguada por la corteza, área de percepción más amplia."
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DepthImpactRow(
                        title = "Profundos (> 70 km)",
                        count = deepCount,
                        total = totalEvents,
                        color = Color(0xFF10B981),
                        description = "Casi nunca causan daños ya que la energía se dispersa antes de llegar al suelo."
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

private data class Quad<A, B, C, D>(val id: A, val label: B, val quakes: C, val color: D)

// Funciones geográficas para clasificar sismos de Panamá
private fun isAzueroRegion(eq: Earthquake): Boolean {
    val placeLower = eq.place.lowercase(Locale.ROOT)
    return eq.latitude in 6.5..8.5 && eq.longitude in -81.5..-79.8 ||
            placeLower.contains("azuero") || placeLower.contains("tonosi") ||
            placeLower.contains("sona") || placeLower.contains("coiba") ||
            placeLower.contains("los santos") || placeLower.contains("herrera")
}

private fun isChiriquiRegion(eq: Earthquake): Boolean {
    val placeLower = eq.place.lowercase(Locale.ROOT)
    return eq.longitude <= -81.8 && eq.latitude in 6.8..9.2 ||
            placeLower.contains("chiriqui") || placeLower.contains("burica") ||
            placeLower.contains("david") || placeLower.contains("puerto armuelles")
}

private fun isBocasRegion(eq: Earthquake): Boolean {
    val placeLower = eq.place.lowercase(Locale.ROOT)
    return eq.latitude > 8.8 && eq.longitude <= -81.2 ||
            placeLower.contains("bocas") || placeLower.contains("almirante") ||
            placeLower.contains("changuinola")
}

private fun isCanalRegion(eq: Earthquake): Boolean {
    val placeLower = eq.place.lowercase(Locale.ROOT)
    return eq.latitude in 8.5..9.7 && eq.longitude in -80.2..-79.0 ||
            placeLower.contains("panama") || placeLower.contains("canal") ||
            placeLower.contains("colon") || placeLower.contains("chorrera")
}

private fun isDarienRegion(eq: Earthquake): Boolean {
    val placeLower = eq.place.lowercase(Locale.ROOT)
    return eq.longitude > -79.0 && eq.latitude in 7.3..9.2 ||
            placeLower.contains("darien") || placeLower.contains("san miguel") ||
            placeLower.contains("meteti")
}

@Composable
private fun CitizenGeodynamicCard(
    hasActiveSwarm: Boolean,
    maxMag: Double,
    totalQuakes: Int
) {
    val bannerBrush = if (hasActiveSwarm) {
        Brush.horizontalGradient(listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF1E293B)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF1E293B)))
    }

    val borderColor = if (hasActiveSwarm) Color(0xFFF59E0B) else Color(0xFF10B981)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bannerBrush)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasActiveSwarm) Icons.Default.NotificationsActive else Icons.Default.Security,
                            contentDescription = null,
                            tint = borderColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasActiveSwarm) "ESTADO: ENJAMBRE SÍSMICO ACTIVO" else "ESTADO: SISMICIDAD NORMAL",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = borderColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasActiveSwarm)
                    "La red sísmica detecta un enjambre activo con alta concentración de eventos en Azuero y Coiba. Para la población: no significa que vendrá un terremoto mayor; es una liberación escalonada de energía tectónica."
                else
                    "La sismicidad en el territorio panameño se encuentra en niveles normales de micro-sismicidad habitual generada por la subducción de Nazca.",
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 17.sp
            )
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
    GlassCard(modifier = modifier, cornerRadius = 14.dp) {
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
                fontSize = 18.sp,
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
private fun InteractiveRegionBarRow(
    label: String,
    count: Int,
    percentage: Int,
    fault: String,
    isExpanded: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Text(
                text = "$count sismos ($percentage%)",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                            listOf(Color(0xFF1E3A8A), PrimaryBlue, Color(0xFF60A5FA))
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
private fun HistoricRegionBarRow(
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
                text = "$percentage% histórico",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                    .background(Color(0xFF475569))
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
private fun InteractiveMagnitudeBarRow(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    isExpanded: Boolean
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$count (${String.format(Locale.US, "%.0f%%", pct * 100)})",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
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

@Composable
private fun DepthImpactRow(
    title: String,
    count: Int,
    total: Int,
    color: Color,
    description: String
) {
    val pct = if (total > 0) count.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(durationMillis = 800),
        label = "depthProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x221E293B), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "$count sismos (${String.format(Locale.US, "%.0f%%", pct * 100)})",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x221E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 10.sp,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun EarthquakeQuickRow(
    earthquake: Earthquake,
    onClick: () -> Unit
) {
    val magColor = when {
        earthquake.magnitude >= 5.0 -> MagRed
        earthquake.magnitude >= 4.0 -> MagOrange
        earthquake.magnitude >= 3.0 -> MagYellow
        else -> MagGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(magColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", earthquake.magnitude),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = earthquake.place,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "${earthquake.getTimeAgo()} • Prof: ${String.format(Locale.US, "%.0f km", earthquake.depthKm)}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ver detalle",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(16.dp)
        )
    }
}
