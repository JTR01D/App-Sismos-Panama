package com.sismos.panama.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.data.models.PanamaFaultsCatalog
import com.sismos.panama.ui.components.GlassCard
import com.sismos.panama.ui.components.MagnitudeBadge
import com.sismos.panama.ui.theme.*
import com.sismos.panama.ui.viewmodel.SeismicViewModel
import kotlinx.coroutines.delay
import kotlin.math.*

// Coordenadas de referencia para ciudades principales de Panamá
data class GeoCity(
    val name: String,
    val lat: Double,
    val lon: Double
)

val PanamaCities = listOf(
    GeoCity("David", 8.427, -82.431),
    GeoCity("Santiago", 8.100, -80.983),
    GeoCity("Chitré", 7.962, -80.428),
    GeoCity("Penonomé", 8.519, -80.357),
    GeoCity("Panamá", 8.982, -79.520),
    GeoCity("Colón", 9.359, -79.901),
    GeoCity("La Palma (Darién)", 8.407, -78.140),
    GeoCity("Bocas del Toro", 9.340, -82.240)
)

// Segmentos geográficos de fallas tectónicas activas de Panamá
data class FaultSegment(
    val name: String,
    val points: List<Pair<Double, Double>>, // Lat, Lon
    val color: Color,
    val strokeWidth: Float = 3f,
    val isDashed: Boolean = false
)

val PanamaFaultSegments = listOf(
    // Zona de Fractura de Panamá (ZFP - Pacífico sur)
    FaultSegment(
        name = "Z.F. Panamá (ZFP)",
        points = listOf(
            Pair(6.2, -82.6),
            Pair(7.0, -82.75),
            Pair(7.8, -82.85),
            Pair(8.25, -82.95)
        ),
        color = Color(0xFFFF5252),
        strokeWidth = 4f
    ),
    // Sistema Burica - Puerto Armuelles
    FaultSegment(
        name = "Falla Burica",
        points = listOf(
            Pair(8.0, -83.0),
            Pair(8.28, -82.9),
            Pair(8.55, -82.75)
        ),
        color = Color(0xFFFF7A00),
        strokeWidth = 3.5f
    ),
    // Falla Soná - Tonosí (Azuero diagonal)
    FaultSegment(
        name = "Falla Soná-Tonosí",
        points = listOf(
            Pair(7.9, -81.4),
            Pair(7.6, -81.0),
            Pair(7.4, -80.6),
            Pair(7.15, -80.25)
        ),
        color = Color(0xFFFFD600),
        strokeWidth = 3.5f
    ),
    // Falla Pedro Miguel (Canal de Panamá)
    FaultSegment(
        name = "Falla Pedro Miguel",
        points = listOf(
            Pair(8.85, -79.62),
            Pair(9.03, -79.60),
            Pair(9.20, -79.58)
        ),
        color = Color(0xFF00E5FF),
        strokeWidth = 3f
    ),
    // Falla Limón (Colón)
    FaultSegment(
        name = "Falla Limón",
        points = listOf(
            Pair(9.15, -79.88),
            Pair(9.35, -79.84),
            Pair(9.48, -79.78)
        ),
        color = Color(0xFF00E5FF),
        strokeWidth = 3f
    ),
    // Cinturón Deformado del Norte de Panamá (CDNP - Margen Caribe)
    FaultSegment(
        name = "Cinturón Deformado Norte (CDNP)",
        points = listOf(
            Pair(9.7, -82.6),
            Pair(9.4, -81.5),
            Pair(9.25, -80.5),
            Pair(9.45, -79.5),
            Pair(9.65, -78.5)
        ),
        color = Color(0xFFFF3366),
        strokeWidth = 4.5f,
        isDashed = true
    ),
    // Falla Chucunaque - Sansón (Darién)
    FaultSegment(
        name = "Falla Chucunaque",
        points = listOf(
            Pair(8.05, -78.1),
            Pair(8.40, -77.8),
            Pair(8.85, -77.5)
        ),
        color = Color(0xFFB388FF),
        strokeWidth = 3f
    )
)

