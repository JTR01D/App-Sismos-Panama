package com.sismos.panama.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
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

// Archipiélago de Bocas del Toro (Isla Colón, Carenero y Bastimentos)
val BocasDelToroIslands = listOf(
    Pair(9.44, -82.35),
    Pair(9.40, -82.20),
    Pair(9.30, -82.16),
    Pair(9.30, -82.28),
    Pair(9.38, -82.34)
)

// Isla Cébaco (Entrada al Golfo de Montijo / Zona de Enjambres)
val CebacoIsland = listOf(
    Pair(7.56, -81.25),
    Pair(7.54, -81.12),
    Pair(7.46, -81.18),
    Pair(7.48, -81.28)
)

// Isla Taboga (Bahía de Panamá)
val TabogaIsland = listOf(
    Pair(8.82, -79.57),
    Pair(8.79, -79.53),
    Pair(8.76, -79.55),
    Pair(8.78, -79.58)
)

// Plataforma Continental de Panamá (Isobata Batimétrica de 200 metros)
val ContinentalShelf200m = listOf(
    Pair(8.00, -83.15),
    Pair(7.70, -82.60),
    Pair(7.28, -82.20),
    Pair(7.05, -81.80),
    Pair(6.92, -81.30),
    Pair(7.00, -80.60),
    Pair(7.15, -80.00),
    Pair(7.30, -79.10),
    Pair(7.60, -78.40),
    Pair(7.10, -77.80)
)

// Rótulos de Cuerpos de Agua y Cuencas Oceánicas
data class WaterBodyLabel(val name: String, val lat: Double, val lon: Double)

val PanamaWaterLabels = listOf(
    WaterBodyLabel("MAR CARIBE", 9.80, -81.30),
    WaterBodyLabel("OCÉANO PACÍFICO", 6.85, -80.10),
    WaterBodyLabel("GOLFO DE CHIRIQUÍ", 7.85, -82.40),
    WaterBodyLabel("GOLFO DE PANAMÁ", 8.05, -79.20),
    WaterBodyLabel("GOLFO DE MONTIJO", 7.68, -81.25),
    WaterBodyLabel("BAHÍA DE PARITA", 8.28, -80.35)
)

// Vectores de Movimiento Tectónico GPS (Geodesia de Placas)
data class TectonicPlateVector(
    val plateName: String,
    val velocityLabel: String,
    val anchorLat: Double,
    val anchorLon: Double,
    val vectorDx: Float,
    val vectorDy: Float
)

val MajorTectonicPlates = listOf(
    TectonicPlateVector("PLACA DE COCOS", "72 mm/a ↗", 6.78, -83.15, 34f, -28f),
    TectonicPlateVector("PLACA DE NAZCA", "54 mm/a ↗", 6.82, -78.60, 30f, -14f),
    TectonicPlateVector("PLACA DEL CARIBE", "12 mm/a ↙", 9.88, -79.80, -20f, 16f),
    TectonicPlateVector("BLOQUE NORANDINO", "10 mm/a ↖", 7.55, -77.35, -18f, -16f)
)

// Hitos Volcánicos y Geológicos Destacados
data class VolcanoLandmark(val name: String, val elevation: String, val lat: Double, val lon: Double)

val BaruVolcano = VolcanoLandmark("VOLCÁN BARÚ", "3,475 m", 8.808, -82.543)

// Estaciones Sismológicas Oficiales de Monitoreo (Red Sismológica Nacional IGC - Universidad de Panamá)
data class SeismicStation(
    val code: String,
    val locality: String,
    val lat: Double,
    val lon: Double,
    val isPrimaryCampus: Boolean = false
)

val PanamaSeismicStations = listOf(
    SeismicStation("UPA", "Panamá Metro", 8.98, -79.53, isPrimaryCampus = true),
    SeismicStation("DVD", "David", 8.44, -82.42),
    SeismicStation("TON", "Tonosí", 7.28, -80.44),
    SeismicStation("BOC", "Bocas del Toro", 9.34, -82.24),
    SeismicStation("SAN", "Santiago", 8.11, -80.98)
)

// Centros Urbanos Secundarios de Referencia (sin estación sismológica principal)
data class ReferenceCity(val name: String, val lat: Double, val lon: Double)

