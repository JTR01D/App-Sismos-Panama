package com.sismos.panama.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.components.GlassCard
import com.sismos.panama.ui.components.MagnitudeBadge
import com.sismos.panama.ui.theme.PrimaryBlue
import com.sismos.panama.ui.viewmodel.SeismicViewModel
import kotlinx.coroutines.delay
import kotlin.math.*

// Contorno Nacional Oficial de la República de Panamá (52 puntos georreferenciados)
val OfficialPanamaCoast = listOf(
    Pair(7.2238, -77.8816),
    Pair(7.5123, -78.2149),
    Pair(8.0520, -78.4292),
    Pair(8.3192, -78.1821),
    Pair(8.3877, -78.4355),
    Pair(8.7181, -78.6221),
    Pair(8.9961, -79.1203),
    Pair(8.9324, -79.5579),
    Pair(8.5845, -79.7606),
    Pair(8.3333, -80.1645),
    Pair(8.2984, -80.3827),
    Pair(8.0903, -80.4807),
    Pair(7.5475, -80.0037),
    Pair(7.4198, -80.2767),
    Pair(7.2716, -80.4212),
    Pair(7.2205, -80.8864),
    Pair(7.8179, -81.0595),
    Pair(7.6479, -81.1897),
    Pair(7.7066, -81.5195),
    Pair(8.1090, -81.7213),
    Pair(8.1754, -82.1314),
    Pair(8.2924, -82.3909),
    Pair(8.2909, -82.8201),
    Pair(8.0738, -82.8510),
    Pair(8.2250, -82.9658),
    Pair(8.4235, -82.9132),
    Pair(8.6263, -82.8298),
    Pair(8.8073, -82.8687),
    Pair(8.9257, -82.7192),
    Pair(9.0743, -82.9272),
    Pair(9.4768, -82.9329),
    Pair(9.5661, -82.5462),
    Pair(9.2074, -82.1871),
    Pair(8.9956, -82.2076),
    Pair(8.9506, -81.8086),
    Pair(9.0320, -81.7142),
    Pair(8.7862, -81.4393),
    Pair(8.8585, -80.9473),
    Pair(9.1111, -80.5219),
    Pair(9.3128, -79.9146),
    Pair(9.6116, -79.5733),
    Pair(9.5529, -79.0212),
    Pair(9.4546, -79.0585),
    Pair(9.4205, -78.5009),
    Pair(9.2477, -78.0559),
    Pair(8.9468, -77.7295),
    Pair(8.6705, -77.3534),
    Pair(8.5243, -77.4747),
    Pair(7.9353, -77.2426),
    Pair(7.6381, -77.4311),
    Pair(7.7098, -77.7534),
    Pair(7.2238, -77.8816)
)

// Isla de Coiba (Epicentro del enjambre sísmico del Pacífico)
val CoibaIsland = listOf(
    Pair(7.62, -81.75),
    Pair(7.58, -81.65),
    Pair(7.35, -81.68),
    Pair(7.25, -81.80),
    Pair(7.42, -81.92),
    Pair(7.58, -81.85)
)

// Archipiélago de Las Perlas
val LasPerlasIslands = listOf(
    Pair(8.42, -79.05),
    Pair(8.30, -78.96),
    Pair(8.22, -79.04),
    Pair(8.32, -79.16)
)

// Lago Gatún / Canal de Panamá (Hito geográfico principal)
val GatunLakePolygon = listOf(
    Pair(9.12, -79.92),
    Pair(9.20, -79.85),
    Pair(9.22, -79.76),
    Pair(9.14, -79.72),
    Pair(9.08, -79.82)
)

// Límites provinciales internos simplificados para dar contexto geográfico real
val ProvinceBorders = listOf(
    // Chiriquí - Bocas del Toro / Veraguas
    listOf(Pair(8.45, -82.90), Pair(8.75, -82.40), Pair(8.85, -81.85)),
    // Veraguas - Azuero (Herrera / Los Santos)
    listOf(Pair(8.15, -81.00), Pair(7.85, -80.90), Pair(7.25, -80.85)),
    // Coclé - Panamá Oeste
    listOf(Pair(8.30, -80.15), Pair(8.70, -80.10), Pair(9.00, -80.20)),
    // Cuenca del Canal (Panamá - Colón)
    listOf(Pair(8.95, -79.55), Pair(9.10, -79.70), Pair(9.35, -79.90)),
    // Panamá - Darién
    listOf(Pair(8.45, -78.50), Pair(8.90, -78.30), Pair(9.20, -78.00))
)

// Rótulos provinciales en el mapa
data class ProvinceLabel(val name: String, val lat: Double, val lon: Double)

val PanamaProvinceLabels = listOf(
    ProvinceLabel("CHIRIQUÍ", 8.45, -82.55),
    ProvinceLabel("BOCAS DEL TORO", 9.30, -82.45),
    ProvinceLabel("VERAGUAS", 8.25, -81.30),
    ProvinceLabel("AZUERO", 7.65, -80.45),
    ProvinceLabel("COCLÉ", 8.55, -80.45),
    ProvinceLabel("COLÓN", 9.25, -79.80),
    ProvinceLabel("PANAMÁ", 9.15, -79.25),
    ProvinceLabel("DARIÉN", 8.20, -77.90)
)

// Modelo interactivo de Fallas Geológicas con ficha técnica completa
data class FaultLineInteractive(
    val id: String,
    val name: String,
    val shortBadge: String,
    val region: String,
    val mechanism: String,
    val slipRate: String,
    val recurrence: String,
    val description: String,
    val historicalEvents: String,
    val points: List<Pair<Double, Double>>,
    val color: Color,
    val labelAnchor: Pair<Double, Double>,
    val strokeWidth: Float = 3.6f,
    val isDashed: Boolean = false
)

