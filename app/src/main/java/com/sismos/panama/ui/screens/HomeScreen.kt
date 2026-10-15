package com.sismos.panama.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.AlertSettings
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.components.EarthquakeCard
import com.sismos.panama.ui.components.NotificationSettingsModal
import com.sismos.panama.ui.components.PanamaRegionsBanner
import com.sismos.panama.ui.theme.BgDark
import com.sismos.panama.ui.theme.MagGreen
import com.sismos.panama.ui.theme.PrimaryBlue
import com.sismos.panama.ui.theme.StrokeDark
import com.sismos.panama.ui.viewmodel.SeismicViewModel

@Composable
fun HomeScreen(
    viewModel: SeismicViewModel,
    onEarthquakeSelected: (Earthquake) -> Unit,
    onNavigateToFaults: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showNotificationSettings by remember { mutableStateOf(false) }
    var alertSettings by remember { mutableStateOf(AlertSettings.load(context)) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            HomeTopBar(
                isLoading = state.isLoading,
                alertSettings = alertSettings,
                onRefresh = { viewModel.loadEarthquakes(isManualRefresh = true) },
                onNavigateToFaults = onNavigateToFaults,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToMap = onNavigateToMap,
                onNavigateToAbout = onNavigateToAbout,
                onOpenNotificationSettings = { showNotificationSettings = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Status Banner Liquid Glass con acceso al Mapa, Dashboard y Alertas
            LiveStatusBanner(
                alertSettings = alertSettings,
                onOpenDashboard = onNavigateToDashboard,
                onOpenMap = onNavigateToMap,
                onOpenNotificationSettings = { showNotificationSettings = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros de Magnitud Liquid Acrylic
            MagnitudeFilterRow(
                selectedMinMag = state.selectedMinMag,
                onSelectMinMag = { viewModel.setMinMagnitudeFilter(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtro Interactivo por Enjambres y Fallas Geológicas
            FaultAndSwarmFilterRow(
                allEarthquakes = state.earthquakes,
                selectedFaultId = state.selectedFaultId,
                isSwarmOnly = state.isSwarmOnly,
                onSelectFault = { viewModel.setFaultFilter(it) },
                onToggleSwarm = { viewModel.toggleSwarmFilter() }
            )

            // Tarjeta Educativa para Ciudadanos (No geólogos)
            val activeEducationalItem = when {
                state.isSwarmOnly -> CitizenSwarmInfo
                state.selectedFaultId != null -> CitizenFaultCatalog.find { it.id == state.selectedFaultId }
                else -> null
            }

            if (activeEducationalItem != null) {
                Spacer(modifier = Modifier.height(8.dp))
                CitizenEducationCard(
                    item = activeEducationalItem,
                    earthquakeCount = state.filteredEarthquakes.size,
                    onDismiss = { viewModel.clearAllFilters() },
                    onOpenMap = onNavigateToMap
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                // Zonas sísmicas de Panamá (5 Zonas con Acrylic Cards)
                PanamaRegionsBanner(
                    selectedRegion = state.selectedRegion,
                    onSelectRegion = { viewModel.setRegionFilter(it) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lista de Sismos ordenados cronológicamente
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.earthquakes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (state.errorMessage != null && state.earthquakes.isEmpty()) {
                    ErrorState(
                        message = state.errorMessage ?: "Error desconocido",
                        onRetry = { viewModel.loadEarthquakes(isManualRefresh = true) }
                    )
                } else if (state.filteredEarthquakes.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(state.filteredEarthquakes, key = { _, eq -> eq.id }) { index, eq ->
                            EarthquakeCard(
                                earthquake = eq,
                                onClick = { onEarthquakeSelected(eq) },
                                orderIndex = index
                            )
                        }

                        // Acceso rápido a Acerca de la Aplicación
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF141C2B))
                                    .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToAbout() }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Acerca de Sismos Panamá",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "v2.5.0 • Propósito, fuentes oficiales y emergencias",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showNotificationSettings) {
        NotificationSettingsModal(
            onDismiss = { showNotificationSettings = false },
            onSettingsChanged = { alertSettings = it }
        )
    }
}

@Composable
private fun HomeTopBar(
    isLoading: Boolean,
    alertSettings: AlertSettings,
    onRefresh: () -> Unit,
    onNavigateToFaults: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    val topBarBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF131B2A),
            Color(0xFF0D121B)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(topBarBrush)
            .statusBarsPadding()
            .border(width = 0.8.dp, brush = Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color.Transparent)), shape = androidx.compose.ui.graphics.RectangleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SISMOS PANAMÁ",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Red Sismológica Oficial en Vivo",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Botón Mapa Interactivo Tectónico
            IconButton(
                onClick = onNavigateToMap,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x2200E5FF))
                    .border(0.8.dp, Color(0x6600E5FF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Mapa Tectónico Interactivo",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Dashboard Gráficas
            IconButton(
                onClick = onNavigateToDashboard,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x223186FF))
                    .border(0.8.dp, Color(0x553186FF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Dashboard",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Fallas Tectónicas
            IconButton(
                onClick = onNavigateToFaults,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
                    .border(0.8.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Fallas Geológicas",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Alertas y Notificaciones (Campana Dinámica)
            val notifIcon = when {
                alertSettings.isCompletelyDisabled() -> Icons.Default.NotificationsOff
                alertSettings.soundEnabled && alertSettings.vibrateEnabled -> Icons.Default.NotificationsActive
                alertSettings.vibrateEnabled -> Icons.Default.Vibration
                alertSettings.soundEnabled -> Icons.Default.VolumeUp
                else -> Icons.Default.Notifications
            }
            val notifTint = when {
                alertSettings.isCompletelyDisabled() -> Color(0xFFEF4444)
                alertSettings.soundEnabled && alertSettings.vibrateEnabled -> Color(0xFF00E5FF)
                alertSettings.vibrateEnabled -> Color(0xFFF59E0B)
                alertSettings.soundEnabled -> Color(0xFF38BDF8)
                else -> Color(0xFFA78BFA)
            }

            IconButton(
                onClick = onOpenNotificationSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(notifTint.copy(alpha = 0.2f))
                    .border(0.8.dp, notifTint.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = notifIcon,
                    contentDescription = "Configurar Alertas Sísmicas",
                    tint = notifTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Acerca de
            IconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x228B5CF6))
                    .border(0.8.dp, Color(0x558B5CF6), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Acerca de",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Refrescar
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
                    .border(0.8.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatusBanner(
    alertSettings: AlertSettings,
    onOpenDashboard: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    val bannerBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF172554),
            Color(0xFF0F172A),
            Color(0xFF1E3A8A)
        )
    )

    val notifBadgeColor = when {
        alertSettings.isCompletelyDisabled() -> Color(0xFFEF4444)
        alertSettings.soundEnabled && alertSettings.vibrateEnabled -> Color(0xFF00E5FF)
        alertSettings.vibrateEnabled -> Color(0xFFF59E0B)
        alertSettings.soundEnabled -> Color(0xFF38BDF8)
        else -> Color(0xFFA78BFA)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bannerBrush)
            .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenMap() }
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(MagGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EN VIVO IGC-UP • 20S",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBAE6FD)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip Interactivo de Alertas Sísmicas
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(notifBadgeColor.copy(alpha = 0.18f))
                        .border(0.8.dp, notifBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onOpenNotificationSettings() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔔 ${alertSettings.getModeDescription()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = notifBadgeColor
                    )
                }

                Text(
                    text = "Mapa ❯",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.clickable { onOpenMap() }
                )
            }
        }
    }
}

@Composable
private fun MagnitudeFilterRow(
    selectedMinMag: Double?,
    onSelectMinMag: (Double?) -> Unit
) {
    val filters = listOf(
        Pair("Todos", null),
        Pair("M ≥ 3.0", 3.0),
        Pair("M ≥ 4.0", 4.0),
        Pair("M ≥ 5.0", 5.0)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (label, value) ->
            val isSelected = selectedMinMag == value
            val chipBg = if (isSelected) PrimaryBlue else Color(0xFF141F30)
            val chipBorder = if (isSelected) PrimaryBlue else Color(0x3338BDF8)
            val chipTextColor = if (isSelected) Color.White else Color(0xFF94A3B8)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(99.dp))
                    .clickable { onSelectMinMag(value) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = chipTextColor
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = Color(0xFFF87171),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Error al conectar con la red sísmica",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron sismos para los filtros seleccionados en Panamá.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -----------------------------------------------------------------------------------------
// COMPONENTES EDUCATIVOS E INTERACTIVOS DE FALLAS Y ENJAMBRES PARA CIUDADANOS (NO GEÓLOGOS)
// -----------------------------------------------------------------------------------------

data class FaultFilterItem(
    val id: String,
    val shortName: String,
    val fullName: String,
    val color: Color,
    val plainDescription: String,
    val whyItShakes: String,
    val safetyTip: String
)

val CitizenSwarmInfo = FaultFilterItem(
    id = "swarm_azuero",
    shortName = "Enjambre Azuero-Coiba",
    fullName = "⚡ ENJAMBRE SÍSMICO ACTIVO (AZUERO - COIBA)",
    color = Color(0xFFFFB300),
    plainDescription = "Un 'enjambre sísmico' ocurre cuando la tierra tiembla decenas de veces seguidas en una misma zona, pero con sismos moderados repetitivos en vez de un gran terremoto destructor.",
    whyItShakes = "La Placa oceánica de Nazca empuja desde el Pacífico contra el sur de Azuero y Veraguas, reacomodando fracturas profundas de la corteza con liberación gradual de energía.",
    safetyTip = "La gran mayoría son eventos leves a moderados (M 3.0 a 4.8) sentidos en Tonosí, Pedasí y Santiago. Mantén la calma: este proceso natural dispersa la energía acumulada."
)

val CitizenFaultCatalog = listOf(
    FaultFilterItem(
        id = "sona_tonosi",
        shortName = "Soná-Tonosí",
        fullName = "Sistema de Fallas Soná - Tonosí",
        color = Color(0xFFFFD600),
        plainDescription = "Gran grieta geológica activa que recorre desde el Golfo de Montijo en Veraguas hasta la punta sur de Los Santos en Azuero.",
        whyItShakes = "Dos bloques de roca sólida se deslizan lateralmente rozando entre sí horizontalmente. Es la estructura directamente responsable del enjambre actual.",
        safetyTip = "Presenta actividad sismológica frecuente en el Pacífico panameño. Es monitoreada las 24 horas por el Instituto de Geociencias (IGC-UP)."
    ),
    FaultFilterItem(
        id = "zfp",
        shortName = "Z.F. Panamá (ZFP)",
        fullName = "Zona de Fractura de Panamá (ZFP)",
        color = Color(0xFFFF3B30),
        plainDescription = "Corte submarino gigantesco en el Océano Pacífico, al sur de Chiriquí y Coiba, donde dos placas oceánicas chocan lateralmente.",
        whyItShakes = "La placa de Cocos y la placa de Nazca avanzan en direcciones opuestas como dos trenes rozándose a gran velocidad.",
        safetyTip = "Al ubicarse a más de 80 km mar adentro, casi todos sus sismos se disipan en el mar y rara vez representan riesgo destructivo en tierra firme."
    ),
    FaultFilterItem(
        id = "burica",
        shortName = "Falla de Burica",
        fullName = "Sistema de Fallas de Burica (Chiriquí)",
        color = Color(0xFFFF9500),
        plainDescription = "Fractura tectónica costera ubicada en la frontera entre Chiriquí y Costa Rica (cerca de Puerto Armuelles y David).",
        whyItShakes = "Punto de compresión triple donde la cordillera volcánica y el fondo marino colisionan de frente.",
        safetyTip = "Es una de las zonas de mayor sismicidad del país. Se recomienda mantener asegurados objetos pesados y repisas en viviendas de Chiriquí."
    ),
    FaultFilterItem(
        id = "pedro_miguel",
        shortName = "Pedro Miguel (Canal)",
        fullName = "Falla Pedro Miguel / Miraflores",
        color = Color(0xFF00E5FF),
        plainDescription = "Falla geológica que cruza el eje central de Panamá muy cerca de las esclusas del Canal de Panamá y la ciudad capital.",
        whyItShakes = "Acomoda lentamente las tensiones tectónicas entre el este y el oeste del istmo.",
        safetyTip = "Su recurrencia es muy espaciada (cientos de años). Cuenta con una red de acelerógrafos dedicados del Canal de Panamá (ACP)."
    ),
    FaultFilterItem(
        id = "limon",
        shortName = "Falla de Limón",
        fullName = "Falla de Limón (Bocas del Toro)",
        color = Color(0xFF00E5FF),
        plainDescription = "Falla cortical en el Caribe occidental que cruza entre Bocas del Toro y la costa vecina de Costa Rica.",
        whyItShakes = "La corteza caribeña comprime el litoral contra la Cordillera Central.",
        safetyTip = "Zona con historial de sismos importantes en el Caribe. Conoce siempre las rutas de evacuación en zonas costeras."
    ),
    FaultFilterItem(
        id = "cdnp",
        shortName = "Frente Caribe (CDNP)",
        fullName = "Cinturón Deformado del Norte de Panamá",
        color = Color(0xFFFF2D55),
        plainDescription = "Frente submarino en el Mar Caribe desde Bocas del Toro hasta San Blas donde el istmo cabalga sobre el suelo oceánico.",
        whyItShakes = "Choque frontal entre el bloque de Panamá y la gran Placa del Caribe en aguas profundas.",
        safetyTip = "Sismos muy fuertes en esta zona en el mar activan protocolos de vigilancia marítima preventiva."
    ),
    FaultFilterItem(
        id = "chucunaque",
        shortName = "Chucunaque (Darién)",
        fullName = "Falla de Chucunaque / Sansón (Darién)",
        color = Color(0xFFAF52DE),
        plainDescription = "Falla longitudinal que recorre el eje central de Darién hacia Colombia.",
        whyItShakes = "Absorbe la presión geológica de la cordillera de los Andes colombianos empujando hacia Panamá.",
        safetyTip = "Genera sismos profundos en zonas selváticas con poca densidad urbana."
    )
)

@Composable
private fun FaultAndSwarmFilterRow(
    allEarthquakes: List<Earthquake>,
    selectedFaultId: String?,
    isSwarmOnly: Boolean,
    onSelectFault: (String) -> Unit,
    onToggleSwarm: () -> Unit
) {
    val swarmQuakesCount = remember(allEarthquakes) {
        allEarthquakes.count { it.latitude in 7.0..8.2 && it.longitude in -82.0..-80.2 }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILTRAR POR ENJAMBRE O FALLA",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            if (isSwarmOnly || selectedFaultId != null) {
                Text(
                    text = "● Filtro activo",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB300)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip de Enjambre Sísmico (Destacado)
            item {
                val swarmBorder = if (isSwarmOnly) Color(0xFFFFB300) else Color(0x55F59E0B)
                val swarmBg = if (isSwarmOnly) Color(0x33FFB300) else Color(0x18F59E0B)

                Surface(
                    onClick = onToggleSwarm,
                    shape = RoundedCornerShape(10.dp),
                    color = swarmBg,
                    border = androidx.compose.foundation.BorderStroke(if (isSwarmOnly) 1.5.dp else 1.dp, swarmBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300))
                        )
                        Text(
                            text = "⚡ Enjambre Azuero ($swarmQuakesCount)",
                            fontSize = 11.sp,
                            fontWeight = if (isSwarmOnly) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSwarmOnly) Color.White else Color(0xFFFFD54F)
                        )
                    }
                }
            }

            // Chips de Fallas Geológicas
            items(CitizenFaultCatalog) { fault ->
                val isSelected = selectedFaultId == fault.id
                val count = remember(allEarthquakes, fault.id) {
                    allEarthquakes.count { eq ->
                        when (fault.id) {
                            "sona_tonosi" -> eq.nearestFault.contains("Soná", ignoreCase = true) ||
                                    eq.nearestFault.contains("Tonosí", ignoreCase = true) ||
                                    (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
                            "zfp" -> eq.nearestFault.contains("Fractura", ignoreCase = true) || eq.nearestFault.contains("ZFP", ignoreCase = true)
                            "burica" -> eq.nearestFault.contains("Burica", ignoreCase = true) || eq.nearestFault.contains("Armuelles", ignoreCase = true)
                            "pedro_miguel" -> eq.nearestFault.contains("Pedro Miguel", ignoreCase = true)
                            "limon" -> eq.nearestFault.contains("Limón", ignoreCase = true) || eq.nearestFault.contains("Limon", ignoreCase = true)
                            "cdnp" -> eq.nearestFault.contains("CDNP", ignoreCase = true) || eq.nearestFault.contains("Caribe", ignoreCase = true)
                            "chucunaque" -> eq.nearestFault.contains("Chucunaque", ignoreCase = true) || eq.nearestFault.contains("Darién", ignoreCase = true)
                            else -> eq.nearestFault.contains(fault.id, ignoreCase = true)
                        }
                    }
                }

                Surface(
                    onClick = { onSelectFault(fault.id) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) fault.color.copy(alpha = 0.25f) else Color(0xFF141F30),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.8.dp,
                        color = if (isSelected) fault.color else Color(0x3338BDF8)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(fault.color)
                        )
                        Text(
                            text = "${fault.shortName} ($count)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CitizenEducationCard(
    item: FaultFilterItem,
    earthquakeCount: Int,
    onDismiss: () -> Unit,
    onOpenMap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xF0101827),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, item.color.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Text(
                        text = item.fullName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8))
                }
            }

            // Explicación sencilla para personas no geólogas
            Text(
                text = item.plainDescription,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
            )

            // ¿Por qué tiembla ahí?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Por qué tiembla: ${item.whyItShakes}",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 15.sp
                )
            }

            // Recomendación preventiva ciudadana
            Text(
                text = "💡 Para tu seguridad: ${item.safetyTip}",
                fontSize = 11.sp,
                color = Color(0xFF93C5FD),
                lineHeight = 15.sp
            )

            // Barra de acción inferior con conteo y botón para ver en mapa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrando $earthquakeCount sismos registrados",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = item.color
                )

                Button(
                    onClick = onOpenMap,
                    colors = ButtonDefaults.buttonColors(containerColor = item.color.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, item.color),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "Ver en Mapa ❯",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