val SecondaryReferenceCities = listOf(
    ReferenceCity("Penonomé", 8.52, -80.36),
    ReferenceCity("Chitré", 7.96, -80.43),
    ReferenceCity("Colón", 9.36, -79.90),
    ReferenceCity("Changuinola", 9.43, -82.52),
    ReferenceCity("La Palma", 8.52, -78.14)
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

// Rótulos provinciales en el mapa (posicionados en los centros geográficos sin colisión)
data class ProvinceLabel(val name: String, val lat: Double, val lon: Double)

val PanamaProvinceLabels = listOf(
    ProvinceLabel("CHIRIQUÍ", 8.60, -82.72),
    ProvinceLabel("BOCAS DEL TORO", 9.18, -82.65),
    ProvinceLabel("VERAGUAS", 8.35, -81.35),
    ProvinceLabel("AZUERO", 7.62, -80.60),
    ProvinceLabel("COCLÉ", 8.62, -80.52),
    ProvinceLabel("COLÓN", 9.18, -80.05),
    ProvinceLabel("PANAMÁ", 9.25, -79.15),
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
        id = "coiba",
        name = "Zona de Fractura de Coiba (ZFC)",
        shortBadge = "Z.F. Coiba",
        region = "Sur de Isla de Coiba / Cuenca de Panamá",
        mechanism = "Falla transformante oceánica activa con sismicidad somera",
        slipRate = "~25-35 mm/año",
        recurrence = "Enjambres sísmicos continuos (M 4.0 - 6.2)",
        description = "Estructura transformante marina que delimita el bloque de Coiba con la placa de Nazca, fuente directa de los mayores enjambres recientes del Pacífico.",
        historicalEvents = "2021 (Mw 6.7 Sur de Coiba), 2026 (Enjambres Golfo de Montijo / Coiba)",
        points = listOf(Pair(7.55, -81.80), Pair(7.20, -81.82), Pair(6.70, -81.85), Pair(6.10, -81.90)),
        color = Color(0xFFFF5722),
        labelAnchor = Pair(6.90, -81.65)
    ),
    FaultLineInteractive(
        id = "fosa_mesoamericana",
        name = "Fosa Mesoamericana / Zona de Subducción",
        shortBadge = "Fosa Mesoamericana",
        region = "Pacífico Occidental (Chiriquí - Costa Rica)",
        mechanism = "Subducción de la Placa de Cocos bajo la Microplaca de Panamá",
        slipRate = "~70 mm/año",
        recurrence = "Grandes eventos megathrust interplaca",
        description = "Extremo oriental de la trinchera oceánica mesoamericana, donde la corteza de Cocos penetra bajo el suroeste panameño.",
        historicalEvents = "Sismicidad interplaca recurrente y grandes sismos históricos",
        points = listOf(Pair(7.80, -83.45), Pair(7.45, -83.25), Pair(7.10, -83.05)),
        color = Color(0xFFE91E63),
        labelAnchor = Pair(7.50, -83.40),
        isDashed = true
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
        id = "ocu_pese",
        name = "Sistema de Fallas Ocú - Pesé (Herrera)",
        shortBadge = "Falla Ocú-Pesé",
        region = "Macizo de Azuero / Herrera",
        mechanism = "Falla de rumbo cortical intraplaca",
        slipRate = "0.5 - 1.2 mm/año",
        recurrence = "Baja frecuencia pero alta sensibilidad local en Azuero",
        description = "Estructura cortical interna que atraviesa el norte de la península de Azuero, responsable de eventos locales sentidos con fuerza.",
        historicalEvents = "Sismicidad intraplaca moderada en Azuero y Golfo de Parita",
        points = listOf(Pair(8.05, -80.70), Pair(7.90, -80.55), Pair(7.75, -80.40)),
        color = Color(0xFFFFC107),
        labelAnchor = Pair(8.08, -80.55)
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
        id = "miraflores",
        name = "Falla de Miraflores (Zona del Canal)",
        shortBadge = "Falla Miraflores",
        region = "Esclusas de Miraflores / Balboa / Bahía de Panamá",
        mechanism = "Rumbo lateral cortical con componente normal",
        slipRate = "1.0 - 1.5 mm/año",
        recurrence = "Histórica en el eje interoceánico",
        description = "Alineamiento tectónico paralelo al Canal de Panamá que atraviesa las esclusas del Pacífico hasta la bahía metropolitana.",
        historicalEvents = "Monitoreada permanentemente por la ACP y el IGC-UP",
        points = listOf(Pair(8.95, -79.60), Pair(8.98, -79.58), Pair(9.03, -79.56)),
        color = Color(0xFF00E5FF),
        labelAnchor = Pair(8.92, -79.68)
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

// Categorías Oficiales de la Escala de Peligro Sísmico (USGS / IGC)
enum class MagnitudeHazardTier(
    val id: String,
    val label: String,
    val title: String,
    val color: Color,
    val minMag: Double,
    val maxMag: Double
) {
    MICRO("micro", "<3.0", "Menor", Color(0xFF00E676), 0.0, 2.999),
    LIGHT("light", "3.0-3.9", "Leve", Color(0xFF00E5FF), 3.0, 3.999),
    MODERATE("moderate", "4.0-4.9", "Moderado", Color(0xFFFFD600), 4.0, 4.999),
    STRONG("strong", "5.0-5.9", "Fuerte", Color(0xFFFF6D00), 5.0, 5.999),
    MAJOR("major", "≥6.0", "Mayor", Color(0xFFFF1744), 6.0, 10.0)
}

@Composable
fun InteractiveMapScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit,
    onEarthquakeSelected: (Earthquake) -> Unit,
    initialWaveSimulation: Boolean = false,
    selectFirst: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    val allEarthquakes = state.filteredEarthquakes

    // Filtros interactivos de magnitud y peligro sísmico en mapa
    var minMagFilter by remember { mutableStateOf<Double?>(null) }
    var selectedHazardTier by remember { mutableStateOf<MagnitudeHazardTier?>(null) }
    var showOnlySwarm by remember { mutableStateOf(false) }

    // Conteo de sismos según la escala de peligro sísmico
    val baseQuakesForCounts = remember(allEarthquakes, showOnlySwarm) {
        if (showOnlySwarm) {
            allEarthquakes.filter { it.latitude in 7.0..8.2 && it.longitude in -82.0..-80.2 }
        } else {
            allEarthquakes
        }
    }
    val hazardTierCounts = remember(baseQuakesForCounts) {
        MagnitudeHazardTier.values().associate { tier ->
            tier.id to baseQuakesForCounts.count { eq ->
                eq.magnitude >= tier.minMag && eq.magnitude <= tier.maxMag
            }
        }
    }

    val displayEarthquakes = remember(allEarthquakes, minMagFilter, selectedHazardTier, showOnlySwarm) {
        allEarthquakes.filter { eq ->
            val passesMinMag = minMagFilter == null || eq.magnitude >= minMagFilter!!
            val passesTier = selectedHazardTier == null ||
                    (eq.magnitude >= selectedHazardTier!!.minMag && eq.magnitude <= selectedHazardTier!!.maxMag)
            val passesSwarm = !showOnlySwarm || (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
            passesMinMag && passesTier && passesSwarm
        }
    }

    // Selecciones interactivas
    var selectedEq by remember { mutableStateOf<Earthquake?>(null) }
    var selectedFault by remember { mutableStateOf<FaultLineInteractive?>(null) }
    var showFaults by remember { mutableStateOf(true) }
    var showWaveSimulation by remember { mutableStateOf(initialWaveSimulation) }
    var simulationTimeSeconds by remember { mutableStateOf(0f) }

    LaunchedEffect(initialWaveSimulation, selectFirst, displayEarthquakes) {
        if ((initialWaveSimulation || selectFirst) && selectedEq == null && displayEarthquakes.isNotEmpty()) {
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
                onSelectMinMag = {
                    minMagFilter = it
                    if (it != null) selectedHazardTier = null
                },
                selectedHazardTier = selectedHazardTier,
                onSelectHazardTier = { tier ->
                    selectedHazardTier = tier
                    if (tier != null) minMagFilter = null
                },
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
                    selectedHazardTier = null
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
                selectedHazardTier = selectedHazardTier,
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
                },
                onTapBlank = {
                    selectedEq = null
                    selectedFault = null
                    showWaveSimulation = false
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

            // Mensaje informativo cuando el rango de magnitud seleccionado no tiene eventos registrados
            AnimatedVisibility(
                visible = displayEarthquakes.isEmpty() && selectedHazardTier != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF20B1322),
                    border = BorderStroke(1.dp, selectedHazardTier?.color?.copy(alpha = 0.65f) ?: Color(0xFF38BDF8))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Sin sismos en rango ${selectedHazardTier?.label ?: ""}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "No se registran eventos activos con magnitud ${selectedHazardTier?.label ?: ""}.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { selectedHazardTier = null },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedHazardTier?.color ?: Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Ver Todos los Sismos",
                                color = Color(0xFF091020),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Escala de Peligro Sísmico como selector interactivo por magnitud
            AnimatedVisibility(
                visible = !showWaveSimulation && selectedEq == null && selectedFault == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 12.dp
                    )
            ) {
                MapLegendBadge(
                    selectedTier = selectedHazardTier,
                    onSelectTier = { tier ->
                        selectedHazardTier = tier
                        if (tier != null) {
                            minMagFilter = null
                        }
                    },
                    quakeCounts = hazardTierCounts,
                    totalQuakesCount = baseQuakesForCounts.size,
                    filteredQuakesCount = displayEarthquakes.size,
                    modifier = Modifier.fillMaxWidth()
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
                    .padding(end = 12.dp)
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
                            "coiba" -> eq.nearestFault.contains("Coiba", ignoreCase = true) ||
                                    (eq.latitude in 6.0..7.8 && eq.longitude in -82.3..-81.4)
                            "fosa_mesoamericana" -> eq.nearestFault.contains("Fosa", ignoreCase = true) ||
                                    (eq.latitude in 6.8..8.0 && eq.longitude in -83.5..-82.7)
                            "burica" -> eq.nearestFault.contains("Burica", ignoreCase = true) ||
                                    eq.nearestFault.contains("Armuelles", ignoreCase = true)
                            "ocu_pese" -> eq.nearestFault.contains("Ocú", ignoreCase = true) ||
                                    eq.nearestFault.contains("Pesé", ignoreCase = true) ||
                                    eq.nearestFault.contains("Herrera", ignoreCase = true)
                            "zfp" -> eq.nearestFault.contains("Fractura", ignoreCase = true) ||
                                    eq.nearestFault.contains("ZFP", ignoreCase = true)
                            "pedro_miguel" -> eq.nearestFault.contains("Pedro Miguel", ignoreCase = true)
                            "miraflores" -> eq.nearestFault.contains("Miraflores", ignoreCase = true)
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
                        onDismiss = {
                            selectedEq = null
                            showWaveSimulation = false
                        }
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
    selectedHazardTier: MagnitudeHazardTier? = null,
    selectedFault: FaultLineInteractive?,
    showFaults: Boolean,
    showWaveSimulation: Boolean,
    simulationSeconds: Float,
    scale: Float,
    offset: Offset,
    onTransform: (Offset, Float) -> Unit,
    onAutoZoomSwarm: ((Float, Offset) -> Unit)? = null,
    onSelectEarthquake: (Earthquake) -> Unit,
    onSelectFault: (FaultLineInteractive) -> Unit,
    onTapBlank: (() -> Unit)? = null
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
                val startY = (canvasSize.height - mapHeight) / 2f

                val rawX = startX + ((swarmLon - minLon).toFloat() * pxPerDegree)
                val rawY = startY + ((maxLat - swarmLat).toFloat() * pxPerDegree)

                val centerX = canvasSize.width / 2f
                val centerY = canvasSize.height / 2f

                val swarmScale = 2.15f
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
        val startY = (size.height - mapHeight) / 2f

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
                        return@detectTapGestures
                    }

                    // Tocar espacio libre en el mapa deselecciona para mayor libertad
                    onTapBlank?.invoke()
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

            // 2. Plataforma Continental de Panamá (Isobata Batimétrica 200m)
            val shelfPath = Path()
            ContinentalShelf200m.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) shelfPath.moveTo(p.x, p.y) else shelfPath.lineTo(p.x, p.y)
            }
            drawPath(
                path = shelfPath,
                color = Color(0x3500E5FF),
                style = Stroke(
                    width = 1.4f * scale.coerceIn(0.8f, 1.8f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )

            // Rótulo discreto de la plataforma continental
            val shelfLabelPos = project(7.18, -80.00, size)
            val shelfPaint = Paint().apply {
                color = android.graphics.Color.argb(85, 0, 229, 255)
                textSize = (9f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            drawContext.canvas.nativeCanvas.drawText("ISOBATA 200m • MARGEN CONTINENTAL", shelfLabelPos.x, shelfLabelPos.y + 14f, shelfPaint)

            // 3. Rótulos Marítimos y Cuencas Oceánicas
            val oceanTextPaint = Paint().apply {
                color = android.graphics.Color.argb(70, 56, 189, 248)
                textSize = (13f * scale.coerceIn(0.8f, 2.0f)).coerceAtLeast(10f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.25f
            }
            val gulfTextPaint = Paint().apply {
                color = android.graphics.Color.argb(60, 56, 189, 248)
                textSize = (10f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8.5f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            PanamaWaterLabels.forEach { label ->
                val p = project(label.lat, label.lon, size)
                val paint = if (label.name.startsWith("MAR") || label.name.startsWith("OCÉANO")) oceanTextPaint else gulfTextPaint
                drawContext.canvas.nativeCanvas.drawText(label.name, p.x, p.y, paint)
            }

            // 4. Placas Tectónicas Principales y Vectores GPS
            val platePaint = Paint().apply {
                color = android.graphics.Color.argb(125, 148, 163, 184)
                textSize = (10f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8.5f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.12f
            }
            val velocityPaint = Paint().apply {
                color = android.graphics.Color.argb(175, 0, 229, 255)
                textSize = (9f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(7.5f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            MajorTectonicPlates.forEach { plate ->
                val p = project(plate.anchorLat, plate.anchorLon, size)
                val vScale = scale.coerceIn(0.8f, 1.6f)
                val arrowEnd = Offset(p.x + plate.vectorDx * vScale, p.y + plate.vectorDy * vScale)

                // Vector de movimiento geodésico
                drawLine(
                    color = Color(0x6600E5FF),
                    start = p,
                    end = arrowEnd,
                    strokeWidth = 2.0f * scale.coerceIn(0.8f, 1.6f)
                )
                // Flecha de punta
                val angle = atan2((arrowEnd.y - p.y).toDouble(), (arrowEnd.x - p.x).toDouble())
                val arrowHeadLen = 10f * vScale
                val pHead1 = Offset(
                    (arrowEnd.x - arrowHeadLen * cos(angle - Math.PI / 6.0)).toFloat(),
                    (arrowEnd.y - arrowHeadLen * sin(angle - Math.PI / 6.0)).toFloat()
                )
                val pHead2 = Offset(
                    (arrowEnd.x - arrowHeadLen * cos(angle + Math.PI / 6.0)).toFloat(),
                    (arrowEnd.y - arrowHeadLen * sin(angle + Math.PI / 6.0)).toFloat()
                )
                drawLine(color = Color(0xFF00E5FF), start = arrowEnd, end = pHead1, strokeWidth = 2f)
                drawLine(color = Color(0xFF00E5FF), start = arrowEnd, end = pHead2, strokeWidth = 2f)

                // Rótulo de la placa y velocidad
                drawContext.canvas.nativeCanvas.drawText(plate.plateName, p.x, p.y - 12f, platePaint)
                drawContext.canvas.nativeCanvas.drawText(plate.velocityLabel, p.x, p.y + 1f, velocityPaint)
            }

            // 5. Masa Continental del Istmo de Panamá
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

            // Rótulo central: Microplaca de Panamá
            val microplatePos = project(8.85, -80.90, size)
            val microplatePaint = Paint().apply {
                color = android.graphics.Color.argb(70, 0, 229, 255)
                textSize = (11f * scale.coerceIn(0.8f, 2.0f)).coerceAtLeast(9f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.22f
            }
            drawContext.canvas.nativeCanvas.drawText("MICROPLACA DE PANAMÁ", microplatePos.x, microplatePos.y, microplatePaint)

            // 6. Archipiélagos e Islas
            // Isla de Coiba
            val coibaPath = Path()
            CoibaIsland.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) coibaPath.moveTo(p.x, p.y) else coibaPath.lineTo(p.x, p.y)
            }
            coibaPath.close()
            drawPath(path = coibaPath, color = Color(0xFF131D2E))
            drawPath(path = coibaPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.8f * scale))

            // Archipiélago de Las Perlas
            val perlasPath = Path()
            LasPerlasIslands.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) perlasPath.moveTo(p.x, p.y) else perlasPath.lineTo(p.x, p.y)
            }
            perlasPath.close()
            drawPath(path = perlasPath, color = Color(0xFF131D2E))
            drawPath(path = perlasPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.5f * scale))

            // Bocas del Toro (Isla Colón / Bastimentos)
            val bocasPath = Path()
            BocasDelToroIslands.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) bocasPath.moveTo(p.x, p.y) else bocasPath.lineTo(p.x, p.y)
            }
            bocasPath.close()
            drawPath(path = bocasPath, color = Color(0xFF131D2E))
            drawPath(path = bocasPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.6f * scale))

            // Isla Cébaco (Golfo de Montijo)
            val cebacoPath = Path()
            CebacoIsland.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) cebacoPath.moveTo(p.x, p.y) else cebacoPath.lineTo(p.x, p.y)
            }
            cebacoPath.close()
            drawPath(path = cebacoPath, color = Color(0xFF131D2E))
            drawPath(path = cebacoPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.4f * scale))

            // Isla Taboga (Bahía de Panamá)
            val tabogaPath = Path()
            TabogaIsland.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) tabogaPath.moveTo(p.x, p.y) else tabogaPath.lineTo(p.x, p.y)
            }
            tabogaPath.close()
            drawPath(path = tabogaPath, color = Color(0xFF131D2E))
            drawPath(path = tabogaPath, color = Color(0xFF38BDF8), style = Stroke(width = 1.2f * scale))

            // 7. Lago Gatún / Canal de Panamá
            val gatunPath = Path()
            GatunLakePolygon.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) gatunPath.moveTo(p.x, p.y) else gatunPath.lineTo(p.x, p.y)
            }
            gatunPath.close()
            drawPath(path = gatunPath, color = Color(0xFF0B121E))
            drawPath(path = gatunPath, color = Color(0x6600E5FF), style = Stroke(width = 1.2f * scale))

            // 8. Límites Provinciales Internos
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

            // 9. Rótulos Provinciales de Fondo (Contexto Geográfico Suave / Marca de Agua)
            val provTextPaint = Paint().apply {
                color = android.graphics.Color.argb(45, 148, 163, 184)
                textSize = (12f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(10f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.20f
            }
            PanamaProvinceLabels.forEach { prov ->
                val pos = project(prov.lat, prov.lon, size)
                drawContext.canvas.nativeCanvas.drawText(prov.name, pos.x, pos.y, provTextPaint)
            }

            // 10. Hito Volcánico: Volcán Barú (3,475 m)
            val baruPos = project(BaruVolcano.lat, BaruVolcano.lon, size)
            // Resplandor volcánico ámbar
            drawCircle(
                color = Color(0x22FF9800),
                radius = 16f * scale.coerceIn(0.8f, 2.0f),
                center = baruPos
            )
            // Triángulo representativo del volcán
            val triH = 10f * scale.coerceIn(0.8f, 1.8f)
            val triW = 8f * scale.coerceIn(0.8f, 1.8f)
            val vPath = Path().apply {
                moveTo(baruPos.x, baruPos.y - triH)
                lineTo(baruPos.x + triW, baruPos.y + (triH * 0.6f))
                lineTo(baruPos.x - triW, baruPos.y + (triH * 0.6f))
                close()
            }
            drawPath(path = vPath, color = Color(0xFFFF9800))
            drawPath(path = vPath, color = Color(0xFFFFD54F), style = Stroke(width = 1.2f))

            val baruPaint = Paint().apply {
                color = android.graphics.Color.argb(220, 255, 183, 77)
                textSize = (9.5f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText("▲ VOLCÁN BARÚ (${BaruVolcano.elevation})", baruPos.x, baruPos.y - triH - 4f, baruPaint)

            // 11. Estaciones Sismológicas Oficiales (Red Sismológica Nacional IGC - Universidad de Panamá)
            val stationBadgePaint = Paint().apply {
                color = android.graphics.Color.argb(235, 9, 16, 32)
                style = Paint.Style.FILL
            }
            val stationBorderPaint = Paint().apply {
                color = android.graphics.Color.argb(160, 0, 229, 255)
                style = Paint.Style.STROKE
                strokeWidth = 1.1f
            }
            val stationCodePaint = Paint().apply {
                color = android.graphics.Color.argb(255, 0, 229, 255)
                textSize = (9f * scale.coerceIn(0.8f, 1.6f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            val stationLocalityPaint = Paint().apply {
                color = android.graphics.Color.argb(220, 226, 232, 240)
                textSize = (9f * scale.coerceIn(0.8f, 1.6f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            PanamaSeismicStations.forEach { station ->
                val sPos = project(station.lat, station.lon, size)
                val dSize = 4.5f * scale.coerceIn(0.8f, 1.6f)

                // Resplandor de pulso
                drawCircle(
                    color = Color(0x2800E5FF),
                    radius = dSize * 2.8f,
                    center = sPos
                )

                // Diamante oficial ◆
                val diamondPath = Path().apply {
                    moveTo(sPos.x, sPos.y - dSize)
                    lineTo(sPos.x + dSize, sPos.y)
                    lineTo(sPos.x, sPos.y + dSize)
                    lineTo(sPos.x - dSize, sPos.y)
                    close()
                }
                drawPath(path = diamondPath, color = Color(0xFF00E5FF))
                drawPath(path = diamondPath, color = Color.White, style = Stroke(width = 1.0f))

                // Badge descriptivo de la estación IGC
                val codeText = "◆ ${station.code}"
                val locText = " • ${station.locality}"
                val codeW = stationCodePaint.measureText(codeText)
                val locW = stationLocalityPaint.measureText(locText)
                val totalW = codeW + locW

                val badgeH = 17f * scale.coerceIn(0.8f, 1.4f)
                val badgeStartX = if (station.code == "UPA") {
                    sPos.x - totalW - dSize - 12f
                } else {
                    sPos.x + dSize + 5f
                }
                val badgeStartY = sPos.y - (badgeH / 2f)

                val badgeRect = android.graphics.RectF(
                    badgeStartX - 4f,
                    badgeStartY,
                    badgeStartX + totalW + 8f,
                    badgeStartY + badgeH
                )

                drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 5f, 5f, stationBadgePaint)
                drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 5f, 5f, stationBorderPaint)

                val textBaselineY = badgeStartY + (badgeH * 0.72f)
                drawContext.canvas.nativeCanvas.drawText(codeText, badgeStartX, textBaselineY, stationCodePaint)
                drawContext.canvas.nativeCanvas.drawText(locText, badgeStartX + codeW, textBaselineY, stationLocalityPaint)
            }

            // 12. Centros Urbanos Secundarios (sin estación sismológica principal)
            val secCityPaint = Paint().apply {
                color = android.graphics.Color.argb(140, 203, 213, 225)
                textSize = (8.5f * scale.coerceIn(0.8f, 1.6f)).coerceAtLeast(7.5f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            SecondaryReferenceCities.forEach { city ->
                val cPos = project(city.lat, city.lon, size)
                val dotR = 2.5f * scale.coerceIn(0.8f, 1.4f)
                drawCircle(color = Color(0xDD080E1C), radius = dotR + 1.2f, center = cPos)
                drawCircle(color = Color(0xFF64748B), radius = dotR, center = cPos)
                drawContext.canvas.nativeCanvas.drawText(city.name, cPos.x + dotR + 4f, cPos.y + 3f, secCityPaint)
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

                // Resplandor de pulso cuando está activo el filtro de peligro sísmico
                if (selectedHazardTier != null) {
                    val pulseRadius = baseRadius * (1.3f + (beaconProgress * 1.5f))
                    val pulseAlpha = ((1f - beaconProgress) * 0.7f).coerceIn(0f, 1f)
                    drawCircle(
                        color = magColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = pos,
                        style = Stroke(width = 1.8f * scale.coerceIn(0.8f, 1.8f))
                    )
                }

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

            // 17. Instrumentos Cartográficos de Navegación y Referencia
            drawCoordinateTickMarks(this, size) { lat, lon -> project(lat, lon, size) }
            drawDynamicScaleBar(this, size, scale, deltaLon)
            drawTectonicCompass(this, size, scale)
        }
    }
}

private fun drawTectonicCompass(
    drawScope: DrawScope,
    size: Size,
    scale: Float
) {
    val compassCenter = Offset(size.width - 38f, 215f)
    val r = 20f

    // Fondo acrílico oscuro
    drawScope.drawCircle(
        color = Color(0xDD0B1324),
        radius = r + 7f,
        center = compassCenter
    )
    drawScope.drawCircle(
        color = Color(0x5500E5FF),
        radius = r + 7f,
        center = compassCenter,
        style = Stroke(width = 1.2f)
    )

    // Ticks cardinales e intercardinales
    for (deg in 0 until 360 step 45) {
        val rad = Math.toRadians(deg.toDouble())
        val p1 = Offset(
            compassCenter.x + (cos(rad) * (r + 1f)).toFloat(),
            compassCenter.y + (sin(rad) * (r + 1f)).toFloat()
        )
        val p2 = Offset(
            compassCenter.x + (cos(rad) * (r + 6f)).toFloat(),
            compassCenter.y + (sin(rad) * (r + 6f)).toFloat()
        )
        drawScope.drawLine(
            color = if (deg % 90 == 0) Color(0xFF00E5FF) else Color(0x5594A3B8),
            start = p1,
            end = p2,
            strokeWidth = if (deg % 90 == 0) 1.5f else 1.0f
        )
    }

    // Flecha Norte (Bicolor: Cian Eléctrico y Blanco)
    val northPathRight = Path().apply {
        moveTo(compassCenter.x, compassCenter.y - r)
        lineTo(compassCenter.x + 4.5f, compassCenter.y)
        lineTo(compassCenter.x, compassCenter.y)
        close()
    }
    drawScope.drawPath(northPathRight, color = Color(0xFF00E5FF))

    val northPathLeft = Path().apply {
        moveTo(compassCenter.x, compassCenter.y - r)
        lineTo(compassCenter.x - 4.5f, compassCenter.y)
        lineTo(compassCenter.x, compassCenter.y)
        close()
    }
    drawScope.drawPath(northPathLeft, color = Color.White)

    // Flecha Sur
    val southPath = Path().apply {
        moveTo(compassCenter.x, compassCenter.y + r)
        lineTo(compassCenter.x + 4f, compassCenter.y)
        lineTo(compassCenter.x - 4f, compassCenter.y)
        close()
    }
    drawScope.drawPath(southPath, color = Color(0xFF475569))

    // Puntos Este y Oeste
    val eastPath = Path().apply {
        moveTo(compassCenter.x + r, compassCenter.y)
        lineTo(compassCenter.x, compassCenter.y + 3.5f)
        lineTo(compassCenter.x, compassCenter.y - 3.5f)
        close()
    }
    drawScope.drawPath(eastPath, color = Color(0xFF64748B))

    val westPath = Path().apply {
        moveTo(compassCenter.x - r, compassCenter.y)
        lineTo(compassCenter.x, compassCenter.y + 3.5f)
        lineTo(compassCenter.x, compassCenter.y - 3.5f)
        close()
    }
    drawScope.drawPath(westPath, color = Color(0xFF64748B))

    // Letra "N" de Norte
    val compassPaint = Paint().apply {
        color = android.graphics.Color.argb(240, 0, 229, 255)
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    drawScope.drawContext.canvas.nativeCanvas.drawText("N", compassCenter.x, compassCenter.y - r - 8f, compassPaint)
}

private fun drawDynamicScaleBar(
    drawScope: DrawScope,
    size: Size,
    scale: Float,
    deltaLon: Float
) {
    val paddingX = 20f
    val usableWidth = size.width - (paddingX * 2f)
    val pxPerDegree = usableWidth / deltaLon
    val pxPerKm = (pxPerDegree / 110.5f) * scale

    val targetBarPx = size.width * 0.22f
    val idealKm = targetBarPx / pxPerKm
    val kmStep = when {
        idealKm >= 150f -> 200f
        idealKm >= 75f -> 100f
        idealKm >= 35f -> 50f
        idealKm >= 18f -> 25f
        else -> 10f
    }
    val barWidthPx = kmStep * pxPerKm

    val origin = Offset(24f, 215f)

    // Fondo acrílico estilizado
    val bgRect = android.graphics.RectF(
        origin.x - 6f,
        origin.y - 18f,
        origin.x + barWidthPx + 10f,
        origin.y + 12f
    )
    val bgPaint = Paint().apply {
        color = android.graphics.Color.argb(190, 11, 19, 36)
        style = Paint.Style.FILL
    }
    val borderPaint = Paint().apply {
        color = android.graphics.Color.argb(60, 0, 229, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, 6f, 6f, bgPaint)
    drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, 6f, 6f, borderPaint)

    // Barra segmentada (Cian / Blanco)
    val halfWidth = barWidthPx / 2f
    drawScope.drawRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(origin.x, origin.y),
        size = Size(halfWidth, 3.5f)
    )
    drawScope.drawRect(
        color = Color.White,
        topLeft = Offset(origin.x + halfWidth, origin.y),
        size = Size(halfWidth, 3.5f)
    )

    // Marcas de división
    drawScope.drawLine(Color.White, Offset(origin.x, origin.y - 3f), Offset(origin.x, origin.y + 6f), strokeWidth = 1f)
    drawScope.drawLine(Color.White, Offset(origin.x + halfWidth, origin.y - 3f), Offset(origin.x + halfWidth, origin.y + 6f), strokeWidth = 1f)
    drawScope.drawLine(Color.White, Offset(origin.x + barWidthPx, origin.y - 3f), Offset(origin.x + barWidthPx, origin.y + 6f), strokeWidth = 1f)

    // Textos de escala
    val textPaint = Paint().apply {
        color = android.graphics.Color.argb(220, 203, 213, 225)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    drawScope.drawContext.canvas.nativeCanvas.drawText("0", origin.x, origin.y - 5f, textPaint)
    drawScope.drawContext.canvas.nativeCanvas.drawText("${(kmStep / 2).toInt()}", origin.x + halfWidth, origin.y - 5f, textPaint)
    drawScope.drawContext.canvas.nativeCanvas.drawText("${kmStep.toInt()} km", origin.x + barWidthPx, origin.y - 5f, textPaint)
}

private fun drawCoordinateTickMarks(
    drawScope: DrawScope,
    size: Size,
    project: (Double, Double) -> Offset
) {
    val tickPaint = Paint().apply {
        color = android.graphics.Color.argb(90, 148, 163, 184)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }

    // Latitud (7°N, 8°N, 9°N)
    for (lat in 7..9) {
        val pt = project(lat.toDouble(), -83.45)
        if (pt.y in 50f..(size.height - 50f)) {
            drawScope.drawLine(
                color = Color(0x3300E5FF),
                start = Offset(0f, pt.y),
                end = Offset(7f, pt.y),
                strokeWidth = 1.2f
            )
            drawScope.drawContext.canvas.nativeCanvas.drawText("${lat}°N", 10f, pt.y + 3f, tickPaint)
        }
    }

    // Longitud (-83°W a -78°W)
    for (lon in -83..-78) {
        val pt = project(9.95, lon.toDouble())
        if (pt.x in 25f..(size.width - 25f)) {
            drawScope.drawLine(
                color = Color(0x3300E5FF),
                start = Offset(pt.x, 0f),
                end = Offset(pt.x, 7f),
                strokeWidth = 1.2f
            )
            tickPaint.textAlign = Paint.Align.CENTER
            drawScope.drawContext.canvas.nativeCanvas.drawText("${kotlin.math.abs(lon)}°W", pt.x, 17f, tickPaint)
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
    selectedHazardTier: MagnitudeHazardTier? = null,
    onSelectHazardTier: ((MagnitudeHazardTier?) -> Unit)? = null,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "EXPLORADOR TECTÓNICO",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (showWaveSimulation) "Simulación de ondas en tiempo real" else "Mostrando $earthquakeCount de $totalCount sismos",
                    fontSize = 11.sp,
                    color = if (showWaveSimulation) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onResetView,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar Panamá",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
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
                            selected = minMagFilter == null && !showOnlySwarm && selectedHazardTier == null,
                            onClick = {
                                onSelectMinMag(null)
                                onSelectHazardTier?.invoke(null)
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

                    if (selectedHazardTier != null) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { onSelectHazardTier?.invoke(null) },
                                label = { Text("M ${selectedHazardTier.label} ($earthquakeCount)", fontSize = 11.sp, maxLines = 1) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar rango",
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = selectedHazardTier.color.copy(alpha = 0.25f),
                                    selectedLabelColor = selectedHazardTier.color,
                                    selectedTrailingIconColor = selectedHazardTier.color,
                                    containerColor = Color(0x1AFFFFFF),
                                    labelColor = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }

                    item {
                        FilterChip(
                            selected = minMagFilter == 4.0 && selectedHazardTier == null,
                            onClick = {
                                onSelectHazardTier?.invoke(null)
                                onSelectMinMag(if (minMagFilter == 4.0) null else 4.0)
                            },
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
private fun MapLegendBadge(
    selectedTier: MagnitudeHazardTier?,
    onSelectTier: (MagnitudeHazardTier?) -> Unit,
    quakeCounts: Map<String, Int>,
    totalQuakesCount: Int,
    filteredQuakesCount: Int,
    modifier: Modifier = Modifier
) {
    // 6 Posiciones discretas: 0=Todos, 1=<3.0, 2=3.0-3.9, 3=4.0-4.9, 4=5.0-5.9, 5=≥6.0
    val targetIndex = when (selectedTier) {
        null -> 0
        MagnitudeHazardTier.MICRO -> 1
        MagnitudeHazardTier.LIGHT -> 2
        MagnitudeHazardTier.MODERATE -> 3
        MagnitudeHazardTier.STRONG -> 4
        MagnitudeHazardTier.MAJOR -> 5
    }

    val tiers = MagnitudeHazardTier.values()
    val activeColor = when (targetIndex) {
        0 -> Color(0xFF38BDF8)
        else -> tiers[targetIndex - 1].color
    }

    // Texto informativo dinámico según la posición de la bolita
    val headerBadgeText = when (targetIndex) {
        0 -> "TODOS • $totalQuakesCount SISMOS"
        else -> {
            val tier = tiers[targetIndex - 1]
            val count = quakeCounts[tier.id] ?: 0
            "M ${tier.label} ${tier.title.uppercase()} • $count SISMOS"
        }
    }

    // Animación fluida de la bolita cuando no está siendo arrastrada directamente
    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }

    val animatedIndex by animateFloatAsState(
        targetValue = targetIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "hazardSliderThumb"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xF2091020),
        border = BorderStroke(
            0.9.dp,
            if (targetIndex > 0) activeColor.copy(alpha = 0.55f) else Color(0x3338BDF8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Fila de Encabezado: Título y Badge de Información Dinámica
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(activeColor)
                    )
                    Text(
                        text = "PELIGRO SÍSMICO",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }

                // Badge dinámico con información del nivel seleccionado
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = activeColor.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, activeColor.copy(alpha = 0.8f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = headerBadgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeColor,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        if (targetIndex > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Mostrar todos los sismos",
                                tint = activeColor,
                                modifier = Modifier
                                    .size(10.dp)
                                    .clickable { onSelectTier(null) }
                            )
                        }
                    }
                }
            }

            // 2. Track Gradiente con la "Bolita" Desplazable (Slider Interactivo)
            val labels = listOf("Todos", "<3.0", "3.0-3.9", "4.0-4.9", "5.0-5.9", "≥6.0")

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val density = LocalDensity.current
                val widthPx = constraints.maxWidth.toFloat()
                val thumbRadiusPx = with(density) { 9.dp.toPx() }
                val haloRadiusPx = with(density) { 14.dp.toPx() }
                val usableWidthPx = (widthPx - 2 * thumbRadiusPx).coerceAtLeast(1f)

                // Función auxiliar para mapear coordenada X a índice de magnitud 0..5
                fun getIndexFromX(x: Float): Int {
                    val relativeX = (x - thumbRadiusPx).coerceIn(0f, usableWidthPx)
                    return ((relativeX / usableWidthPx) * 5f).roundToInt().coerceIn(0, 5)
                }

                fun selectIndex(idx: Int) {
                    val newTier = if (idx == 0) null else tiers[idx - 1]
                    onSelectTier(newTier)
                }

                val currentThumbX = if (isDragging) {
                    dragX.coerceIn(thumbRadiusPx, widthPx - thumbRadiusPx)
                } else {
                    thumbRadiusPx + (animatedIndex / 5f) * usableWidthPx
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val tappedIdx = getIndexFromX(offset.x)
                                selectIndex(tappedIdx)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    dragX = offset.x
                                    val idx = getIndexFromX(offset.x)
                                    selectIndex(idx)
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val idx = getIndexFromX(dragX)
                                    selectIndex(idx)
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragX += dragAmount
                                    val idx = getIndexFromX(dragX)
                                    selectIndex(idx)
                                }
                            )
                        }
                ) {
                    // Barra continua de gradiente sísmico (láser neón de 4dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 9.dp)
                            .height(4.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF00E676),
                                        Color(0xFF00E5FF),
                                        Color(0xFFFFD600),
                                        Color(0xFFFF6D00),
                                        Color(0xFFFF1744)
                                    )
                                )
                            )
                    )

                    // Puntos de parada discretos a lo largo del riel
                    for (i in 0..5) {
                        val tickXPx = thumbRadiusPx + (i.toFloat() / 5f) * usableWidthPx
                        val tickXDp = with(density) { tickXPx.toDp() }
                        val isTickActive = targetIndex == i

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = tickXDp - 2.5.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isTickActive) Color.White else Color(0x66000000))
                                .border(0.5.dp, if (isTickActive) activeColor else Color(0x44FFFFFF), CircleShape)
                        )
                    }

                    // La "Bolita" Desplazable (Glowing Thumb)
                    val thumbXDp = with(density) { (currentThumbX - haloRadiusPx).toDp() }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = thumbXDp)
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Halo exterior pulsante/difuso
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(activeColor.copy(alpha = 0.35f))
                        )
                        // Núcleo sólido de la bolita
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                                .border(1.8.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Destello blanco central
                            Box(
                                modifier = Modifier
                                    .size(3.5.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            // 3. Rótulos de Rangos debajo del riel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                labels.forEachIndexed { i, label ->
                    val isCurrent = targetIndex == i
                    val labelColor = when {
                        isCurrent -> activeColor
                        i == 0 -> Color(0xFF64748B)
                        else -> tiers[i - 1].color.copy(alpha = 0.65f)
                    }

                    Text(
                        text = label,
                        fontSize = 7.5.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                        color = labelColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .clickable {
                                val newTier = if (i == 0) null else tiers[i - 1]
                                onSelectTier(newTier)
                            }
                            .padding(horizontal = 2.dp, vertical = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
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
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x2EFFFFFF))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(20.dp))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x18FFFFFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(0.85f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(15.dp),
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Cerrar", fontSize = 12.sp, color = Color.White)
                }

                if (associatedQuakesCount > 0) {
                    Button(
                        onClick = onFilterFaultQuakes,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = fault.color.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, fault.color),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1.35f).height(38.dp)
                    ) {
                        Text(
                            text = "⚡ Ver $associatedQuakesCount sismos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0x2EFFFFFF))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Detener",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
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
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xCC162133))
                .border(0.8.dp, Color(0x44FFFFFF), CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Acercar", tint = Color.White, modifier = Modifier.size(18.dp))
        }

        IconButton(
            onClick = onZoomOut,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xCC162133))
                .border(0.8.dp, Color(0x44FFFFFF), CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Alejar", tint = Color.White, modifier = Modifier.size(18.dp))
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

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x2EFFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x2EFFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar selección",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Cerrar / Deseleccionar explícito
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x18FFFFFF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cerrar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Botón Simular Onda
                    Button(
                        onClick = onToggleWaveSim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x333186FF)
                        ),
                        border = BorderStroke(1.dp, Color(0x663186FF)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFF60A5FA)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Simular",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Botón Ver Análisis
                    Button(
                        onClick = onViewDetail,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.25f)
                    ) {
                        Text(text = "Ver Análisis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