val InteractivePanamaFaults = listOf(
    FaultLineInteractive(
        id = "zfp",
        name = "Zona de Fractura de Panamá (ZFP)",
        shortBadge = "Z.F. Panamá (ZFP)",
        region = "Golfo de Chiriquí / Burica (Pacífico Sur)",
        mechanism = "Rumbo dextral (falla transformante oceánica entre Cocos y Nazca)",
        slipRate = ">40 mm/año (la estructura más rápida y activa del istmo)",
        recurrence = "5 a 15 años (M ≥ 6.0) / 25 a 40 años (M ≥ 7.0)",
        description = "Límite tectónico principal que concentra más del 65% de la sismicidad registrada en Panamá. Conecta el centro de expansión de Galápagos con la fosa mesoamericana.",
        historicalEvents = "1934 (Mw 7.4), 1979 (Mw 6.6), 2002 (Mw 6.4), 2021 (Mw 6.8)",
        points = listOf(Pair(5.8, -82.60), Pair(6.8, -82.72), Pair(7.7, -82.84), Pair(8.25, -82.95)),
        color = Color(0xFFFF5252),
        labelAnchor = Pair(6.7, -82.55)
    ),
    FaultLineInteractive(
        id = "burica",
        name = "Sistema de Fallas de Burica / Puerto Armuelles",
        shortBadge = "Falla de Burica",
        region = "Península de Burica (Chiriquí)",
        mechanism = "Transpresiva y rumbo cortical con levantamiento",
        slipRate = "~20-30 mm/año",
        recurrence = "Enjambres frecuentes y sismos constantes",
        description = "Transfiere los esfuerzos de cizallamiento oceánicos de la ZFP hacia la corteza continental de Chiriquí, generando continuos microsismos y enjambres.",
        historicalEvents = "Enjambres de Melliza y Puerto Armuelles (2002, 2019, 2024)",
        points = listOf(Pair(7.95, -83.02), Pair(8.25, -82.92), Pair(8.55, -82.78)),
        color = Color(0xFFFF9800),
        labelAnchor = Pair(8.40, -83.15)
    ),
    FaultLineInteractive(
        id = "sona_tonosi",
        name = "Sistema de Fallas Soná-Tonosí",
        shortBadge = "Falla Soná-Tonosí",
        region = "Golfo de Montijo / Península de Azuero",
        mechanism = "Rumbo sinestral con componente transpresivo cortical",
        slipRate = "1.0 - 2.5 mm/año",
        recurrence = "70 a 150 años (M > 6.5)",
        description = "Estructura continental diagonal que atraviesa desde el Golfo de Montijo hasta la costa sur de Tonosí. Es el origen del enjambre sísmico activo actual.",
        historicalEvents = "1913 (Ms 6.9 Tonosí), 2026 (Enjambre Golfo de Montijo/Coiba Mw 5.0+)",
        points = listOf(Pair(7.95, -81.42), Pair(7.65, -81.02), Pair(7.42, -80.62), Pair(7.15, -80.25)),
        color = Color(0xFFFFEB3B),
        labelAnchor = Pair(7.75, -80.85)
    ),
    FaultLineInteractive(
        id = "pedro_miguel",
        name = "Falla de Pedro Miguel (Canal de Panamá)",
        shortBadge = "Falla Pedro Miguel",
        region = "Cuenca del Canal / Panamá Metro",
        mechanism = "Rumbo lateral izquierdo cortical (sinestral)",
        slipRate = "1.6 - 2.1 mm/año",
        recurrence = "300 a 500 años (ruptura superficial)",
        description = "Cruza directamente las esclusas y obras del Canal de Panamá (Corte Culebra). Es una de las fallas más estudiadas por su cercanía a la infraestructura metropolitana.",
        historicalEvents = "2 de mayo de 1621 (Destrucción de Panamá Viejo, Mw ~6.9)",
        points = listOf(Pair(8.88, -79.62), Pair(9.03, -79.60), Pair(9.18, -79.58)),
        color = Color(0xFF00E5FF),
        labelAnchor = Pair(9.05, -79.40)
    ),
    FaultLineInteractive(
        id = "limon",
        name = "Falla de Limón (Colón)",
        shortBadge = "Falla de Limón",
        region = "Colón / Margen Norte del Canal",
        mechanism = "Rumbo transtensivo cortical",
        slipRate = "~1.0 mm/año",
        recurrence = "400 a 700 años",
        description = "Se extiende desde el Lago Gatún hacia la Bahía de Manzanillo en Colón, conectando con el sistema del Caribe.",
        historicalEvents = "Actividad paleosísmica cuaternaria confirmada en Lago Gatún",
        points = listOf(Pair(9.18, -79.88), Pair(9.35, -79.84), Pair(9.46, -79.78)),
        color = Color(0xFF00E5FF),
        labelAnchor = Pair(9.38, -80.05)
    ),
    FaultLineInteractive(
        id = "cdnp",
        name = "Cinturón Deformado del Norte de Panamá (CDNP)",
        shortBadge = "CDNP (Frente Caribe)",
        region = "Margen Submarino Caribe (Bocas del Toro hasta San Blas)",
        mechanism = "Cabalgamiento inverso y sobrecorrimiento submarino",
        slipRate = "6 a 12 mm/año",
        recurrence = "200 a 450 años (grandes rupturas con tsunami)",
        description = "Zona de colisión activa donde el istmo de Panamá cabalga sobre la placa del Caribe. Históricamente causante de los terremotos y tsunamis más destructivos del Caribe.",
        historicalEvents = "1882 (Mw 7.9 San Blas con tsunami en Colón), 1991 (Mw 7.7 Bocas del Toro)",
        points = listOf(Pair(9.85, -82.80), Pair(9.65, -81.50), Pair(9.50, -80.50), Pair(9.65, -79.50), Pair(9.80, -78.50)),
        color = Color(0xFFFF3366),
        labelAnchor = Pair(9.80, -80.50),
        isDashed = true
    ),
    FaultLineInteractive(
        id = "chucunaque",
        name = "Falla de Chucunaque / Sansón (Darién)",
        shortBadge = "Falla Chucunaque",
        region = "Cuenca del Río Chucunaque (Darién)",
        mechanism = "Rumbo con componente transpresivo cortical",
        slipRate = "~1.5 - 3.0 mm/año",
        recurrence = "Indeterminada / Sismicidad difusa profunda",
        description = "Estructura longitudinal que recorre el eje central del Darién hacia Colombia, absorbiendo la compresión del Bloque Norandino.",
        historicalEvents = "Sismicidad cortical moderada registrada por la red IGC",
        points = listOf(Pair(8.05, -78.10), Pair(8.40, -77.80), Pair(8.85, -77.50)),
        color = Color(0xFFB388FF),
        labelAnchor = Pair(8.55, -77.55)
    )
)

