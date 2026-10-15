package com.sismos.panama.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
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

// Coordenadas geográficas oficiales de la costa de Panamá (Polígono GeoJSON de alta precisión)
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

// Isla de Coiba (Área clave del enjambre sísmico en el Golfo de Montijo / Pacífico)
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

// Fallas tectónicas activas
data class FaultLineGeo(
    val name: String,
    val points: List<Pair<Double, Double>>,
    val color: Color,
    val strokeWidth: Float = 3f,
    val isDashed: Boolean = false
)

val PanamaActiveFaults = listOf(
    // Zona de Fractura de Panamá (ZFP - Pacífico sur)
    FaultLineGeo(
        name = "Z.F. Panamá (ZFP)",
        points = listOf(
            Pair(6.0, -82.60),
            Pair(7.0, -82.72),
            Pair(7.8, -82.84),
            Pair(8.25, -82.95)
        ),
        color = Color(0xFFFF5252),
        strokeWidth = 3.8f
    ),
    // Sistema Burica
    FaultLineGeo(
        name = "Falla Burica",
        points = listOf(
            Pair(8.0, -83.02),
            Pair(8.28, -82.92),
            Pair(8.55, -82.78)
        ),
        color = Color(0xFFFF9800),
        strokeWidth = 3.2f
    ),
    // Falla Soná - Tonosí
    FaultLineGeo(
        name = "Falla Soná-Tonosí",
        points = listOf(
            Pair(7.95, -81.42),
            Pair(7.65, -81.02),
            Pair(7.42, -80.62),
            Pair(7.15, -80.25)
        ),
        color = Color(0xFFFFEB3B),
        strokeWidth = 3.5f
    ),
    // Falla Pedro Miguel (Canal)
    FaultLineGeo(
        name = "Falla Pedro Miguel",
        points = listOf(
            Pair(8.88, -79.62),
            Pair(9.03, -79.60),
            Pair(9.18, -79.58)
        ),
        color = Color(0xFF00E5FF),
        strokeWidth = 2.8f
    ),
    // Falla Limón (Colón)
    FaultLineGeo(
        name = "Falla Limón",
        points = listOf(
            Pair(9.18, -79.88),
            Pair(9.35, -79.84),
            Pair(9.46, -79.78)
        ),
        color = Color(0xFF00E5FF),
        strokeWidth = 2.8f
    ),
    // Cinturón Deformado del Norte (CDNP - Margen Caribe)
    FaultLineGeo(
        name = "Cinturón Deformado Norte (CDNP)",
        points = listOf(
            Pair(9.65, -82.60),
            Pair(9.40, -81.50),
            Pair(9.25, -80.50),
            Pair(9.45, -79.50),
            Pair(9.65, -78.50)
        ),
        color = Color(0xFFFF3366),
        strokeWidth = 4.0f,
        isDashed = true
    ),
    // Falla Chucunaque (Darién)
    FaultLineGeo(
        name = "Falla Chucunaque",
        points = listOf(
            Pair(8.05, -78.10),
            Pair(8.40, -77.80),
            Pair(8.85, -77.50)
        ),
        color = Color(0xFFB388FF),
        strokeWidth = 2.8f
    )
)

data class CityLabel(val name: String, val lat: Double, val lon: Double)

val PanamaKeyCities = listOf(
    CityLabel("David", 8.427, -82.431),
    CityLabel("Santiago", 8.100, -80.983),
    CityLabel("Chitré", 7.962, -80.428),
    CityLabel("Penonomé", 8.519, -80.357),
    CityLabel("Panamá", 8.982, -79.520),
    CityLabel("Colón", 9.359, -79.901),
    CityLabel("Bocas del Toro", 9.340, -82.240),
    CityLabel("Yaviza (Darién)", 8.158, -77.692)
)