// Silueta costera simplificada del Istmo de Panamá
val PanamaCoastline = listOf(
    // Costa Caribe (de oeste a este)
    Pair(9.55, -82.85),
    Pair(9.35, -82.25),
    Pair(9.00, -81.80),
    Pair(8.90, -81.00),
    Pair(9.10, -80.20),
    Pair(9.35, -79.90),
    Pair(9.55, -79.50),
    Pair(9.45, -78.80),
    Pair(9.20, -78.00),
    Pair(8.70, -77.30),
    // Frontera Colombia
    Pair(8.10, -77.15),
    Pair(7.60, -77.80),
    // Costa Pacífica (de este a oeste)
    Pair(8.30, -78.15), // Golfo San Miguel
    Pair(8.45, -78.50),
    Pair(8.90, -79.20),
    Pair(8.95, -79.55), // Bahía Panamá
    Pair(8.60, -79.85),
    Pair(8.30, -80.40),
    Pair(7.70, -80.20), // Península Azuero este
    Pair(7.20, -80.35), // Punta Mala
    Pair(7.25, -80.70), // Tonosí
    Pair(7.60, -81.00),
    Pair(7.85, -81.25), // Golfo de Montijo
    Pair(7.50, -81.80),
    Pair(8.20, -81.85),
    Pair(8.30, -82.35), // Golfo Chiriquí
    Pair(8.15, -82.85), // Burica
    Pair(8.45, -83.05),
    Pair(8.75, -82.90)  // Frontera Costa Rica
)