@Composable
fun InteractiveMapScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit,
    onEarthquakeSelected: (Earthquake) -> Unit,
    initialWaveSimulation: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    val allEarthquakes = state.filteredEarthquakes

    // Filtro interactivo de magnitud en mapa
    var minMagFilter by remember { mutableStateOf<Double?>(null) }
    var showOnlySwarm by remember { mutableStateOf(false) }

    val displayEarthquakes = remember(allEarthquakes, minMagFilter, showOnlySwarm) {
        allEarthquakes.filter { eq ->
            val passesMag = minMagFilter == null || eq.magnitude >= minMagFilter!!
            val passesSwarm = !showOnlySwarm || (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
            passesMag && passesSwarm
        }
    }

    // Selecciones interactivas
    var selectedEq by remember { mutableStateOf<Earthquake?>(null) }
    var selectedFault by remember { mutableStateOf<FaultLineInteractive?>(null) }
    var showFaults by remember { mutableStateOf(true) }
    var showWaveSimulation by remember { mutableStateOf(initialWaveSimulation) }
    var simulationTimeSeconds by remember { mutableStateOf(0f) }

    LaunchedEffect(initialWaveSimulation, displayEarthquakes) {
        if (initialWaveSimulation && selectedEq == null && displayEarthquakes.isNotEmpty()) {
            selectedEq = displayEarthquakes.first()
        }
    }

    // Canvas Pan & Zoom
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isSwarmFocused by remember { mutableStateOf(false) }

    // Simulación de ondas en tiempo real
    LaunchedEffect(showWaveSimulation, selectedEq) {
        if (showWaveSimulation && selectedEq != null) {
            simulationTimeSeconds = 0f
            while (showWaveSimulation) {
                delay(60)
                simulationTimeSeconds += 0.35f
                if (simulationTimeSeconds > 90f) {
                    simulationTimeSeconds = 0f
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF070B13),
        topBar = {
            MapTopBar(
                onBack = onBack,
                earthquakeCount = displayEarthquakes.size,
                totalCount = allEarthquakes.size,
                minMagFilter = minMagFilter,
                onSelectMinMag = { minMagFilter = it },
                showOnlySwarm = showOnlySwarm,
                onToggleSwarm = { showOnlySwarm = !showOnlySwarm },
                showFaults = showFaults,
                onToggleFaults = { showFaults = !showFaults },
                selectedFault = selectedFault,
                onSelectFault = { fault ->
                    selectedFault = fault
                    if (fault != null) {
                        selectedEq = null
                    }
                },
                showWaveSimulation = showWaveSimulation,
                onToggleWaves = {
                    showWaveSimulation = !showWaveSimulation
                    if (showWaveSimulation && selectedEq == null && displayEarthquakes.isNotEmpty()) {
                        selectedEq = displayEarthquakes.first()
                    }
                },
                onResetView = {
                    scale = 1f
                    offset = Offset.Zero
                    selectedFault = null
                    selectedEq = null
                    showOnlySwarm = false
                    minMagFilter = null
                    isSwarmFocused = false
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val latestQuake = remember(displayEarthquakes) {
                displayEarthquakes.maxByOrNull { it.timeMillis }
            }

            // Lienzo interactivo avanzado
            AdvancedTectonicMapCanvas(
                earthquakes = displayEarthquakes,
                latestEarthquakeId = latestQuake?.id,
                selectedEarthquake = selectedEq,
                selectedFault = selectedFault,
                showFaults = showFaults,
                showWaveSimulation = showWaveSimulation,
                simulationSeconds = simulationTimeSeconds,
                scale = scale,
                offset = offset,
                onTransform = { pan, zoom ->
                    scale = (scale * zoom).coerceIn(0.7f, 6.0f)
                    offset += pan
                },
                onAutoZoomSwarm = { targetScale, targetOffset ->
                    scale = targetScale
                    offset = targetOffset
                    isSwarmFocused = true
                },
                onSelectEarthquake = { eq ->
                    selectedEq = eq
                    selectedFault = null // Priorizar sismo
                },
                onSelectFault = { fault ->
                    selectedFault = fault
                    selectedEq = null // Priorizar falla
                }
            )

            // Banner flotante de Enfoque Automático en Enjambre
            val swarmQuakes = remember(displayEarthquakes) {
                displayEarthquakes.filter { it.latitude in 7.0..8.2 && it.longitude in -82.0..-80.2 }
            }
            val latestSwarmQuake = remember(swarmQuakes, displayEarthquakes) {
                swarmQuakes.maxByOrNull { it.timeMillis } ?: displayEarthquakes.maxByOrNull { it.timeMillis }
            }
            val swarmCount = swarmQuakes.size

            if (isSwarmFocused && swarmCount >= 5 && !showWaveSimulation) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 12.dp, end = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xF20B1324),
                    border = BorderStroke(1.2.dp, Color(0xFFFFB300))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300))
                        )
                        Text(
                            text = "⚡ Enjambre Azuero ($swarmCount) • M ${String.format(java.util.Locale.US, "%.1f", latestSwarmQuake?.magnitude ?: 0.0)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (latestSwarmQuake != null) {
                            TextButton(
                                onClick = {
                                    selectedEq = latestSwarmQuake
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "📍 Ver #1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                                isSwarmFocused = false
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = "Ver Panamá",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            // Indicador de Onda Sísmica activa en la parte superior
            if (showWaveSimulation && selectedEq != null) {
                WaveSimulationHeader(
                    earthquake = selectedEq!!,
                    elapsedSeconds = simulationTimeSeconds,
                    onStop = { showWaveSimulation = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                )
            }

            // Leyenda de Magnitud Flotante (oculta automáticamente durante simulación de ondas para máxima visibilidad geográfica)
            AnimatedVisibility(
                visible = !showWaveSimulation,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 12.dp,
                        end = 72.dp,
                        bottom = if (selectedEq != null || selectedFault != null) 180.dp else 16.dp
                    )
            ) {
                MapLegendBadge()
            }

            // Controles flotantes de Zoom
            FloatingZoomControls(
                onZoomIn = { scale = (scale * 1.3f).coerceIn(0.7f, 6.0f) },
                onZoomOut = { scale = (scale / 1.3f).coerceIn(0.7f, 6.0f) },
                onReset = {
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )

            // Tarjeta inferior interactiva: Falla seleccionada
            AnimatedVisibility(
                visible = selectedFault != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedFault?.let { fault ->
                    val faultQuakes = allEarthquakes.filter { eq ->
                        when (fault.id) {
                            "sona_tonosi" -> eq.nearestFault.contains("Soná", ignoreCase = true) ||
                                    eq.nearestFault.contains("Tonosí", ignoreCase = true) ||
                                    (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
                            "zfp" -> eq.nearestFault.contains("Fractura", ignoreCase = true) ||
                                    eq.nearestFault.contains("ZFP", ignoreCase = true)
                            "burica" -> eq.nearestFault.contains("Burica", ignoreCase = true) ||
                                    eq.nearestFault.contains("Armuelles", ignoreCase = true)
                            "pedro_miguel" -> eq.nearestFault.contains("Pedro Miguel", ignoreCase = true)
                            "limon" -> eq.nearestFault.contains("Limón", ignoreCase = true) ||
                                    eq.nearestFault.contains("Limon", ignoreCase = true)
                            "cdnp" -> eq.nearestFault.contains("CDNP", ignoreCase = true) ||
                                    eq.nearestFault.contains("Caribe", ignoreCase = true)
                            "chucunaque" -> eq.nearestFault.contains("Chucunaque", ignoreCase = true) ||
                                    eq.nearestFault.contains("Darién", ignoreCase = true)
                            else -> false
                        }
                    }
                    SelectedFaultCard(
                        fault = fault,
                        associatedQuakesCount = faultQuakes.size,
                        onDismiss = { selectedFault = null },
                        onFilterFaultQuakes = {
                            showOnlySwarm = fault.id == "sona_tonosi"
                        }
                    )
                }
            }

            // Tarjeta inferior interactiva: Sismo seleccionado
            AnimatedVisibility(
                visible = selectedEq != null && selectedFault == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedEq?.let { eq ->
                    SelectedEarthquakeCard(
                        earthquake = eq,
                        isSimulatingWaves = showWaveSimulation,
                        onToggleWaveSim = {
                            showWaveSimulation = !showWaveSimulation
                        },
                        onViewDetail = { onEarthquakeSelected(eq) },
                        onDismiss = { selectedEq = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedTectonicMapCanvas(
    earthquakes: List<Earthquake>,
    latestEarthquakeId: String? = null,
    selectedEarthquake: Earthquake?,
    selectedFault: FaultLineInteractive?,
    showFaults: Boolean,
    showWaveSimulation: Boolean,
    simulationSeconds: Float,
    scale: Float,
    offset: Offset,
    onTransform: (Offset, Float) -> Unit,
    onAutoZoomSwarm: ((Float, Offset) -> Unit)? = null,
    onSelectEarthquake: (Earthquake) -> Unit,
    onSelectFault: (FaultLineInteractive) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconProgress"
    )
    val minLon = -83.5
    val maxLon = -77.0
    val minLat = 6.8
    val maxLat = 10.0

    val deltaLon = (maxLon - minLon).toFloat()
    val deltaLat = (maxLat - minLat).toFloat()

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var hasAutoZoomedToSwarm by remember { mutableStateOf(false) }

    LaunchedEffect(canvasSize, earthquakes) {
        if (canvasSize.width > 0f && !hasAutoZoomedToSwarm && earthquakes.isNotEmpty()) {
            val swarmQuakes = earthquakes.filter { it.latitude in 7.0..8.2 && it.longitude in -82.0..-80.2 }
            if (swarmQuakes.size >= 5) {
                val swarmLat = swarmQuakes.map { it.latitude }.average()
                val swarmLon = swarmQuakes.map { it.longitude }.average()

                val paddingX = 20f
                val usableWidth = canvasSize.width - (paddingX * 2f)
                val pxPerDegree = usableWidth / deltaLon
                val mapHeight = deltaLat * pxPerDegree

                val startX = paddingX
                val startY = (canvasSize.height - mapHeight) / 2f - 35f

                val rawX = startX + ((swarmLon - minLon).toFloat() * pxPerDegree)
                val rawY = startY + ((maxLat - swarmLat).toFloat() * pxPerDegree)

                val centerX = canvasSize.width / 2f
                val centerY = canvasSize.height / 2f

                val swarmScale = 2.45f
                val targetOffset = Offset((centerX - rawX) * swarmScale, (centerY - rawY) * swarmScale)

                hasAutoZoomedToSwarm = true
                onAutoZoomSwarm?.invoke(swarmScale, targetOffset)
            }
        }
    }

    // Proyección Equirectangular 1:1
    fun project(lat: Double, lon: Double, size: Size): Offset {
        val paddingX = 20f
        val usableWidth = size.width - (paddingX * 2f)
        val pxPerDegree = usableWidth / deltaLon
        val mapHeight = deltaLat * pxPerDegree

        val startX = paddingX
        val startY = (size.height - mapHeight) / 2f - 35f

        val rawX = startX + ((lon - minLon).toFloat() * pxPerDegree)
        val rawY = startY + ((maxLat - lat).toFloat() * pxPerDegree)

        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val finalX = (rawX - centerX) * scale + centerX + offset.x
        val finalY = (rawY - centerY) * scale + centerY + offset.y
        return Offset(finalX, finalY)
    }

    // Distancia punto a segmento para selección táctil de fallas
    fun distToSegment(p: Offset, v: Offset, w: Offset): Float {
        val l2 = (v.x - w.x).pow(2) + (v.y - w.y).pow(2)
        if (l2 == 0f) return sqrt((p.x - v.x).pow(2) + (p.y - v.y).pow(2))
        val t = (((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2).coerceIn(0f, 1f)
        val proj = Offset(v.x + t * (w.x - v.x), v.y + t * (w.y - v.y))
        return sqrt((p.x - proj.x).pow(2) + (p.y - proj.y).pow(2))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(earthquakes, scale, offset, canvasSize) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width <= 0f) return@detectTapGestures

                    // 1. Prioridad: Detectar si tocó algún sismo directamente
                    var touchedEq: Earthquake? = null
                    var minEqDist = 65f
                    for (eq in earthquakes) {
                        val pt = project(eq.latitude, eq.longitude, canvasSize)
                        val dist = sqrt((pt.x - tapOffset.x).pow(2) + (pt.y - tapOffset.y).pow(2))
                        if (dist < minEqDist) {
                            minEqDist = dist
                            touchedEq = eq
                        }
                    }
                    if (touchedEq != null) {
                        onSelectEarthquake(touchedEq)
                        return@detectTapGestures
                    }

                    // 2. Detectar si tocó alguna etiqueta de falla
                    for (fault in InteractivePanamaFaults) {
                        val labelPt = project(fault.labelAnchor.first, fault.labelAnchor.second, canvasSize)
                        val distLabel = sqrt((labelPt.x - tapOffset.x).pow(2) + (labelPt.y - tapOffset.y).pow(2))
                        if (distLabel < 95f) {
                            onSelectFault(fault)
                            return@detectTapGestures
                        }
                    }

                    // 3. Detectar si tocó alguna línea de falla geológica
                    var touchedFault: FaultLineInteractive? = null
                    var minFaultDist = 55f
                    for (fault in InteractivePanamaFaults) {
                        for (i in 0 until fault.points.lastIndex) {
                            val p1 = project(fault.points[i].first, fault.points[i].second, canvasSize)
                            val p2 = project(fault.points[i + 1].first, fault.points[i + 1].second, canvasSize)
                            val d = distToSegment(tapOffset, p1, p2)
                            if (d < minFaultDist) {
                                minFaultDist = d
                                touchedFault = fault
                            }
                        }
                    }

                    if (touchedFault != null) {
                        onSelectFault(touchedFault)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(pan, zoom)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasSize = size

            // 1. Fondo oceánico profundo
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF060A12), Color(0xFF090E1A), Color(0xFF04070D))
                )
            )

            // Cuadrícula batimétrica
            drawGeographicGrid(this, size, scale, offset)

            // 2. Masa Continental del Istmo de Panamá
            val panamaPath = Path()
            OfficialPanamaCoast.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) panamaPath.moveTo(p.x, p.y) else panamaPath.lineTo(p.x, p.y)
            }
            panamaPath.close()

            // Relleno sólido acrílico
            drawPath(
                path = panamaPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF131D2E), Color(0xFF19263B)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            // Litoral con borde brillante
            drawPath(
                path = panamaPath,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 2.2f * scale.coerceIn(0.8f, 2.0f))
            )

            // 3. Isla de Coiba y Las Perlas
            val coibaPath = Path()
            CoibaIsland.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) coibaPath.moveTo(p.x, p.y) else coibaPath.lineTo(p.x, p.y)
            }
            coibaPath.close()
            drawPath(path = coibaPath, color = Color(0xFF131D2E))
            drawPath(path = coibaPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.8f * scale))

            val perlasPath = Path()
            LasPerlasIslands.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) perlasPath.moveTo(p.x, p.y) else perlasPath.lineTo(p.x, p.y)
            }
            perlasPath.close()
            drawPath(path = perlasPath, color = Color(0xFF131D2E))
            drawPath(path = perlasPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.5f * scale))

            // 4. Lago Gatún / Canal de Panamá
            val gatunPath = Path()
            GatunLakePolygon.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) gatunPath.moveTo(p.x, p.y) else gatunPath.lineTo(p.x, p.y)
            }
            gatunPath.close()
            drawPath(path = gatunPath, color = Color(0xFF0B121E))
            drawPath(path = gatunPath, color = Color(0x6600E5FF), style = Stroke(width = 1.2f * scale))

            // 5. Límites Provinciales Internos
            ProvinceBorders.forEach { borderLine ->
                val bPath = Path()
                borderLine.forEachIndexed { idx, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (idx == 0) bPath.moveTo(p.x, p.y) else bPath.lineTo(p.x, p.y)
                }
                drawPath(
                    path = bPath,
                    color = Color(0x3394A3B8),
                    style = Stroke(
                        width = 1.2f * scale.coerceIn(0.8f, 1.8f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                )
            }

            // 6. Rótulos Provinciales
            val provTextPaint = Paint().apply {
                color = android.graphics.Color.argb(90, 148, 163, 184)
                textSize = (12f * scale.coerceIn(0.8f, 2.0f)).coerceAtLeast(10f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            PanamaProvinceLabels.forEach { prov ->
                val pos = project(prov.lat, prov.lon, size)
                drawContext.canvas.nativeCanvas.drawText(prov.name, pos.x, pos.y, provTextPaint)
            }

            // 7. Enjambre Sísmico Activo (Azuero - Coiba): Área Resaltada
            val clusterQuakes = earthquakes.filter { it.latitude in 7.0..8.2 && it.longitude in -82.0..-80.2 }
            if (clusterQuakes.size >= 5) {
                val centerPt = project(7.55, -81.0, size)
                drawCircle(
                    color = Color(0x18FF9800),
                    radius = 95f * scale.coerceIn(0.8f, 2.2f),
                    center = centerPt
                )
                drawCircle(
                    color = Color(0x44FF9800),
                    radius = 95f * scale.coerceIn(0.8f, 2.2f),
                    center = centerPt,
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                )

                // Rótulo del enjambre
                val swarmTextPaint = Paint().apply {
                    color = android.graphics.Color.argb(220, 255, 183, 77)
                    textSize = (11f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(10f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "⚡ ENJAMBRE SÍSMICO ACTIVO (${clusterQuakes.size})",
                    centerPt.x,
                    centerPt.y + (115f * scale.coerceIn(0.8f, 2.2f)),
                    swarmTextPaint
                )
            }

            // 8. Fallas Tectónicas Activas con Rótulos Interactivos
            if (showFaults) {
                InteractivePanamaFaults.forEach { fault ->
                    val isFaultSelected = selectedFault?.id == fault.id
                    val path = Path()
                    fault.points.forEachIndexed { idx, pt ->
                        val p = project(pt.first, pt.second, size)
                        if (idx == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    }

                    // Resplandor
                    drawPath(
                        path = path,
                        color = if (isFaultSelected) Color.White.copy(alpha = 0.5f) else fault.color.copy(alpha = 0.28f),
                        style = Stroke(width = (if (isFaultSelected) 10f else 6f) * scale.coerceIn(0.8f, 2f))
                    )
                    // Trazo principal
                    drawPath(
                        path = path,
                        color = fault.color,
                        style = Stroke(
                            width = (fault.strokeWidth * (if (isFaultSelected) 1.5f else 1.0f)) * scale.coerceIn(0.8f, 2f),
                            pathEffect = if (fault.isDashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) else null
                        )
                    )

                    // Rótulo / Etiqueta Interactiva sobre el mapa
                    val badgePos = project(fault.labelAnchor.first, fault.labelAnchor.second, size)

                    // Fondo de la etiqueta
                    val badgePaint = Paint().apply {
                        color = if (isFaultSelected) android.graphics.Color.argb(240, 30, 41, 59) else android.graphics.Color.argb(200, 15, 23, 42)
                        style = Paint.Style.FILL
                    }
                    val badgeStroke = Paint().apply {
                        color = if (isFaultSelected) android.graphics.Color.WHITE else fault.color.toArgb()
                        style = Paint.Style.STROKE
                        strokeWidth = if (isFaultSelected) 2.5f else 1.2f
                    }
                    val badgeText = Paint().apply {
                        color = if (isFaultSelected) android.graphics.Color.WHITE else fault.color.toArgb()
                        textSize = (11f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(10f)
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }

                    val textWidth = badgeText.measureText(fault.shortBadge)
                    val badgeRect = android.graphics.RectF(
                        badgePos.x - textWidth / 2f - 14f,
                        badgePos.y - 18f,
                        badgePos.x + textWidth / 2f + 14f,
                        badgePos.y + 10f
                    )

                    drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)
                    drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 10f, 10f, badgeStroke)
                    drawContext.canvas.nativeCanvas.drawText(fault.shortBadge, badgePos.x, badgePos.y - 1f, badgeText)
                }
            }

            // 9. Simulación de Ondas Sísmicas P y S con física realista de atenuación geométrica
            if (showWaveSimulation && selectedEarthquake != null) {
                val epi = project(selectedEarthquake.latitude, selectedEarthquake.longitude, size)
                val paddingX = 20f
                val usableWidth = size.width - (paddingX * 2f)
                val pxPerDegree = usableWidth / deltaLon
                val pxPerKm = (pxPerDegree / 110.5f) * scale

                val distKmP = simulationSeconds * 6.0f
                val distKmS = simulationSeconds * 3.5f

                val radiusP = distKmP * pxPerKm
                val radiusS = distKmS * pxPerKm

                val mag = selectedEarthquake.magnitude.toFloat().coerceAtLeast(2.5f)
                // Alcance físico según magnitud (los sismos mayores cubren más territorio antes de disiparse)
                val maxReachKm = (90f * 10.0.pow(((mag - 3.0) / 2.25)).toFloat()).coerceIn(70f, 1300f)

                val attenP = ((1f - (distKmP / maxReachKm)).coerceIn(0f, 1f)).pow(1.3f)
                val attenS = ((1f - (distKmS / maxReachKm)).coerceIn(0f, 1f)).pow(1.1f)

                val magScale = ((mag - 2.5f) / 3.0f).coerceIn(0.5f, 2.2f)
                val strokeP = (3.2f * magScale * attenP * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(0.6f)
                val strokeS = (4.8f * magScale * attenS * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(0.8f)

                // Onda P (Cyan / Compresiva) - se atenúa progresivamente
                if (attenP > 0.015f && radiusP > 4f) {
                    val alphaStroke = (0.65f * attenP * magScale.coerceAtMost(1.2f)).coerceIn(0f, 0.85f)
                    val alphaFill = (0.12f * attenP).coerceIn(0f, 0.22f)
                    drawCircle(color = Color(0xFF00E5FF).copy(alpha = alphaStroke), radius = radiusP, center = epi, style = Stroke(width = strokeP))
                    drawCircle(color = Color(0xFF00E5FF).copy(alpha = alphaFill), radius = radiusP, center = epi)
                }

                // Onda S (Roja / Corte) - más destructiva y pierde energía con la distancia
                if (attenS > 0.015f && radiusS > 4f) {
                    val alphaStroke = (0.85f * attenS * magScale.coerceAtMost(1.3f)).coerceIn(0f, 0.95f)
                    val alphaFill = (0.18f * attenS).coerceIn(0f, 0.32f)
                    drawCircle(color = Color(0xFFFF5252).copy(alpha = alphaStroke), radius = radiusS, center = epi, style = Stroke(width = strokeS))
                    drawCircle(color = Color(0xFFFF5252).copy(alpha = alphaFill), radius = radiusS, center = epi)
                }
            }

            // 10. Epicentros / Sismos con Rótulos y Claridad
            // Ordenar por magnitud ascendente: sismos grandes sobre sismos pequeños
            val sortedEarthquakes = earthquakes.sortedBy { it.magnitude }

            val magLabelPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (12f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(10f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            sortedEarthquakes.forEach { eq ->
                val pos = project(eq.latitude, eq.longitude, size)
                val isSelected = selectedEarthquake?.id == eq.id

                val magColor = when {
                    eq.magnitude >= 6.0 -> Color(0xFFFF1744) // Severo / Destructivo (Rojo Carmesí)
                    eq.magnitude >= 5.0 -> Color(0xFFFF6D00) // Fuerte / Daño estructural (Naranja Fuego)
                    eq.magnitude >= 4.0 -> Color(0xFFFFD600) // Moderado / Sentido ampliamente (Ámbar Dorado)
                    eq.magnitude >= 3.0 -> Color(0xFF00E5FF) // Leve / Sentido débilmente (Cian Eléctrico)
                    else -> Color(0xFF00E676)                // Instrumental / Imperceptible (Verde Esmeralda)
                }

                val baseRadius = (eq.magnitude.toFloat() * 2.5f).coerceIn(6.5f, 22f) * scale.coerceIn(0.8f, 2.2f)

                // 1. Anillo oscuro exterior para separar ópticamente sismos encimados
                drawCircle(
                    color = Color(0xF0080E1C),
                    radius = baseRadius + 2.5f,
                    center = pos
                )

                // 2. Halo de disipación de energía sismológica
                val haloMultiplier = when {
                    eq.magnitude >= 6.0 -> 3.2f
                    eq.magnitude >= 5.0 -> 2.2f
                    eq.magnitude >= 4.0 -> 1.7f
                    else -> 1.4f
                }
                drawCircle(
                    color = magColor.copy(alpha = if (isSelected) 0.75f else if (eq.magnitude >= 6.0) 0.55f else 0.35f),
                    radius = baseRadius * (if (isSelected) 2.5f else haloMultiplier),
                    center = pos
                )

                // Para eventos destructivos M >= 6.0: Doble anillo de advertencia general
                if (eq.magnitude >= 6.0) {
                    drawCircle(
                        color = Color(0xFFFF1744),
                        radius = baseRadius * 2.2f,
                        center = pos,
                        style = Stroke(width = 2.2f * scale.coerceIn(0.8f, 2f))
                    )
                }

                // 3. Núcleo con color de categoría destructiva
                drawCircle(
                    color = magColor,
                    radius = baseRadius,
                    center = pos
                )

                // 4. Centro brillante de ruptura
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = (baseRadius * 0.35f).coerceAtLeast(2.2f),
                    center = pos
                )

                val isLatest = eq.id == latestEarthquakeId

                // Radar Beacon pulsante dorado para el sismo más reciente (#1)
                if (isLatest) {
                    val pulseRadius = baseRadius + (beaconProgress * 30f * scale.coerceIn(0.8f, 2.0f))
                    val pulseAlpha = (1f - beaconProgress) * 0.9f
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = pos,
                        style = Stroke(width = 2.5f * scale.coerceIn(0.8f, 1.8f))
                    )
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = pulseAlpha * 0.25f),
                        radius = pulseRadius,
                        center = pos
                    )
                }

                // En sismos perceptibles (M ≥ 3.5), seleccionados o más reciente: dibujar etiqueta con el número de magnitud para claridad inmediata
                if (eq.magnitude >= 3.5 || isSelected || isLatest) {
                    val magText = String.format(java.util.Locale.US, "%.1f", eq.magnitude)
                    val tagBgPaint = Paint().apply {
                        color = android.graphics.Color.argb(240, 10, 16, 30)
                        style = Paint.Style.FILL
                    }
                    val tagBorderPaint = Paint().apply {
                        color = if (isLatest) android.graphics.Color.argb(255, 255, 215, 0) else magColor.toArgb()
                        style = Paint.Style.STROKE
                        strokeWidth = if (isLatest) 2.2f else if (eq.magnitude >= 5.0) 2.0f else 1.2f
                    }
                    val tWidth = magLabelPaint.measureText(magText)
                    val tagRect = android.graphics.RectF(
                        pos.x - tWidth / 2f - 7f,
                        pos.y - baseRadius - 22f,
                        pos.x + tWidth / 2f + 7f,
                        pos.y - baseRadius - 2f
                    )
                    drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 6f, 6f, tagBgPaint)
                    drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 6f, 6f, tagBorderPaint)
                    drawContext.canvas.nativeCanvas.drawText(magText, pos.x, pos.y - baseRadius - 6f, magLabelPaint)
                }

                // Distintivo especial flotante en oro: "★ #1 RECIENTE"
                if (isLatest) {
                    val latestBadgeText = "★ #1 RECIENTE"
                    val latestTextPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = (9.5f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8.5f)
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val lw = latestTextPaint.measureText(latestBadgeText)
                    val topY = pos.y - baseRadius - 26f
                    val lRect = android.graphics.RectF(
                        pos.x - lw / 2f - 6f,
                        topY - 16f,
                        pos.x + lw / 2f + 6f,
                        topY
                    )
                    val goldPaint = Paint().apply {
                        color = android.graphics.Color.argb(255, 255, 215, 0)
                        style = Paint.Style.FILL
                    }
                    drawContext.canvas.nativeCanvas.drawRoundRect(lRect, 4f, 4f, goldPaint)
                    drawContext.canvas.nativeCanvas.drawText(latestBadgeText, pos.x, topY - 3.5f, latestTextPaint)
                }

                // Retícula de selección
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 2.8f,
                        center = pos,
                        style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                }
            }
        }
    }
}