@Composable
fun InteractiveMapScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit,
    onEarthquakeSelected: (Earthquake) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val earthquakes = state.filteredEarthquakes

    var selectedEq by remember { mutableStateOf<Earthquake?>(null) }
    var showFaults by remember { mutableStateOf(true) }
    var showWaveSimulation by remember { mutableStateOf(false) }
    var simulationTimeSeconds by remember { mutableStateOf(0f) }

    // Estado del Canvas (Pan & Zoom)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Loop de propagación de ondas
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

    // Auto-seleccionar primer sismo
    LaunchedEffect(earthquakes) {
        if (selectedEq == null && earthquakes.isNotEmpty()) {
            selectedEq = earthquakes.first()
        }
    }

    Scaffold(
        containerColor = Color(0xFF070B13),
        topBar = {
            MapTopBar(
                onBack = onBack,
                earthquakeCount = earthquakes.size,
                showFaults = showFaults,
                onToggleFaults = { showFaults = !showFaults },
                showWaveSimulation = showWaveSimulation,
                onToggleWaves = {
                    showWaveSimulation = !showWaveSimulation
                    if (showWaveSimulation && selectedEq == null && earthquakes.isNotEmpty()) {
                        selectedEq = earthquakes.first()
                    }
                },
                onResetView = {
                    scale = 1f
                    offset = Offset.Zero
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mapa Tectónico Vectorial con Proporción Geográfica Real 1:1
            RealTectonicMapCanvas(
                earthquakes = earthquakes,
                selectedEarthquake = selectedEq,
                showFaults = showFaults,
                showWaveSimulation = showWaveSimulation,
                simulationSeconds = simulationTimeSeconds,
                scale = scale,
                offset = offset,
                onTransform = { pan, zoom ->
                    scale = (scale * zoom).coerceIn(0.7f, 6.0f)
                    offset += pan
                },
                onSelectEarthquake = { eq ->
                    selectedEq = eq
                }
            )

            // Indicador de Onda Sísmica activa
            if (showWaveSimulation && selectedEq != null) {
                WaveSimulationHeader(
                    earthquake = selectedEq!!,
                    elapsedSeconds = simulationTimeSeconds,
                    onStop = { showWaveSimulation = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                )
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

            // Tarjeta inferior flotante del sismo seleccionado
            AnimatedVisibility(
                visible = selectedEq != null,
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
private fun RealTectonicMapCanvas(
    earthquakes: List<Earthquake>,
    selectedEarthquake: Earthquake?,
    showFaults: Boolean,
    showWaveSimulation: Boolean,
    simulationSeconds: Float,
    scale: Float,
    offset: Offset,
    onTransform: (Offset, Float) -> Unit,
    onSelectEarthquake: (Earthquake) -> Unit
) {
    // Límites geográficos reales de Panamá con margen oceánico
    val minLon = -83.5
    val maxLon = -77.0
    val minLat = 6.8
    val maxLat = 10.0

    val deltaLon = (maxLon - minLon).toFloat() // 6.5 grados
    val deltaLat = (maxLat - minLat).toFloat() // 3.2 grados

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Proyección con Proporción 1:1 Estricta (Equirectangular a 8.5°N de latitud)
    fun project(lat: Double, lon: Double, size: Size): Offset {
        val paddingX = 20f
        val usableWidth = size.width - (paddingX * 2f)

        // Factor de escala idéntico para X y Y para preservar la forma real de Panamá
        val pxPerDegree = usableWidth / deltaLon
        val mapHeight = deltaLat * pxPerDegree

        val startX = paddingX
        val startY = (size.height - mapHeight) / 2f - 40f

        val rawX = startX + ((lon - minLon).toFloat() * pxPerDegree)
        val rawY = startY + ((maxLat - lat).toFloat() * pxPerDegree)

        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val finalX = (rawX - centerX) * scale + centerX + offset.x
        val finalY = (rawY - centerY) * scale + centerY + offset.y
        return Offset(finalX, finalY)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(pan, zoom)
                }
            }
            .pointerInput(earthquakes, scale, offset, canvasSize) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width <= 0f) return@detectTapGestures
                    var closestEq: Earthquake? = null
                    var minDistance = 55f

                    for (eq in earthquakes) {
                        val pt = project(eq.latitude, eq.longitude, canvasSize)
                        val dist = sqrt((pt.x - tapOffset.x).pow(2) + (pt.y - tapOffset.y).pow(2))
                        if (dist < minDistance) {
                            minDistance = dist
                            closestEq = eq
                        }
                    }
                    if (closestEq != null) {
                        onSelectEarthquake(closestEq)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasSize = size

            // 1. Fondo Oceánico Profundo
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF070B13), Color(0xFF0A101D), Color(0xFF05080E))
                )
            )

            // Cuadrícula sutil batimétrica / coordenadas
            drawGeographicGrid(this, size, scale, offset)

            // 2. Trazo Continental del Istmo de Panamá
            val panamaPath = Path()
            OfficialPanamaCoast.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) panamaPath.moveTo(p.x, p.y) else panamaPath.lineTo(p.x, p.y)
            }
            panamaPath.close()

            // Masa continental sólida acrílica
            drawPath(
                path = panamaPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF131D2E), Color(0xFF18253A)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            // Litoral con resplandor cian/azul nítido
            drawPath(
                path = panamaPath,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 2.2f * scale.coerceIn(0.8f, 2.2f))
            )

            // 3. Isla de Coiba (Epicentro histórico y enjambres)
            val coibaPath = Path()
            CoibaIsland.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) coibaPath.moveTo(p.x, p.y) else coibaPath.lineTo(p.x, p.y)
            }
            coibaPath.close()
            drawPath(path = coibaPath, color = Color(0xFF131D2E))
            drawPath(path = coibaPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.8f * scale))

            // 4. Archipiélago de Las Perlas
            val perlasPath = Path()
            LasPerlasIslands.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) perlasPath.moveTo(p.x, p.y) else perlasPath.lineTo(p.x, p.y)
            }
            perlasPath.close()
            drawPath(path = perlasPath, color = Color(0xFF131D2E))
            drawPath(path = perlasPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.5f * scale))

            // 5. Ciudades de Referencia Nacional
            PanamaKeyCities.forEach { city ->
                val pos = project(city.lat, city.lon, size)
                drawCircle(
                    color = Color(0x9994A3B8),
                    radius = 3.5f * scale.coerceIn(0.8f, 1.8f),
                    center = pos
                )
            }

            // 6. Fallas Tectónicas Activas
            if (showFaults) {
                PanamaActiveFaults.forEach { fault ->
                    val path = Path()
                    fault.points.forEachIndexed { idx, pt ->
                        val p = project(pt.first, pt.second, size)
                        if (idx == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    }

                    // Resplandor
                    drawPath(
                        path = path,
                        color = fault.color.copy(alpha = 0.28f),
                        style = Stroke(width = fault.strokeWidth * 3.2f * scale.coerceIn(0.8f, 2f))
                    )
                    // Trazo principal
                    drawPath(
                        path = path,
                        color = fault.color,
                        style = Stroke(
                            width = fault.strokeWidth * scale.coerceIn(0.8f, 2f),
                            pathEffect = if (fault.isDashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) else null
                        )
                    )
                }
            }

            // 7. Simulación de Ondas Sísmicas P y S
            if (showWaveSimulation && selectedEarthquake != null) {
                val epi = project(selectedEarthquake.latitude, selectedEarthquake.longitude, size)
                val paddingX = 20f
                val usableWidth = size.width - (paddingX * 2f)
                val pxPerDegree = usableWidth / deltaLon
                val pxPerKm = (pxPerDegree / 110.5f) * scale

                val radiusP = simulationSeconds * 6.0f * pxPerKm
                val radiusS = simulationSeconds * 3.5f * pxPerKm

                // Onda P (Cyan / Compresiva)
                if (radiusP > 5f) {
                    drawCircle(
                        color = Color(0x3300E5FF),
                        radius = radiusP,
                        center = epi,
                        style = Stroke(width = 3.5f)
                    )
                    drawCircle(
                        color = Color(0x1100E5FF),
                        radius = radiusP,
                        center = epi
                    )
                }

                // Onda S (Rojo / Destructiva)
                if (radiusS > 5f) {
                    drawCircle(
                        color = Color(0x88FF5252),
                        radius = radiusS,
                        center = epi,
                        style = Stroke(width = 4.5f)
                    )
                    drawCircle(
                        color = Color(0x22FF5252),
                        radius = radiusS,
                        center = epi
                    )
                }
            }

            // 8. Epicentros / Sismos
            earthquakes.forEach { eq ->
                val pos = project(eq.latitude, eq.longitude, size)
                val isSelected = selectedEarthquake?.id == eq.id

                val magColor = when {
                    eq.magnitude >= 5.0 -> Color(0xFFFF5252)
                    eq.magnitude >= 4.0 -> Color(0xFFFF9800)
                    eq.magnitude >= 3.0 -> Color(0xFFFFEB3B)
                    else -> Color(0xFF00E676)
                }

                val baseRadius = (eq.magnitude.toFloat() * 2.2f).coerceIn(5f, 16f) * scale.coerceIn(0.8f, 2.2f)

                // Halo exterior
                drawCircle(
                    color = magColor.copy(alpha = if (isSelected) 0.55f else 0.28f),
                    radius = baseRadius * (if (isSelected) 2.2f else 1.6f),
                    center = pos
                )

                // Núcleo
                drawCircle(
                    color = magColor,
                    radius = baseRadius,
                    center = pos
                )

                // Centro blanco
                drawCircle(
                    color = Color.White,
                    radius = baseRadius * 0.4f,
                    center = pos
                )

                // Retícula de selección
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 2.8f,
                        center = pos,
                        style = Stroke(width = 2.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
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
    val gridColor = Color(0x10FFFFFF)
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
    showFaults: Boolean,
    onToggleFaults: () -> Unit,
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
                        text = "$earthquakeCount sismos activos • Geometría Nacional 1:1",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
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

        Spacer(modifier = Modifier.height(6.dp))

        // Fila de Capas / Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showFaults,
                onClick = onToggleFaults,
                label = { Text("Fallas Geológicas", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x33FF9800),
                    selectedLabelColor = Color(0xFFFFB74D),
                    selectedLeadingIconColor = Color(0xFFFFB74D),
                    containerColor = Color(0x1AFFFFFF),
                    labelColor = Color(0xFF94A3B8)
                )
            )

            FilterChip(
                selected = showWaveSimulation,
                onClick = onToggleWaves,
                label = { Text("Ondas P & S", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x333186FF),
                    selectedLabelColor = Color(0xFF93C5FD),
                    selectedLeadingIconColor = Color(0xFF93C5FD),
                    containerColor = Color(0x1AFFFFFF),
                    labelColor = Color(0xFF94A3B8)
                )
            )
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
                text = "PROPAGACIÓN DE ONDAS EN VIVO • ${elapsedSeconds.toInt()}s",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF60A5FA)
            )
            Text(
                text = "Onda P: ${distP} km (6 km/s) • Onda S: ${distS} km (3.5 km/s)",
                fontSize = 12.sp,
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
        cornerRadius = 20.dp
    ) {
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
                        containerColor = if (isSimulatingWaves) Color(0xFFFF5252) else Color(0x333186FF)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSimulatingWaves) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSimulatingWaves) "Detener Onda" else "Simular Ondas",
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