@Composable
fun InteractiveMapScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit,
    onEarthquakeSelected: (Earthquake) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val earthquakes = state.filteredEarthquakes

    // Selección interactiva y controles
    var selectedEq by remember { mutableStateOf<Earthquake?>(null) }
    var showFaults by remember { mutableStateOf(true) }
    var showWaveSimulation by remember { mutableStateOf(false) }
    var simulationTimeSeconds by remember { mutableStateOf(0f) }

    // Estado del Canvas (Pan & Zoom)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Temporizador de propagación de onda cuando está activo
    LaunchedEffect(showWaveSimulation, selectedEq) {
        if (showWaveSimulation && selectedEq != null) {
            simulationTimeSeconds = 0f
            while (showWaveSimulation) {
                delay(50)
                simulationTimeSeconds += 0.25f
                if (simulationTimeSeconds > 90f) {
                    simulationTimeSeconds = 0f // Loop suave
                }
            }
        }
    }

    // Auto-seleccionar el sismo más reciente si ninguno está seleccionado
    LaunchedEffect(earthquakes) {
        if (selectedEq == null && earthquakes.isNotEmpty()) {
            selectedEq = earthquakes.first()
        }
    }

    Scaffold(
        containerColor = Color(0xFF080D16),
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
            // Lienzo interactivo Vectorial
            InteractiveTectonicCanvas(
                earthquakes = earthquakes,
                selectedEarthquake = selectedEq,
                showFaults = showFaults,
                showWaveSimulation = showWaveSimulation,
                simulationSeconds = simulationTimeSeconds,
                scale = scale,
                offset = offset,
                onTransform = { pan, zoom ->
                    scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                    offset += pan
                },
                onSelectEarthquake = { eq ->
                    selectedEq = eq
                }
            )

            // Indicador de Onda Sísmica activa en la esquina superior
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

            // Barra flotante de Controles de Zoom
            FloatingZoomControls(
                onZoomIn = { scale = (scale * 1.25f).coerceIn(0.8f, 5.0f) },
                onZoomOut = { scale = (scale / 1.25f).coerceIn(0.8f, 5.0f) },
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
                        text = "EXPLORADOR TECTÓNICO",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "$earthquakeCount sismos activos • Mapa Tectónico Nacional",
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
private fun InteractiveTectonicCanvas(
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
    // Límites de coordenadas geográficas de Panamá
    val minLat = 6.2
    val maxLat = 10.4
    val minLon = -83.6
    val maxLon = -76.8

    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    fun project(lat: Double, lon: Double, size: androidx.compose.ui.geometry.Size): Offset {
        val normX = ((lon - minLon) / (maxLon - minLon)).toFloat()
        val normY = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()) // Invertir Y porque en pantalla 0 está arriba
        val rawX = normX * size.width
        val rawY = normY * size.height

        // Aplicar transformación pan y zoom alrededor del centro
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val scaledX = (rawX - centerX) * scale + centerX + offset.x
        val scaledY = (rawY - centerY) * scale + centerY + offset.y
        return Offset(scaledX, scaledY)
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
                    // Buscar si se tocó algún sismo dentro de un radio de tolerancia
                    var closestEq: Earthquake? = null
                    var minDistance = 50f // radio táctil en px

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

            // 1. Fondo oceánico con degradado profundo
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF070B13), Color(0xFF0D1524))
                )
            )

            // Cuadrícula sutil de latitud/longitud
            drawCoordinateGrid(this, size, scale, offset)

            // 2. Silueta del Istmo de Panamá
            val coastPath = Path()
            PanamaCoastline.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) coastPath.moveTo(p.x, p.y) else coastPath.lineTo(p.x, p.y)
            }
            coastPath.close()

            // Relleno de la masa continental con acrílico oscuro
            drawPath(
                path = coastPath,
                color = Color(0xFF131D2E)
            )
            // Borde costero con brillo azul sutil
            drawPath(
                path = coastPath,
                color = Color(0x4460A5FA),
                style = Stroke(width = 2.5f * scale)
            )

            // 3. Ciudades principales de Panamá
            PanamaCities.forEach { city ->
                val pos = project(city.lat, city.lon, size)
                drawCircle(
                    color = Color(0x8894A3B8),
                    radius = 3.5f * scale.coerceIn(0.8f, 2f),
                    center = pos
                )
            }

            // 4. Fallas Tectónicas Activas
            if (showFaults) {
                PanamaFaultSegments.forEach { fault ->
                    val path = Path()
                    fault.points.forEachIndexed { idx, pt ->
                        val p = project(pt.first, pt.second, size)
                        if (idx == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    }

                    // Resplandor de la falla
                    drawPath(
                        path = path,
                        color = fault.color.copy(alpha = 0.25f),
                        style = Stroke(width = fault.strokeWidth * 3.5f * scale.coerceIn(0.8f, 2f))
                    )
                    // Trazo principal de la falla
                    drawPath(
                        path = path,
                        color = fault.color,
                        style = Stroke(
                            width = fault.strokeWidth * scale.coerceIn(0.8f, 2f),
                            pathEffect = if (fault.isDashed) PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f) else null
                        )
                    )
                }
            }

            // 5. Simulación de Ondas Sísmicas P y S
            if (showWaveSimulation && selectedEarthquake != null) {
                val epi = project(selectedEarthquake.latitude, selectedEarthquake.longitude, size)
                // Velocidades aproximadas: Onda P = 6 km/s, Onda S = 3.5 km/s
                // Escala de píxeles por km aproximada en pantalla
                val kmPerDegree = 111f
                val degWidth = (maxLon - minLon).toFloat()
                val pxPerKm = (size.width / (degWidth * kmPerDegree)) * scale

                val radiusP = simulationSeconds * 6.0f * pxPerKm
                val radiusS = simulationSeconds * 3.5f * pxPerKm

                // Onda P (Compresiva - Azul/Cyan)
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

                // Onda S (Corte / Destructiva - Rojo/Naranja)
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

            // 6. Sismos / Epicentros
            earthquakes.forEach { eq ->
                val pos = project(eq.latitude, eq.longitude, size)
                val isSelected = selectedEarthquake?.id == eq.id

                val magColor = when {
                    eq.magnitude >= 5.0 -> Color(0xFFFF5252)
                    eq.magnitude >= 4.0 -> Color(0xFFFF9800)
                    eq.magnitude >= 3.0 -> Color(0xFFFFEB3B)
                    else -> Color(0xFF00E676)
                }

                val baseRadius = (eq.magnitude.toFloat() * 2.2f).coerceIn(5f, 18f) * scale.coerceIn(0.8f, 2.5f)

                // Resplandor exterior
                drawCircle(
                    color = magColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                    radius = baseRadius * (if (isSelected) 2.2f else 1.6f),
                    center = pos
                )

                // Núcleo del sismo
                drawCircle(
                    color = magColor,
                    radius = baseRadius,
                    center = pos
                )
                // Centro luminoso
                drawCircle(
                    color = Color.White,
                    radius = baseRadius * 0.4f,
                    center = pos
                )

                // Retícula de objetivo si está seleccionado
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 2.8f,
                        center = pos,
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                }
            }
        }
    }
}

private fun drawCoordinateGrid(
    drawScope: DrawScope,
    size: androidx.compose.ui.geometry.Size,
    scale: Float,
    offset: Offset
) {
    val gridColor = Color(0x14FFFFFF)
    val step = 100f * scale.coerceIn(0.8f, 2f)
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