private fun drawGeographicGrid(
    drawScope: DrawScope,
    size: Size,
    scale: Float,
    offset: Offset
) {
    val gridColor = Color(0x0CFFFFFF)
    val step = 110f * scale.coerceIn(0.8f, 2f)
    var x = (offset.x % step)
    while (x < size.width) {
        if (x > 0) {
            drawScope.drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        }
        x += step
    }
    var y = (offset.y % step)
    while (y < size.height) {
        if (y > 0) {
            drawScope.drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        y += step
    }
}

@Composable
private fun MapTopBar(
    onBack: () -> Unit,
    earthquakeCount: Int,
    totalCount: Int,
    minMagFilter: Double?,
    onSelectMinMag: (Double?) -> Unit,
    showOnlySwarm: Boolean,
    onToggleSwarm: () -> Unit,
    showFaults: Boolean,
    onToggleFaults: () -> Unit,
    selectedFault: FaultLineInteractive?,
    onSelectFault: (FaultLineInteractive?) -> Unit,
    showWaveSimulation: Boolean,
    onToggleWaves: () -> Unit,
    onResetView: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .border(0.8.dp, Color(0x33FFFFFF), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "EXPLORADOR TECTÓNICO DE PANAMÁ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = if (showWaveSimulation) "Simulación de ondas en tiempo real • Máxima visibilidad" else "Mostrando $earthquakeCount de $totalCount sismos • Toca fallas o sismos",
                        fontSize = 11.sp,
                        color = if (showWaveSimulation) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                    )
                }
            }

            IconButton(
                onClick = onResetView,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar Panamá",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Fila de Filtros y Carrusel de Fallas (se ocultan suavemente durante la simulación de ondas para no restar espacio al mapa)
        AnimatedVisibility(
            visible = !showWaveSimulation,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))

                // Fila de Filtros y Capas Principales con Scroll Horizontal para cualquier tamaño de pantalla
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filtro rápido de sismos para evitar saturación
                    item {
                        FilterChip(
                            selected = minMagFilter == null && !showOnlySwarm,
                            onClick = {
                                onSelectMinMag(null)
                                if (showOnlySwarm) onToggleSwarm()
                            },
                            label = { Text("Todos ($totalCount)", fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x333186FF),
                                selectedLabelColor = Color(0xFF93C5FD),
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = minMagFilter == 4.0,
                            onClick = { onSelectMinMag(if (minMagFilter == 4.0) null else 4.0) },
                            label = { Text("M ≥ 4.0", fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33FF9800),
                                selectedLabelColor = Color(0xFFFFB74D),
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = showOnlySwarm,
                            onClick = onToggleSwarm,
                            label = { Text("⚡ Enjambre Azuero", fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33FF9800),
                                selectedLabelColor = Color(0xFFFFB74D),
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = showFaults,
                            onClick = onToggleFaults,
                            label = { Text("Fallas", fontSize = 11.sp, maxLines = 1) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x3300E5FF),
                                selectedLabelColor = Color(0xFF00E5FF),
                                selectedLeadingIconColor = Color(0xFF00E5FF),
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = showWaveSimulation,
                            onClick = onToggleWaves,
                            label = { Text("Ondas P & S", fontSize = 11.sp, maxLines = 1) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33FF5252),
                                selectedLabelColor = Color(0xFFFF8A80),
                                selectedLeadingIconColor = Color(0xFFFF8A80),
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                // Carrusel Horizontal de Fallas Registradas para acceso interactivo directo
                if (showFaults) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "FALLAS REGISTRADAS (TOCA PARA INSPECCIONAR):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.4.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(InteractivePanamaFaults) { fault ->
                            val isSelected = selectedFault?.id == fault.id
                            Surface(
                                onClick = {
                                    if (isSelected) onSelectFault(null) else onSelectFault(fault)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) fault.color.copy(alpha = 0.25f) else Color(0x18FFFFFF),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) fault.color else Color(0x28FFFFFF)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(fault.color)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = fault.shortBadge,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegendBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xF20B1220),
        border = BorderStroke(0.8.dp, Color(0x44FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "ESCALA DE PELIGRO DESTRUCTIVO",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(Color(0xFF00E676), "<3.0 Instrumental")
                LegendItem(Color(0xFF00E5FF), "3.0-3.9 Leve")
                LegendItem(Color(0xFFFFD600), "4.0-4.9 Moderado")
                LegendItem(Color(0xFFFF6D00), "5.0-5.9 Fuerte")
                LegendItem(Color(0xFFFF1744), "≥6.0 Destructivo")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, fontSize = 10.sp, color = Color(0xFFE2E8F0), fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SelectedFaultCard(
    fault: FaultLineInteractive,
    associatedQuakesCount: Int,
    onDismiss: () -> Unit,
    onFilterFaultQuakes: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(fault.color)
                    )
                    Column {
                        Text(
                            text = fault.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = fault.region,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8))
                }
            }

            // Datos de Geociencia explicados para ciudadanos (No geólogos)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val mechanismPlain = when {
                    fault.mechanism.contains("Rumbo", ignoreCase = true) || fault.mechanism.contains("transcurrente", ignoreCase = true) ->
                        "Deslizamiento lateral (los bloques rozan horizontalmente)"
                    fault.mechanism.contains("Cabalgamiento", ignoreCase = true) || fault.mechanism.contains("inverso", ignoreCase = true) ->
                        "Choque frontal submarino (una placa se mete bajo la otra)"
                    else -> "Deformación tectónica cortical activa"
                }

                Text(text = "Mecanismo: ${fault.mechanism}", fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.SemiBold)
                Text(text = "💡 Explicación: $mechanismPlain", fontSize = 11.sp, color = Color(0xFF93C5FD))
                Text(text = "Tasa de movimiento: ${fault.slipRate}", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontFamily = FontFamily.Monospace)
                Text(text = "Tiempo de recurrencia: ${fault.recurrence}", fontSize = 11.sp, color = Color(0xFFFFB74D))

                val statusText = if (associatedQuakesCount > 0) {
                    "Sismos activos asociados: $associatedQuakesCount registrados${if (fault.id == "sona_tonosi") " (Enjambre activo en curso)" else ""}"
                } else {
                    "Sismos activos asociados: 0 registrados en las últimas 48h (Sin actividad reciente)"
                }
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (associatedQuakesCount > 0) fault.color else Color(0xFF94A3B8)
                )
            }

            Text(
                text = fault.description,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
            )

            Text(
                text = "Historial: ${fault.historicalEvents}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )

            if (associatedQuakesCount > 0) {
                Button(
                    onClick = onFilterFaultQuakes,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = fault.color.copy(alpha = 0.25f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, fault.color),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(
                        text = "⚡ Ver los $associatedQuakesCount sismos de esta falla",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WaveSimulationHeader(
    earthquake: Earthquake,
    elapsedSeconds: Float,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distP = (elapsedSeconds * 6.0f).toInt()
    val distS = (elapsedSeconds * 3.5f).toInt()
    val mag = earthquake.magnitude.toFloat().coerceAtLeast(2.5f)
    val maxReachKm = (90f * 10.0.pow(((mag - 3.0) / 2.25)).toFloat()).coerceIn(70f, 1300f).toInt()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xEE0D1320))
            .border(1.dp, Color(0x553186FF), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "PROPAGACIÓN SÍSMICA CON ATENUACIÓN • ${elapsedSeconds.toInt()}s",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF60A5FA)
            )
            Text(
                text = "Onda P: ${distP} km • Onda S: ${distS} km • Alcance M ${earthquake.magnitude}: ~${maxReachKm} km",
                fontSize = 11.sp,
                color = Color.White
            )
        }
        IconButton(
            onClick = onStop,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Detener",
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun FloatingZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onZoomIn,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xCC162133))
                .border(1.dp, Color(0x44FFFFFF), CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Acercar", tint = Color.White)
        }

        IconButton(
            onClick = onZoomOut,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xCC162133))
                .border(1.dp, Color(0x44FFFFFF), CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Alejar", tint = Color.White)
        }
    }
}

@Composable
private fun SelectedEarthquakeCard(
    earthquake: Earthquake,
    isSimulatingWaves: Boolean,
    onToggleWaveSim: () -> Unit,
    onViewDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = if (isSimulatingWaves) 16.dp else 20.dp
    ) {
        if (isSimulatingWaves) {
            // Modo compacto durante propagación de ondas: Máxima visibilidad del mapa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MagnitudeBadge(magnitude = earthquake.magnitude, size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "M ${earthquake.magnitude} Mw • ${earthquake.place}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Profundidad: ${earthquake.depthKm.toInt()} km • ${earthquake.panamaRegion}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onToggleWaveSim,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Detener", fontSize = 11.sp, color = Color.White)
                    }

                    IconButton(
                        onClick = onViewDetail,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x333186FF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Detalles",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MagnitudeBadge(magnitude = earthquake.magnitude, size = 48.dp)
                        Column {
                            Text(
                                text = "M ${earthquake.magnitude} Mw • ${earthquake.panamaRegion}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${earthquake.place} (${earthquake.depthKm.toInt()} km)",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Text(
                    text = "Falla asociada: ${earthquake.nearestFault} (${earthquake.faultMechanism})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF93C5FD)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Simular Onda
                    Button(
                        onClick = onToggleWaveSim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x333186FF)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simular Ondas",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Botón Ver Detalle
                    Button(
                        onClick = onViewDetail,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Ver Análisis", fontSize = 12.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
