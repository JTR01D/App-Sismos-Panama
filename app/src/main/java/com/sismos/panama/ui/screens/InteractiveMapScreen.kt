package com.sismos.panama.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

// Lagos y Embalses Hidrográficos Principales
val GatunLakePolygon = listOf(
    Pair(9.12, -79.92),
    Pair(9.20, -79.85),
    Pair(9.22, -79.76),
    Pair(9.14, -79.72),
    Pair(9.08, -79.82)
)

val BayanoLakePolygon = listOf(
    Pair(9.16, -78.98),
    Pair(9.22, -78.88),
    Pair(9.19, -78.75),
    Pair(9.12, -78.62),
    Pair(9.04, -78.72),
    Pair(9.08, -78.90)
)

val AlajuelaLakePolygon = listOf(
    Pair(9.23, -79.62),
    Pair(9.27, -79.57),
    Pair(9.22, -79.53),
    Pair(9.17, -79.58)
)

// Crestas y Relieve Topográfico de la Cordillera Central (Estilo Apple Maps Dark 3D Relief)
val MountainRidges = listOf(
    listOf(Pair(8.85, -82.85), Pair(8.82, -82.60), Pair(8.75, -82.35)),
    listOf(Pair(8.65, -81.90), Pair(8.58, -81.50), Pair(8.55, -81.10), Pair(8.60, -80.60)),
    listOf(Pair(9.30, -79.70), Pair(9.38, -79.30), Pair(9.32, -78.80), Pair(9.10, -78.20)),
    listOf(Pair(8.80, -77.80), Pair(8.50, -77.50), Pair(8.00, -77.30))
)

// Coordenadas del Encuadre Cartográfico Regional (Panamá, Costa Rica y Colombia en Alta Resolución)
const val MAP_MIN_LON = -86.0
const val MAP_MAX_LON = -75.0
const val MAP_MIN_LAT = 5.2
const val MAP_MAX_LAT = 11.2

// Rótulos de Cuerpos de Agua y Cuencas Oceánicas
data class WaterBodyLabel(val name: String, val lat: Double, val lon: Double)

val PanamaWaterLabels = listOf(
    WaterBodyLabel("MAR CARIBE", 9.80, -81.30),
    WaterBodyLabel("OCÉANO PACÍFICO", 6.85, -80.10),
    WaterBodyLabel("GOLFO DE CHIRIQUÍ", 7.85, -82.40),
    WaterBodyLabel("GOLFO DE PANAMÁ", 8.05, -79.20),
    WaterBodyLabel("GOLFO DE MONTIJO", 7.68, -81.25),
    WaterBodyLabel("BAHÍA DE PARITA", 8.28, -80.35),
    WaterBodyLabel("GOLFO DE URABÁ", 8.30, -76.90),
    WaterBodyLabel("GOLFO DULCE", 8.45, -83.40),
    WaterBodyLabel("GOLFO DE NICOYA", 9.75, -84.95)
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

// Centros Urbanos y Referencias Geográficas Detalladas (Estilo Apple Maps)
data class ReferenceCity(val name: String, val lat: Double, val lon: Double, val isKey: Boolean = false)

val SecondaryReferenceCities = listOf(
    ReferenceCity("Soná", 8.01, -81.53, isKey = true),
    ReferenceCity("Las Tablas", 7.76, -80.28, isKey = true),
    ReferenceCity("Chitré", 7.96, -80.43, isKey = true),
    ReferenceCity("Penonomé", 8.52, -80.36, isKey = true),
    ReferenceCity("Boquete", 8.78, -82.44),
    ReferenceCity("Puerto Armuelles", 8.28, -82.86),
    ReferenceCity("Pedasí", 7.53, -80.03),
    ReferenceCity("Colón", 9.36, -79.90, isKey = true),
    ReferenceCity("Changuinola", 9.43, -82.52),
    ReferenceCity("Chepo", 9.17, -79.10),
    ReferenceCity("Volcán", 8.77, -82.64),
    ReferenceCity("Santa Fe", 8.51, -81.08),
    ReferenceCity("La Palma", 8.52, -78.14),
    ReferenceCity("Jaqué", 7.52, -78.17),
    ReferenceCity("Metetí", 8.50, -78.00)
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
    ProvinceLabel("CHIRIQUÍ", 8.60, -82.72),
    ProvinceLabel("BOCAS DEL TORO", 9.18, -82.65),
    ProvinceLabel("VERAGUAS", 8.35, -81.35),
    ProvinceLabel("AZUERO", 7.62, -80.60),
    ProvinceLabel("COCLÉ", 8.62, -80.52),
    ProvinceLabel("COLÓN", 9.18, -80.05),
    ProvinceLabel("PANAMÁ", 9.10, -79.35),
    ProvinceLabel("PANAMÁ OESTE", 8.85, -79.85),
    ProvinceLabel("DARIÉN", 8.20, -77.90)
)

// Provincias de Costa Rica y Departamentos de Colombia (Contexto Cartográfico Completo)
val NeighborProvinceLabels = listOf(
    // Costa Rica (Las 7 Provincias Oficiales)
    ProvinceLabel("LIMÓN", 9.85, -83.25),
    ProvinceLabel("SAN JOSÉ", 9.75, -84.05),
    ProvinceLabel("PUNTARENAS", 8.90, -83.45),
    ProvinceLabel("CARTAGO", 9.80, -83.75),
    ProvinceLabel("HEREDIA", 10.25, -84.05),
    ProvinceLabel("ALAJUELA", 10.35, -84.45),
    ProvinceLabel("GUANACASTE", 10.45, -85.35),
    // Colombia (Departamentos Limítrofes y del Caribe / Pacífico)
    ProvinceLabel("CHOCÓ", 6.50, -77.00),
    ProvinceLabel("ANTIOQUIA", 7.20, -75.80),
    ProvinceLabel("CÓRDOBA", 8.40, -75.60),
    ProvinceLabel("SUCRE", 9.15, -75.10),
    ProvinceLabel("BOLÍVAR", 9.50, -74.90)
)

val NeighborProvinceBorders = listOf(
    // Costa Rica: Puntarenas - Limón (Cordillera de Talamanca)
    listOf(Pair(9.55, -82.60), Pair(9.40, -83.15), Pair(9.20, -83.45), Pair(8.90, -83.70)),
    // Costa Rica: Limón - Cartago
    listOf(Pair(9.95, -83.50), Pair(9.80, -83.60), Pair(9.65, -83.65)),
    // Costa Rica: Cartago - San José
    listOf(Pair(9.90, -83.95), Pair(9.75, -83.90), Pair(9.55, -83.85)),
    // Costa Rica: San José - Puntarenas
    listOf(Pair(9.55, -83.85), Pair(9.45, -84.05), Pair(9.60, -84.45)),
    // Costa Rica: Limón - Heredia (Hacia Sarapiquí)
    listOf(Pair(10.55, -83.70), Pair(10.25, -83.85), Pair(10.05, -83.95)),
    // Costa Rica: Heredia - Alajuela
    listOf(Pair(10.60, -84.10), Pair(10.20, -84.15), Pair(10.00, -84.20)),
    // Costa Rica: Alajuela - Puntarenas
    listOf(Pair(10.00, -84.50), Pair(9.85, -84.70)),
    // Costa Rica: Guanacaste - Alajuela / Puntarenas (Cordillera de Guanacaste)
    listOf(Pair(10.85, -85.00), Pair(10.40, -85.05), Pair(10.05, -85.00)),

    // Colombia: Chocó - Antioquia (Eje del Río Atrato / Urabá)
    listOf(Pair(8.40, -77.00), Pair(7.95, -76.85), Pair(7.30, -76.75), Pair(6.40, -76.60)),
    // Colombia: Antioquia - Córdoba
    listOf(Pair(8.65, -76.50), Pair(8.25, -76.30), Pair(8.00, -76.05)),
    // Colombia: Córdoba - Sucre
    listOf(Pair(9.30, -75.70), Pair(8.90, -75.40), Pair(8.50, -75.30))
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
        labelAnchor = Pair(9.14, -79.62)
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
        labelAnchor = Pair(8.91, -79.52)
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

// Cálculo geodésico de distancia ortodrómica en kilómetros (Fórmula de Haversine)
fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

// Distancia mínima desde una coordenada a cualquiera de los segmentos de una falla geológica
fun calculateDistanceToFault(lat: Double, lon: Double, fault: FaultLineInteractive): Double {
    return fault.points.minOfOrNull { pt -> calculateDistanceKm(lat, lon, pt.first, pt.second) } ?: 9999.0
}

@Composable
fun InteractiveMapScreen(
    viewModel: SeismicViewModel,
    onBack: () -> Unit,
    onEarthquakeSelected: (Earthquake) -> Unit,
    initialWaveSimulation: Boolean = false,
    selectFirst: Boolean = false,
    initialFaultId: String? = null,
    tourTarget: String? = null
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val allEarthquakes = state.filteredEarthquakes

    // ==========================================
    // INTEGRACIÓN GPS TIEMPO REAL (APPLE BLUE PUCK)
    // ==========================================
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            try {
                val lastGps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val best = listOfNotNull(lastGps, lastNet).maxByOrNull { it.time }
                if (best != null) userLocation = best
            } catch (_: SecurityException) {}
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    userLocation = location
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            try {
                val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val best = listOfNotNull(lastGps, lastNet).maxByOrNull { it.time }
                if (best != null) userLocation = best

                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 2f, locationListener)
                }
                if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 5f, locationListener)
                }
            } catch (_: SecurityException) {}

            onDispose {
                try {
                    locationManager?.removeUpdates(locationListener)
                } catch (_: SecurityException) {}
            }
        }
    }

    // Filtros interactivos de magnitud y peligro sísmico en mapa
    var selectedHazardTier by remember { mutableStateOf<MagnitudeHazardTier?>(null) }
    var showOnlySwarm by remember { mutableStateOf(false) }
    var dismissEmptyTierNoticeFor by remember { mutableStateOf<MagnitudeHazardTier?>(null) }
    var isHazardScaleCollapsed by remember { mutableStateOf(true) }

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

    val displayEarthquakes = remember(allEarthquakes, selectedHazardTier, showOnlySwarm) {
        allEarthquakes.filter { eq ->
            val passesTier = selectedHazardTier == null ||
                    (eq.magnitude >= selectedHazardTier!!.minMag && eq.magnitude <= selectedHazardTier!!.maxMag)
            val passesSwarm = !showOnlySwarm || (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
            passesTier && passesSwarm
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

    LaunchedEffect(initialFaultId) {
        if (initialFaultId != null) {
            selectedFault = InteractivePanamaFaults.find { it.id.equals(initialFaultId, ignoreCase = true) }
        }
    }

    // Canvas Pan & Zoom
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isSwarmFocused by remember { mutableStateOf(false) }
    var isSwarmBannerDismissed by remember { mutableStateOf(false) }
    var canvasSizeState by remember { mutableStateOf(Size.Zero) }

    // Función: Centrar en mi ubicación GPS (Estilo Apple Maps)
    val onCenterUser: () -> Unit = {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            userLocation?.let { loc ->
                val minLon = MAP_MIN_LON
                val maxLon = MAP_MAX_LON
                val minLat = MAP_MIN_LAT
                val maxLat = MAP_MAX_LAT
                val deltaLon = (maxLon - minLon).toFloat()
                val deltaLat = (maxLat - minLat).toFloat()

                val paddingX = 20f
                val usableWidth = (canvasSizeState.width - (paddingX * 2f)).coerceAtLeast(100f)
                val pxPerDegree = usableWidth / deltaLon
                val mapHeight = deltaLat * pxPerDegree

                val startX = paddingX
                val startY = (canvasSizeState.height - mapHeight) / 2f

                val rawX = startX + ((loc.longitude - minLon).toFloat() * pxPerDegree)
                val rawY = startY + ((maxLat - loc.latitude).toFloat() * pxPerDegree)

                val centerX = canvasSizeState.width / 2f
                val centerY = canvasSizeState.height / 2f

                val targetScale = 3.6f
                scale = targetScale
                offset = Offset((centerX - rawX) * targetScale, (centerY - rawY) * targetScale)
            }
        }
    }

    // Control Automático del Recorrido Guiado / Tour Cartográfico
    LaunchedEffect(tourTarget, canvasSizeState) {
        if (canvasSizeState.width > 0f && tourTarget != null) {
            val minLon = MAP_MIN_LON
            val maxLon = MAP_MAX_LON
            val minLat = MAP_MIN_LAT
            val maxLat = MAP_MAX_LAT
            val deltaLon = (maxLon - minLon).toFloat()
            val deltaLat = (maxLat - minLat).toFloat()

            val paddingX = 20f
            val usableWidth = (canvasSizeState.width - (paddingX * 2f)).coerceAtLeast(100f)
            val pxPerDegree = usableWidth / deltaLon
            val mapHeight = deltaLat * pxPerDegree

            val startX = paddingX
            val startY = (canvasSizeState.height - mapHeight) / 2f
            val centerX = canvasSizeState.width / 2f
            val centerY = canvasSizeState.height / 2f

            fun centerOn(targetLat: Double, targetLon: Double, targetScale: Float) {
                val rawX = startX + ((targetLon - minLon).toFloat() * pxPerDegree)
                val rawY = startY + ((maxLat - targetLat).toFloat() * pxPerDegree)
                scale = targetScale
                offset = Offset((centerX - rawX) * targetScale, (centerY - rawY) * targetScale)
            }

            when (tourTarget.lowercase()) {
                "overview" -> {
                    scale = 1.0f
                    offset = Offset.Zero
                }
                "canal" -> centerOn(9.015, -79.610, 5.8f)
                "metro" -> centerOn(9.020, -79.490, 3.6f)
                "costarica", "cr" -> centerOn(9.900, -83.700, 2.5f)
                "guapiles" -> centerOn(10.216, -83.784, 4.2f)
                "colombia", "col" -> centerOn(7.900, -76.800, 2.5f)
                "swarm", "azuero" -> centerOn(7.450, -80.700, 2.5f)
            }
        }
    }

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
        containerColor = Color(0xFF070B14),
        topBar = {
            MapTopBar(
                onBack = onBack,
                earthquakeCount = displayEarthquakes.size,
                totalCount = allEarthquakes.size,
                showOnlySwarm = showOnlySwarm,
                onToggleSwarm = {
                    showOnlySwarm = !showOnlySwarm
                    if (showOnlySwarm) {
                        isSwarmBannerDismissed = false
                    }
                },
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
                    selectedHazardTier = null
                    dismissEmptyTierNoticeFor = null
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

            // Lienzo interactivo avanzado estilo Apple Maps Dark
            AdvancedTectonicMapCanvas(
                earthquakes = displayEarthquakes,
                latestEarthquakeId = latestQuake?.id,
                selectedEarthquake = selectedEq,
                selectedHazardTier = selectedHazardTier,
                selectedFault = selectedFault,
                userLocation = userLocation,
                showFaults = showFaults,
                showWaveSimulation = showWaveSimulation,
                simulationSeconds = simulationTimeSeconds,
                scale = scale,
                offset = offset,
                tourTarget = tourTarget,
                onCanvasSizeChanged = { canvasSizeState = it },
                onTransform = { pan, zoom ->
                    val newScale = (scale * zoom).coerceIn(0.85f, 10.0f)
                    scale = newScale
                    val maxPanX = (canvasSizeState.width * (newScale - 0.45f)).coerceAtLeast(canvasSizeState.width * 0.35f)
                    val maxPanY = (canvasSizeState.height * (newScale - 0.45f)).coerceAtLeast(canvasSizeState.height * 0.35f)
                    val targetX = offset.x + pan.x
                    val targetY = offset.y + pan.y
                    offset = Offset(
                        targetX.coerceIn(-maxPanX, maxPanX),
                        targetY.coerceIn(-maxPanY, maxPanY)
                    )
                },
                onAutoZoomSwarm = { targetScale, targetOffset ->
                    scale = targetScale
                    offset = targetOffset
                    isSwarmFocused = true
                },
                onSelectEarthquake = { eq ->
                    selectedEq = eq
                    selectedFault = null
                },
                onSelectFault = { fault ->
                    selectedFault = fault
                    selectedEq = null
                },
                onTapBlank = {
                    selectedEq = null
                    selectedFault = null
                    showWaveSimulation = false
                }
            )

            // Controles flotantes Apple Liquid Glass (Zoom + GPS + Compás)
            FloatingAppleMapControls(
                hasUserLocation = userLocation != null,
                onZoomIn = {
                    val newScale = (scale * 1.35f).coerceIn(0.85f, 10.0f)
                    scale = newScale
                },
                onZoomOut = {
                    val newScale = (scale / 1.35f).coerceIn(0.85f, 10.0f)
                    scale = newScale
                    val maxPanX = (canvasSizeState.width * (newScale - 0.45f)).coerceAtLeast(canvasSizeState.width * 0.35f)
                    val maxPanY = (canvasSizeState.height * (newScale - 0.45f)).coerceAtLeast(canvasSizeState.height * 0.35f)
                    offset = Offset(
                        offset.x.coerceIn(-maxPanX, maxPanX),
                        offset.y.coerceIn(-maxPanY, maxPanY)
                    )
                },
                onCenterUser = onCenterUser,
                onResetPanama = {
                    scale = 1f
                    offset = Offset.Zero
                    selectedFault = null
                    selectedEq = null
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )

            // HUD Superior de Telemetría GPS de Proximidad
            val closestFault = remember(userLocation) {
                userLocation?.let { loc ->
                    InteractivePanamaFaults.map { it to calculateDistanceToFault(loc.latitude, loc.longitude, it) }
                        .minByOrNull { it.second }
                }
            }
            val closestEq = remember(userLocation, displayEarthquakes) {
                userLocation?.let { loc ->
                    displayEarthquakes.map { it to calculateDistanceKm(loc.latitude, loc.longitude, it.latitude, it.longitude) }
                        .minByOrNull { it.second }
                }
            }

            val swarmQuakes = remember(displayEarthquakes) {
                displayEarthquakes.filter { it.latitude in 7.0..8.2 && it.longitude in -82.0..-80.2 }
            }
            val latestSwarmQuake = remember(swarmQuakes, displayEarthquakes) {
                swarmQuakes.maxByOrNull { it.timeMillis } ?: displayEarthquakes.maxByOrNull { it.timeMillis }
            }
            val swarmCount = swarmQuakes.size

            // Contenedor Vertical de Notificaciones Superiores (GPS + Enjambre)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // HUD Superior de Telemetría GPS de Proximidad
                if (!showWaveSimulation) {
                    Surface(
                        onClick = onCenterUser,
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xEB0C162A),
                        border = BorderStroke(1.dp, if (userLocation != null) Color(0x66007AFF) else Color(0x3338BDF8))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (userLocation != null) Color(0xFF007AFF) else Color(0xFF94A3B8))
                            )
                            if (userLocation != null) {
                                Text(
                                    text = "📍 A ${closestFault?.second?.toInt() ?: "--"} km de ${closestFault?.first?.shortBadge ?: ""} • A ${closestEq?.second?.toInt() ?: "--"} km de sismo cercano",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "📍 Toca para activar tu ubicación en tiempo real",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF60A5FA),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Banner flotante de Enfoque Automático en Enjambre
                if (isSwarmFocused && swarmCount >= 5 && !showWaveSimulation && !isSwarmBannerDismissed) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xF20B1324),
                        border = BorderStroke(1.2.dp, Color(0xFFFFB300))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 4.dp, end = 6.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB300))
                                )
                                Text(
                                    text = "⚡ Enjambre Azuero ($swarmCount) • M ${String.format(java.util.Locale.US, "%.1f", latestSwarmQuake?.magnitude ?: 0.0)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (latestSwarmQuake != null) {
                                    Surface(
                                        onClick = { selectedEq = latestSwarmQuake },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x33FFB300),
                                        border = BorderStroke(0.6.dp, Color(0x66FFB300))
                                    ) {
                                        Text(
                                            text = "📍 Ver #1",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        isSwarmBannerDismissed = true
                                        isSwarmFocused = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar aviso de enjambre",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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

            // Escala de Peligro Sísmico como selector interactivo por magnitud
            AnimatedVisibility(
                visible = !showWaveSimulation && selectedEq == null && selectedFault == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 78.dp
                    )
            ) {
                MapLegendBadge(
                    selectedTier = selectedHazardTier,
                    onSelectTier = { tier ->
                        selectedHazardTier = tier
                        dismissEmptyTierNoticeFor = null
                    },
                    quakeCounts = hazardTierCounts,
                    totalQuakesCount = baseQuakesForCounts.size,
                    filteredQuakesCount = displayEarthquakes.size,
                    isCollapsed = isHazardScaleCollapsed,
                    onToggleCollapse = { isHazardScaleCollapsed = !isHazardScaleCollapsed },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notificación flotante si el rango de magnitud seleccionado no tiene sismos
            if (selectedHazardTier != null && displayEarthquakes.isEmpty() && dismissEmptyTierNoticeFor != selectedHazardTier) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF50B1324),
                    border = BorderStroke(1.2.dp, selectedHazardTier?.color ?: Color(0xFF38BDF8)),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(selectedHazardTier?.color ?: Color(0xFF38BDF8))
                                )
                                Text(
                                    text = "Sin sismos en rango ${selectedHazardTier?.label ?: ""}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { dismissEmptyTierNoticeFor = selectedHazardTier },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar aviso",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "No se registran eventos activos con magnitud ${selectedHazardTier?.label ?: ""}.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    selectedHazardTier = null
                                    dismissEmptyTierNoticeFor = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = selectedHazardTier?.color ?: Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    "Ver Todos los Sismos",
                                    color = Color(0xFF091020),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Tarjeta inferior interactiva: Falla seleccionada (Estilo Apple Sheet)
            AnimatedVisibility(
                visible = selectedFault != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 82.dp)
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
                    val userDistToFault = userLocation?.let { loc ->
                        calculateDistanceToFault(loc.latitude, loc.longitude, fault)
                    }
                    SelectedFaultCard(
                        fault = fault,
                        associatedQuakesCount = faultQuakes.size,
                        userDistanceKm = userDistToFault,
                        onDismiss = { selectedFault = null },
                        onFilterFaultQuakes = {
                            showOnlySwarm = fault.id == "sona_tonosi"
                        }
                    )
                }
            }

            // Tarjeta inferior interactiva: Sismo seleccionado (Estilo Apple Sheet)
            AnimatedVisibility(
                visible = selectedEq != null && selectedFault == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 82.dp)
            ) {
                selectedEq?.let { eq ->
                    val userDistToEq = userLocation?.let { loc ->
                        calculateDistanceKm(loc.latitude, loc.longitude, eq.latitude, eq.longitude)
                    }
                    SelectedEarthquakeCard(
                        earthquake = eq,
                        isSimulatingWaves = showWaveSimulation,
                        userDistanceKm = userDistToEq,
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
    userLocation: Location? = null,
    showFaults: Boolean,
    showWaveSimulation: Boolean,
    simulationSeconds: Float,
    scale: Float,
    offset: Offset,
    tourTarget: String? = null,
    onCanvasSizeChanged: (Size) -> Unit,
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
    val minLon = MAP_MIN_LON
    val maxLon = MAP_MAX_LON
    val minLat = MAP_MIN_LAT
    val maxLat = MAP_MAX_LAT

    val deltaLon = (maxLon - minLon).toFloat()
    val deltaLat = (maxLat - minLat).toFloat()

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var hasAutoZoomedToSwarm by remember { mutableStateOf(false) }

    LaunchedEffect(canvasSize, earthquakes) {
        if (tourTarget != null) {
            hasAutoZoomedToSwarm = true
            return@LaunchedEffect
        }
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

                    // Tocar espacio libre en el mapa deselecciona
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
            onCanvasSizeChanged(size)

            // Registro de Bounding Boxes de Rótulos para Evitar Superposiciones (Smart Anti-Collision Engine)
            val occupiedLabelRects = mutableListOf<android.graphics.RectF>()

            fun canDrawLabel(rect: android.graphics.RectF): Boolean {
                for (existing in occupiedLabelRects) {
                    if (android.graphics.RectF.intersects(existing, rect)) {
                        return false
                    }
                }
                occupiedLabelRects.add(rect)
                return true
            }

            // Prioridad 1: Reservar el espacio de los rótulos de fallas geológicas para que nunca queden tapados
            if (showFaults) {
                InteractivePanamaFaults.forEach { fault ->
                    val bp = project(fault.labelAnchor.first, fault.labelAnchor.second, size)
                    val halfW = (fault.shortBadge.length * 4.2f * scale.coerceIn(0.8f, 1.4f)).coerceAtLeast(35f)
                    val halfH = 14f * scale.coerceIn(0.8f, 1.4f)
                    occupiedLabelRects.add(android.graphics.RectF(bp.x - halfW, bp.y - halfH, bp.x + halfW, bp.y + halfH))
                }
            }

            // 1. Fondo oceánico profundo táctico (Restaurado a solicitud del usuario)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF060A12), Color(0xFF090E1A), Color(0xFF04070D))
                )
            )

            // Cuadrícula batimétrica y coordenadas (Graticule Grid)
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
                color = android.graphics.Color.argb(90, 56, 189, 248)
                textSize = (9f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            drawContext.canvas.nativeCanvas.drawText("ISOBATA 200m • MARGEN CONTINENTAL", shelfLabelPos.x, shelfLabelPos.y + 14f, shelfPaint)

            // 3. Rótulos Marítimos y Cuencas Oceánicas
            val oceanTextPaint = Paint().apply {
                color = android.graphics.Color.argb(85, 125, 175, 240)
                textSize = (13f * scale.coerceIn(0.8f, 2.0f)).coerceAtLeast(10f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.25f
            }
            val gulfTextPaint = Paint().apply {
                color = android.graphics.Color.argb(75, 125, 175, 240)
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
                color = android.graphics.Color.argb(125, 186, 210, 240)
                textSize = (10f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(8.5f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.12f
            }
            val velocityPaint = Paint().apply {
                color = android.graphics.Color.argb(190, 0, 229, 255)
                textSize = (9f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(7.5f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            MajorTectonicPlates.forEach { plate ->
                val p = project(plate.anchorLat, plate.anchorLon, size)
                val vScale = scale.coerceIn(0.8f, 1.6f)
                val arrowEnd = Offset(p.x + plate.vectorDx * vScale, p.y + plate.vectorDy * vScale)

                drawLine(
                    color = Color(0x6600E5FF),
                    start = p,
                    end = arrowEnd,
                    strokeWidth = 2.0f * scale.coerceIn(0.8f, 1.6f)
                )
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

                drawContext.canvas.nativeCanvas.drawText(plate.plateName, p.x, p.y - 12f, platePaint)
                drawContext.canvas.nativeCanvas.drawText(plate.velocityLabel, p.x, p.y + 1f, velocityPaint)
            }

            // 4B. Países Vecinos: Costa Rica y Colombia (Tono de fondo geográfico oscuro/oliva)
            fun drawNeighborCountry(polygon: List<Pair<Double, Double>>) {
                val cPath = Path()
                polygon.forEachIndexed { i, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (i == 0) cPath.moveTo(p.x, p.y) else cPath.lineTo(p.x, p.y)
                }
                cPath.close()
                // Tono profundo de selva oliva y esmeralda (diferenciado de Panamá, estilo Apple Maps)
                drawPath(
                    path = cPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF134A38), Color(0xFF175742), Color(0xFF103E2F))
                    )
                )
                drawPath(
                    path = cPath,
                    color = Color(0xFF1C634A),
                    style = Stroke(width = 1.4f * scale.coerceIn(0.8f, 1.8f))
                )
            }
            drawNeighborCountry(HighResCostaRicaCoast)
            drawNeighborCountry(HighResColombiaCoast)

            // Líneas de Frontera Internacional Punteadas
            fun drawBorder(points: List<Pair<Double, Double>>) {
                val bPath = Path()
                points.forEachIndexed { i, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (i == 0) bPath.moveTo(p.x, p.y) else bPath.lineTo(p.x, p.y)
                }
                drawPath(
                    path = bPath,
                    color = Color(0xCCFFD54F),
                    style = Stroke(
                        width = 1.4f * scale.coerceIn(0.8f, 1.8f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                )
            }
            drawBorder(CostaRicaBorderLine)
            drawBorder(ColombiaBorderLine)

            // Rótulos de Países Vecinos (Estilo Apple Maps)
            val neighborCountryPaint = Paint().apply {
                color = android.graphics.Color.argb(165, 220, 238, 228)
                textSize = (13.5f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(10.5f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.35f
                setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(180, 0, 0, 0))
            }
            val crPos = project(9.85, -84.10, size)
            drawContext.canvas.nativeCanvas.drawText("COSTA RICA", crPos.x, crPos.y, neighborCountryPaint)

            val colPos = project(7.00, -75.80, size)
            drawContext.canvas.nativeCanvas.drawText("COLOMBIA", colPos.x, colPos.y, neighborCountryPaint)

            // Poblados de Costa Rica y Colombia (Contexto Cartográfico Estilo Apple Maps con Anti-Colisión)
            val neighborTownPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (8.8f * scale.coerceIn(0.8f, 1.4f)).coerceAtLeast(7.8f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(3.5f, 0f, 1f, android.graphics.Color.argb(220, 0, 0, 0))
            }
            NeighborCountryTowns.forEach { town ->
                if (scale >= town.minScale) {
                    val tPos = project(town.lat, town.lon, size)
                    val tR = 2.4f * scale.coerceIn(0.8f, 1.3f)
                    val tW = neighborTownPaint.measureText(town.name)
                    val tRect = android.graphics.RectF(
                        tPos.x - tR - 2f,
                        tPos.y - 10f * scale.coerceIn(0.8f, 1.4f),
                        tPos.x + tR + tW + 4f,
                        tPos.y + tR + 2f
                    )
                    // Guápiles siempre tiene prioridad garantizada de renderizado
                    if (town.name == "Guápiles" || canDrawLabel(tRect)) {
                        drawCircle(color = Color(0xDD080E1C), radius = tR + 1.2f, center = tPos)
                        drawCircle(
                            color = if (town.name == "Guápiles" || town.name == "San José" || town.name == "Turbo" || town.name == "Apartadó") Color.White else Color(0xFFE2E8F0),
                            radius = tR,
                            center = tPos
                        )
                        drawContext.canvas.nativeCanvas.drawText(town.name, tPos.x + tR + 4f, tPos.y + 2.5f, neighborTownPaint)
                    }
                }
            }

            // 5. Masa Continental de Panamá de Alta Resolución (Tono Verde Esmeralda Tropical)
            val panamaPath = Path()
            HighResPanamaCoast.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) panamaPath.moveTo(p.x, p.y) else panamaPath.lineTo(p.x, p.y)
            }
            panamaPath.close()

            // Relleno verde bosque tropical vibrante de Panamá
            drawPath(
                path = panamaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F7055), Color(0xFF127B5E), Color(0xFF0D5E47))
                )
            )

            // Borde costero nítido de Panamá
            drawPath(
                path = panamaPath,
                color = Color(0xFF199371),
                style = Stroke(width = 1.6f * scale.coerceIn(0.8f, 2.0f))
            )

            // Rótulo Nacional Oficial PANAMÁ (Estilo Apple Maps Rosa Salmón)
            if (scale < 2.8f) {
                val panamaCountryPaint = Paint().apply {
                    color = android.graphics.Color.argb(220, 226, 138, 153)
                    textSize = (13.5f * scale.coerceIn(0.9f, 2.0f)).coerceAtLeast(11f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.28f
                }
                val natPos = project(8.52, -80.35, size)
                drawContext.canvas.nativeCanvas.drawText("PANAMÁ", natPos.x, natPos.y, panamaCountryPaint)
            }

            // Rótulo Geográfico Istmo de Panamá (Estilo Apple Maps)
            if (scale in 1.8f..4.5f) {
                val isthmusPaint = Paint().apply {
                    color = android.graphics.Color.argb(80, 255, 255, 255)
                    textSize = (11f * scale.coerceIn(1.0f, 2.0f)).coerceAtLeast(10f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.40f
                }
                val istPos = project(9.30, -79.35, size)
                drawContext.canvas.nativeCanvas.drawText("ISTMO DE PANAMÁ", istPos.x, istPos.y, isthmusPaint)
            }

            // 6. Relieve y Crestas Topográficas de la Cordillera Central (Sombreado Hillshade)
            MountainRidges.forEach { ridge ->
                val rPath = Path()
                ridge.forEachIndexed { idx, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (idx == 0) rPath.moveTo(p.x, p.y) else rPath.lineTo(p.x, p.y)
                }
                drawPath(
                    path = rPath,
                    color = Color(0x35084030),
                    style = Stroke(width = 3.5f * scale.coerceIn(0.8f, 2.0f))
                )
                drawPath(
                    path = rPath,
                    color = Color(0x551BB287),
                    style = Stroke(
                        width = 1.2f * scale.coerceIn(0.8f, 2.0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )
            }

            // 7. Manchas Urbanas / Huellas Metropolitanas (Estilo Apple/Google Maps Dark Slate)
            fun drawUrbanArea(polygon: List<Pair<Double, Double>>) {
                val uPath = Path()
                polygon.forEachIndexed { idx, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (idx == 0) uPath.moveTo(p.x, p.y) else uPath.lineTo(p.x, p.y)
                }
                uPath.close()
                drawPath(path = uPath, color = Color(0xEE1E2838))
                drawPath(path = uPath, color = Color(0x3388A0C0), style = Stroke(width = 1.0f * scale))
            }
            drawUrbanArea(PanamaCityMetropolitanFootprint)
            drawUrbanArea(ArraijanUrbanFootprint)
            drawUrbanArea(ChorreraUrbanFootprint)
            drawUrbanArea(ColonCityUrbanFootprint)
            drawUrbanArea(listOf(Pair(8.41, -82.45), Pair(8.46, -82.44), Pair(8.45, -82.41), Pair(8.42, -82.42))) // David
            drawUrbanArea(listOf(Pair(7.94, -80.45), Pair(7.98, -80.44), Pair(7.97, -80.41), Pair(7.93, -80.42))) // Chitré

            // 8. Archipiélagos e Islas de Alta Resolución
            fun drawIsland(points: List<Pair<Double, Double>>) {
                val iPath = Path()
                points.forEachIndexed { i, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (i == 0) iPath.moveTo(p.x, p.y) else iPath.lineTo(p.x, p.y)
                }
                iPath.close()
                drawPath(path = iPath, color = Color(0xFF0F7055))
                drawPath(path = iPath, color = Color(0xFF199371), style = Stroke(width = 1.4f * scale))
            }
            drawIsland(HighResCoibaIsland)
            drawIsland(HighResIslaDelRey)
            drawIsland(HighResCebacoIsland)
            drawIsland(BocasDelToroIslands)
            drawIsland(TabogaIsland)

            // 9. Lagos y Embalses de Alta Resolución (Lago Gatún Dendrítico 111 pts, Alajuela, Bayano)
            fun drawLake(polygon: List<Pair<Double, Double>>) {
                val lPath = Path()
                polygon.forEachIndexed { i, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (i == 0) lPath.moveTo(p.x, p.y) else lPath.lineTo(p.x, p.y)
                }
                lPath.close()
                drawPath(path = lPath, color = Color(0xFF070E1A))
                drawPath(path = lPath, color = Color(0x6638BDF8), style = Stroke(width = 1.1f * scale))
            }
            drawLake(HighResGatunLake)
            // Isla Barro Colorado (Monumento Natural en el centro del Lago Gatún)
            drawIsland(BarroColoradoIsland)
            drawLake(HighResAlajuelaLake)
            drawLake(BayanoLakePolygon)

            // Conector Fluvial Río Chagres (Represa Alajuela a Gamboa)
            val chagresPath = Path()
            RioChagresConnector.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) chagresPath.moveTo(p.x, p.y) else chagresPath.lineTo(p.x, p.y)
            }
            drawPath(
                path = chagresPath,
                color = Color(0xFF070E1A),
                style = Stroke(width = 2.4f * scale.coerceIn(0.8f, 2.2f), cap = StrokeCap.Round)
            )

            // =====================================================================
            // 9B. CANAL DE PANAMÁ CONTINUO (Vía Navegable Interoceánica Océano a Océano)
            // =====================================================================
            val canalPath = Path()
            PanamaCanalWaterway.forEachIndexed { i, pt ->
                val p = project(pt.first, pt.second, size)
                if (i == 0) canalPath.moveTo(p.x, p.y) else canalPath.lineTo(p.x, p.y)
            }
            // Ribera / orilla acuática del canal
            drawPath(
                path = canalPath,
                color = Color(0x6638BDF8),
                style = Stroke(
                    width = 5.2f * scale.coerceIn(0.8f, 2.4f),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            // Cauce de agua navegable
            drawPath(
                path = canalPath,
                color = Color(0xFF070E1A),
                style = Stroke(
                    width = 3.6f * scale.coerceIn(0.8f, 2.4f),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Rótulos Acuáticos de Lagos y Canal (Estilo Apple Maps)
            if (scale >= 2.0f) {
                val hydroLabelPaint = Paint().apply {
                    color = android.graphics.Color.argb(195, 95, 170, 240)
                    textSize = (9.5f * scale.coerceIn(0.8f, 1.5f)).coerceAtLeast(8.5f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    textAlign = Paint.Align.CENTER
                }
                val gatunPos = project(9.16, -79.88, size)
                drawContext.canvas.nativeCanvas.drawText("Lago Gatún", gatunPos.x, gatunPos.y, hydroLabelPaint)

                val alajuelaPos = project(9.22, -79.62, size)
                drawContext.canvas.nativeCanvas.drawText("Lago Alajuela", alajuelaPos.x, alajuelaPos.y, hydroLabelPaint)

                if (scale >= 3.2f) {
                    val bciPos = project(9.15, -79.84, size)
                    drawContext.canvas.nativeCanvas.drawText("Isla Barro Colorado", bciPos.x, bciPos.y, hydroLabelPaint)
                }

                if (scale >= 3.0f) {
                    val canalPos = project(9.08, -79.70, size)
                    drawContext.canvas.nativeCanvas.drawText("Canal de Panamá", canalPos.x, canalPos.y, hydroLabelPaint)
                }
            }

            // 10. Red Vial y Carreteras Principales (Carretera Panamericana Ruta 1, Ruta 2, Ruta 9 y Corredores)
            fun drawHighway(waypoints: List<Pair<Double, Double>>, isPrimary: Boolean = true) {
                val hPath = Path()
                waypoints.forEachIndexed { i, pt ->
                    val p = project(pt.first, pt.second, size)
                    if (i == 0) hPath.moveTo(p.x, p.y) else hPath.lineTo(p.x, p.y)
                }
                drawPath(
                    path = hPath,
                    color = Color(0xFF111E2D),
                    style = Stroke(width = (if (isPrimary) 3.6f else 2.6f) * scale.coerceIn(0.8f, 2.0f))
                )
                drawPath(
                    path = hPath,
                    color = Color(0xFF7F9BB8),
                    style = Stroke(width = (if (isPrimary) 2.0f else 1.4f) * scale.coerceIn(0.8f, 2.0f))
                )
            }
            drawHighway(PanAmericanHighwayRoute1, isPrimary = true)
            drawHighway(ViaCentenarioHighway, isPrimary = true)
            drawHighway(CorredorSurHighway, isPrimary = false)
            drawHighway(CorredorNorteHighway, isPrimary = false)
            drawHighway(AutopistaArraijanChorrera, isPrimary = false)
            drawHighway(ColonHighwayRoute9, isPrimary = false)
            drawHighway(AzueroHighwayRoute2, isPrimary = false)
            // Red Vial de Costa Rica y Colombia
            drawHighway(CostaRicaRoute32Highway, isPrimary = true)
            drawHighway(CostaRicaRoute2Highway, isPrimary = true)
            drawHighway(ColombiaRoute90Highway, isPrimary = true)
            drawHighway(ColombiaRoute62Highway, isPrimary = true)

            // Escudos de Rutas Viales (Solo visibles con zoom >= 2.5 para evitar saturación)
            if (scale >= 2.5f) {
                val shieldPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                }
                val shieldBorderPaint = Paint().apply {
                    color = android.graphics.Color.argb(180, 20, 30, 45)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.0f
                }
                val shieldTextPaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = (8.5f * scale.coerceIn(0.8f, 1.6f)).coerceAtLeast(7.5f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                fun drawRouteShield(routeNum: String, lat: Double, lon: Double) {
                    val sPos = project(lat, lon, size)
                    val sW = 14f * scale.coerceIn(0.8f, 1.4f)
                    val sH = 12f * scale.coerceIn(0.8f, 1.4f)
                    val sRect = android.graphics.RectF(sPos.x - sW / 2f, sPos.y - sH / 2f, sPos.x + sW / 2f, sPos.y + sH / 2f)
                    drawContext.canvas.nativeCanvas.drawRoundRect(sRect, 3.5f, 3.5f, shieldPaint)
                    drawContext.canvas.nativeCanvas.drawRoundRect(sRect, 3.5f, 3.5f, shieldBorderPaint)
                    drawContext.canvas.nativeCanvas.drawText(routeNum, sPos.x, sPos.y + (sH * 0.32f), shieldTextPaint)
                }
                // Panamá
                drawRouteShield("1", 8.42, -80.18)
                drawRouteShield("1", 8.94, -79.62)
                drawRouteShield("3", 9.03, -79.62)
                drawRouteShield("4", 8.99, -79.62)
                drawRouteShield("6", 9.04, -79.52)
                drawRouteShield("1", 8.44, -82.35)
                drawRouteShield("2", 7.82, -80.31)
                drawRouteShield("9", 9.18, -79.72)
                drawRouteShield("852", 8.97, -79.58)
                // Costa Rica (Ruta 32 San José - Guápiles - Limón y Ruta 2)
                drawRouteShield("32", 10.15, -83.75)
                drawRouteShield("2", 8.65, -82.95)
                // Colombia (Ruta 90 Carretera al Mar y Ruta 62 Urabá-Medellín)
                drawRouteShield("90", 8.42, -76.78)
                drawRouteShield("62", 7.65, -76.68)
            }

            // 11. Hitos y Distintivos Cartográficos con Control Inteligente de Zoom (LOD / Anti-Colisión)
            // 11. Hitos y Distintivos Cartográficos (Estilo Apple Maps Pins Circulares + Tipografía Nítida)
            val landmarkTextPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (8.5f * scale.coerceIn(0.8f, 1.4f)).coerceAtLeast(7.5f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(3.5f, 0f, 1f, android.graphics.Color.argb(220, 0, 0, 0))
            }

            AppleMapLandmarks.forEach { landmark ->
                // Smart Zoom LOD: Si el zoom actual es inferior al mínimo del hito, NO se dibuja (evita el amontonamiento)
                if (scale < landmark.minScale) return@forEach

                val lPos = project(landmark.lat, landmark.lon, size)
                val pinRadius = 6.5f * scale.coerceIn(0.8f, 1.3f)
                val labelText = if (landmark.extra.isNotEmpty()) "${landmark.name} (${landmark.extra})" else landmark.name
                val textW = landmarkTextPaint.measureText(labelText)
                val textH = 14f * scale.coerceIn(0.8f, 1.3f)
                val lRect = android.graphics.RectF(
                    lPos.x - pinRadius - 3f,
                    lPos.y - pinRadius - 3f,
                    lPos.x + pinRadius + textW + 8f,
                    lPos.y + pinRadius + 3f
                )
                // Anti-colisión: Si interseca con otro elemento prioritario ya dibujado, omitir para mantener el mapa limpio
                if (!canDrawLabel(lRect)) return@forEach

                val pinColor = when (landmark.type) {
                    LandmarkType.NATIONAL_PARK -> Color(0xFF1B6B4C)
                    LandmarkType.MOUNTAIN_PEAK -> Color(0xFF795548)
                    LandmarkType.AIRPORT -> Color(0xFF1976D2)
                    LandmarkType.CANAL_LOCK -> Color(0xFF0277BD)
                    LandmarkType.LANDMARK -> Color(0xFF5E35B1)
                }
                drawCircle(color = pinColor, radius = pinRadius, center = lPos)
                drawCircle(color = Color.White, radius = pinRadius, center = lPos, style = Stroke(width = 1.0f))

                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    lPos.x + pinRadius + 4f,
                    lPos.y + (pinRadius * 0.45f),
                    landmarkTextPaint
                )
            }

            // 11B. Rótulos de Barrios y Vecindarios (Estilo Google Maps)
            val neighborhoodPaint = Paint().apply {
                color = android.graphics.Color.argb(200, 203, 213, 225)
                textSize = (9.5f * scale.coerceIn(0.8f, 1.4f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            PanamaNeighborhoods.forEach { nb ->
                if (scale >= nb.minScale) {
                    val nPos = project(nb.lat, nb.lon, size)
                    val nW = neighborhoodPaint.measureText(nb.name)
                    val nRect = android.graphics.RectF(
                        nPos.x - nW / 2f - 4f,
                        nPos.y - 12f * scale.coerceIn(0.8f, 1.4f),
                        nPos.x + nW / 2f + 4f,
                        nPos.y + 4f
                    )
                    if (canDrawLabel(nRect)) {
                        drawContext.canvas.nativeCanvas.drawText(nb.name, nPos.x, nPos.y, neighborhoodPaint)
                    }
                }
            }

            // 12. Límites Provinciales Internos (Panamá, Costa Rica y Colombia)
            fun drawInternalBorder(borderList: List<List<Pair<Double, Double>>>, strokeColor: Color) {
                borderList.forEach { borderLine ->
                    val bPath = Path()
                    borderLine.forEachIndexed { idx, pt ->
                        val p = project(pt.first, pt.second, size)
                        if (idx == 0) bPath.moveTo(p.x, p.y) else bPath.lineTo(p.x, p.y)
                    }
                    drawPath(
                        path = bPath,
                        color = strokeColor,
                        style = Stroke(
                            width = 1.1f * scale.coerceIn(0.8f, 1.8f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )
                }
            }
            drawInternalBorder(ProvinceBorders, Color(0x66FFFFFF))
            drawInternalBorder(NeighborProvinceBorders, Color(0x55E2E8F0))

            // 13. Rótulos Provinciales de Fondo (Panamá)
            val provTextPaint = Paint().apply {
                color = android.graphics.Color.argb(195, 255, 255, 255)
                textSize = (12f * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(10f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.20f
                setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(180, 0, 0, 0))
            }
            PanamaProvinceLabels.forEach { prov ->
                val pos = project(prov.lat, prov.lon, size)
                drawContext.canvas.nativeCanvas.drawText(prov.name, pos.x, pos.y, provTextPaint)
            }

            // Rótulos Provinciales y Departamentales de Vecinos (Costa Rica y Colombia en Alta Resolución)
            val neighborProvTextPaint = Paint().apply {
                color = android.graphics.Color.argb(185, 230, 245, 235)
                textSize = (11f * scale.coerceIn(0.8f, 1.7f)).coerceAtLeast(9f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.22f
                setShadowLayer(4f, 0f, 1f, android.graphics.Color.argb(180, 0, 0, 0))
            }
            NeighborProvinceLabels.forEach { prov ->
                val pos = project(prov.lat, prov.lon, size)
                drawContext.canvas.nativeCanvas.drawText(prov.name, pos.x, pos.y, neighborProvTextPaint)
            }

            // 12. Estaciones Sismológicas Oficiales (Red Sismológica Nacional IGC - Universidad de Panamá)
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

                val codeText = "◆ ${station.code}"
                val locText = if (station.code == "UPA" && scale >= 2.8f) "" else " • ${station.locality}"
                val codeW = stationCodePaint.measureText(codeText)
                val locW = if (locText.isNotEmpty()) stationLocalityPaint.measureText(locText) else 0f
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

                if (!canDrawLabel(badgeRect)) return@forEach

                drawCircle(
                    color = Color(0x2800E5FF),
                    radius = dSize * 2.8f,
                    center = sPos
                )
                val diamondPath = Path().apply {
                    moveTo(sPos.x, sPos.y - dSize)
                    lineTo(sPos.x + dSize, sPos.y)
                    lineTo(sPos.x, sPos.y + dSize)
                    lineTo(sPos.x - dSize, sPos.y)
                    close()
                }
                drawPath(path = diamondPath, color = Color(0xFF00E5FF))
                drawPath(path = diamondPath, color = Color.White, style = Stroke(width = 1.0f))

                drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 5f, 5f, stationBadgePaint)
                drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, 5f, 5f, stationBorderPaint)

                val textBaselineY = badgeStartY + (badgeH * 0.72f)
                drawContext.canvas.nativeCanvas.drawText(codeText, badgeStartX, textBaselineY, stationCodePaint)
                if (locText.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(locText, badgeStartX + codeW, textBaselineY, stationLocalityPaint)
                }
            }

            // 13. Centros Urbanos y Ciudades de Referencia (Estilo Apple Maps Crisp Typography)
            val secCityPaint = Paint().apply {
                color = android.graphics.Color.argb(165, 226, 232, 240)
                textSize = (8.8f * scale.coerceIn(0.8f, 1.6f)).coerceAtLeast(8f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            SecondaryReferenceCities.forEach { city ->
                val cPos = project(city.lat, city.lon, size)
                val dotR = (if (city.isKey) 3.0f else 2.3f) * scale.coerceIn(0.8f, 1.4f)
                val cW = secCityPaint.measureText(city.name)
                val cRect = android.graphics.RectF(
                    cPos.x - dotR - 2f,
                    cPos.y - 10f * scale.coerceIn(0.8f, 1.4f),
                    cPos.x + dotR + cW + 4f,
                    cPos.y + dotR + 2f
                )
                if (city.name == "Ciudad de Panamá" || canDrawLabel(cRect)) {
                    drawCircle(color = Color(0xDD080E1C), radius = dotR + 1.2f, center = cPos)
                    drawCircle(color = if (city.isKey) Color(0xFF94A3B8) else Color(0xFF64748B), radius = dotR, center = cPos)
                    drawContext.canvas.nativeCanvas.drawText(city.name, cPos.x + dotR + 4f, cPos.y + 3f, secCityPaint)
                }
            }

            // 14. Enjambre Sísmico Activo (Azuero - Coiba): Área Resaltada
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

            // 15. Fallas Tectónicas Activas con Rótulos Interactivos
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

            // 16. Simulación de Ondas Sísmicas P y S con física realista de atenuación
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
                val maxReachKm = (90f * 10.0.pow(((mag - 3.0) / 2.25)).toFloat()).coerceIn(70f, 1300f)

                val attenP = ((1f - (distKmP / maxReachKm)).coerceIn(0f, 1f)).pow(1.3f)
                val attenS = ((1f - (distKmS / maxReachKm)).coerceIn(0f, 1f)).pow(1.1f)

                val magScale = ((mag - 2.5f) / 3.0f).coerceIn(0.5f, 2.2f)
                val strokeP = (3.2f * magScale * attenP * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(0.6f)
                val strokeS = (4.8f * magScale * attenS * scale.coerceIn(0.8f, 1.8f)).coerceAtLeast(0.8f)

                // Onda P (Cyan / Compresiva)
                if (attenP > 0.015f && radiusP > 4f) {
                    val alphaStroke = (0.65f * attenP * magScale.coerceAtMost(1.2f)).coerceIn(0f, 0.85f)
                    val alphaFill = (0.12f * attenP).coerceIn(0f, 0.22f)
                    drawCircle(color = Color(0xFF00E5FF).copy(alpha = alphaStroke), radius = radiusP, center = epi, style = Stroke(width = strokeP))
                    drawCircle(color = Color(0xFF00E5FF).copy(alpha = alphaFill), radius = radiusP, center = epi)
                }

                // Onda S (Roja / Corte)
                if (attenS > 0.015f && radiusS > 4f) {
                    val alphaStroke = (0.85f * attenS * magScale.coerceAtMost(1.3f)).coerceIn(0f, 0.95f)
                    val alphaFill = (0.18f * attenS).coerceIn(0f, 0.32f)
                    drawCircle(color = Color(0xFFFF5252).copy(alpha = alphaStroke), radius = radiusS, center = epi, style = Stroke(width = strokeS))
                    drawCircle(color = Color(0xFFFF5252).copy(alpha = alphaFill), radius = radiusS, center = epi)
                }
            }

            // 17. Epicentros / Sismos con Rótulos y Claridad
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
                    eq.magnitude >= 6.0 -> Color(0xFFFF1744)
                    eq.magnitude >= 5.0 -> Color(0xFFFF6D00)
                    eq.magnitude >= 4.0 -> Color(0xFFFFD600)
                    eq.magnitude >= 3.0 -> Color(0xFF00E5FF)
                    else -> Color(0xFF00E676)
                }

                val baseRadius = (eq.magnitude.toFloat() * 2.5f).coerceIn(6.5f, 22f) * scale.coerceIn(0.8f, 2.2f)

                // Anillo oscuro exterior
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

                if (eq.magnitude >= 6.0) {
                    drawCircle(
                        color = Color(0xFFFF1744),
                        radius = baseRadius * 2.2f,
                        center = pos,
                        style = Stroke(width = 2.2f * scale.coerceIn(0.8f, 2f))
                    )
                }

                // Núcleo
                drawCircle(
                    color = magColor,
                    radius = baseRadius,
                    center = pos
                )

                // Centro brillante de ruptura
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

                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 2.8f,
                        center = pos,
                        style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                }
            }

            // ==========================================
            // 18. GPS BLUE PUCK (ESTILO APPLE MAPS iOS)
            // ==========================================
            if (userLocation != null) {
                val uPos = project(userLocation.latitude, userLocation.longitude, size)

                if (uPos.x in -150f..(size.width + 150f) && uPos.y in -150f..(size.height + 150f)) {
                    val appleBlue = Color(0xFF007AFF)

                    // 1. Radar expansivo pulsante
                    val pulseMaxRadius = 38f * scale.coerceIn(0.8f, 2.0f)
                    val pulseRad = 12f + (beaconProgress * (pulseMaxRadius - 12f))
                    val pulseAlpha = (1f - beaconProgress) * 0.45f
                    drawCircle(
                        color = appleBlue.copy(alpha = pulseAlpha),
                        radius = pulseRad,
                        center = uPos
                    )

                    // 2. Halo de precisión estimada
                    val accMeters = userLocation.accuracy.coerceIn(10f, 500f)
                    val pxPerKm = ((size.width - 40f) / deltaLon) / 110.5f * scale
                    val accPx = (accMeters / 1000f * pxPerKm).coerceIn(14f, 75f)
                    drawCircle(
                        color = appleBlue.copy(alpha = 0.12f),
                        radius = accPx,
                        center = uPos
                    )
                    drawCircle(
                        color = appleBlue.copy(alpha = 0.30f),
                        radius = accPx,
                        center = uPos,
                        style = Stroke(width = 1.0f)
                    )

                    // 3. Sombra de separación
                    drawCircle(
                        color = Color(0x66000000),
                        radius = 11.5f,
                        center = uPos
                    )

                    // 4. Anillo exterior blanco Apple
                    drawCircle(
                        color = Color.White,
                        radius = 9.5f,
                        center = uPos
                    )

                    // 5. Núcleo azul vibrante iOS
                    drawCircle(
                        color = appleBlue,
                        radius = 7.0f,
                        center = uPos
                    )

                    // 6. Reflejo especular blanco (efecto cristal)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = 2.2f,
                        center = Offset(uPos.x - 1.8f, uPos.y - 1.8f)
                    )

                    // 7. Etiqueta flotante Apple "TU UBICACIÓN"
                    val userTagPaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 9.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val tagBg = Paint().apply {
                        color = android.graphics.Color.argb(235, 10, 18, 36)
                        style = Paint.Style.FILL
                    }
                    val tagBorder = Paint().apply {
                        color = android.graphics.Color.argb(160, 0, 122, 255)
                        style = Paint.Style.STROKE
                        strokeWidth = 1.2f
                    }
                    val labelText = "📍 TU UBICACIÓN"
                    val lWidth = userTagPaint.measureText(labelText)
                    val tagRect = android.graphics.RectF(
                        uPos.x - lWidth / 2f - 7f,
                        uPos.y - 28f,
                        uPos.x + lWidth / 2f + 7f,
                        uPos.y - 10f
                    )
                    drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 9f, 9f, tagBg)
                    drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 9f, 9f, tagBorder)
                    drawContext.canvas.nativeCanvas.drawText(labelText, uPos.x, uPos.y - 15.5f, userTagPaint)
                }
            }

            // 19. Instrumentos Cartográficos de Navegación y Referencia
            drawCoordinateTickMarks(this, size) { lat, lon -> project(lat, lon, size) }
            drawDynamicScaleBar(this, size, scale, deltaLon)
            drawTectonicCompass(this, size, scale)
        }
    }
}

// Botones Flotantes Apple Liquid Glass (Zoom, Centrar en Mí, Centrar Panamá)
@Composable
private fun FloatingAppleMapControls(
    hasUserLocation: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCenterUser: () -> Unit,
    onResetPanama: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón: Centrar en Mi Ubicación (Apple Style)
        IconButton(
            onClick = onCenterUser,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xDD0D1627))
                .border(1.2.dp, if (hasUserLocation) Color(0xFF007AFF) else Color(0x44FFFFFF), CircleShape)
                .shadow(6.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Centrar en mi ubicación",
                tint = if (hasUserLocation) Color(0xFF007AFF) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Agrupación Zoom (+ / -)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xDD0D1627),
            border = BorderStroke(1.dp, Color(0x44FFFFFF)),
            shadowElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Acercar",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(1.dp)
                        .background(Color(0x33FFFFFF))
                )
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Alejar",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Botón: Vista General de Panamá
        IconButton(
            onClick = onResetPanama,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xDD0D1627))
                .border(1.dp, Color(0x44FFFFFF), CircleShape)
                .shadow(6.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Ver todo Panamá",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
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

    val southPath = Path().apply {
        moveTo(compassCenter.x, compassCenter.y + r)
        lineTo(compassCenter.x + 4f, compassCenter.y)
        lineTo(compassCenter.x - 4f, compassCenter.y)
        close()
    }
    drawScope.drawPath(southPath, color = Color(0xFF475569))

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

    drawScope.drawLine(Color.White, Offset(origin.x, origin.y - 3f), Offset(origin.x, origin.y + 6f), strokeWidth = 1f)
    drawScope.drawLine(Color.White, Offset(origin.x + halfWidth, origin.y - 3f), Offset(origin.x + halfWidth, origin.y + 6f), strokeWidth = 1f)
    drawScope.drawLine(Color.White, Offset(origin.x + barWidthPx, origin.y - 3f), Offset(origin.x + barWidthPx, origin.y + 6f), strokeWidth = 1f)

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

    for (lat in 6..10) {
        val pt = project(lat.toDouble(), MAP_MIN_LON + 0.5)
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

    for (lon in -85..-76) {
        val pt = project(MAP_MAX_LAT - 0.25, lon.toDouble())
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
    var areToolsExpanded by remember { mutableStateOf(false) }

    val topBarBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xEA0D1627),
            Color(0xF2080D1A)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RectangleShape,
                ambientColor = Color(0x66000000),
                spotColor = Color(0xAA000000)
            )
            .background(topBarBrush)
            .statusBarsPadding()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x5538BDF8))),
                shape = RectangleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(38.dp)
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
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = "EXPLORADOR TECTÓNICO",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (showWaveSimulation) "Simulación de ondas en tiempo real" else "Mostrando $earthquakeCount de $totalCount sismos",
                    fontSize = 10.5.sp,
                    color = if (showWaveSimulation) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { areToolsExpanded = !areToolsExpanded },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (areToolsExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                    contentDescription = if (areToolsExpanded) "Ocultar herramientas" else "Mostrar herramientas",
                    tint = if (areToolsExpanded || showOnlySwarm || showFaults || showWaveSimulation) PrimaryBlue else Color(0xFF94A3B8),
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onResetView,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar Panamá",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = !showWaveSimulation && areToolsExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                            label = { Text("Fallas Geológicas", fontSize = 11.sp, maxLines = 1) },
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

                if (showFaults) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FALLAS REGISTRADAS (TOCA PARA INSPECCIONAR):",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.4.sp
                    )
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
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    val headerBadgeText = when (targetIndex) {
        0 -> "TODOS • $totalQuakesCount SISMOS"
        else -> {
            val tier = tiers[targetIndex - 1]
            val count = quakeCounts[tier.id] ?: 0
            "M ${tier.label} ${tier.title.uppercase()} • $count SISMOS"
        }
    }

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

    if (isCollapsed) {
        Surface(
            modifier = modifier.clickable { onToggleCollapse() },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xF2091020),
            border = BorderStroke(
                0.9.dp,
                if (targetIndex > 0) activeColor.copy(alpha = 0.65f) else Color(0x3338BDF8)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(activeColor)
                    )
                    Text(
                        text = "PELIGRO SÍSMICO",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = activeColor.copy(alpha = 0.18f),
                        border = BorderStroke(0.8.dp, activeColor.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = headerBadgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Expandir selector",
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleCollapse() }
                        .padding(vertical = 2.dp),
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
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Contraer escala",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

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

                        val thumbXDp = with(density) { (currentThumbX - haloRadiusPx).toDp() }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = thumbXDp)
                                .size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(activeColor.copy(alpha = 0.35f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                                    .border(1.8.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
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
}

// Tarjeta inferior Falla Seleccionada (Estilo Apple Sheet con rounded 24dp y pill handle)
@Composable
private fun SelectedFaultCard(
    fault: FaultLineInteractive,
    associatedQuakesCount: Int,
    userDistanceKm: Double? = null,
    onDismiss: () -> Unit,
    onFilterFaultQuakes: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pill grabber estilo Apple Sheet
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x44FFFFFF))
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(fault.color)
                    )
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = fault.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = fault.region,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x2EFFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Datos de Geociencia explicados para ciudadanos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (userDistanceKm != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007AFF))
                        )
                        Text(
                            text = "📍 Estás a ${userDistanceKm.toInt()} km de esta falla activa",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA)
                        )
                    }
                }

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
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(
                        text = "⚡ Ver $associatedQuakesCount sismos activos asociados",
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

// Tarjeta inferior Sismo Seleccionado (Estilo Apple Sheet con rounded 24dp y telemetría GPS)
@Composable
private fun SelectedEarthquakeCard(
    earthquake: Earthquake,
    isSimulatingWaves: Boolean,
    userDistanceKm: Double? = null,
    onToggleWaveSim: () -> Unit,
    onViewDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = if (isSimulatingWaves) 16.dp else 24.dp
    ) {
        if (isSimulatingWaves) {
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
                // Pill grabber estilo Apple Sheet
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x44FFFFFF))
                        .align(Alignment.CenterHorizontally)
                )

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

                // Telemetría GPS con estimación de propagación de ondas hasta tu posición
                if (userDistanceKm != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x22007AFF),
                        border = BorderStroke(0.8.dp, Color(0x66007AFF)),
                        modifier = Modifier.fillMaxWidth()
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
                                    .background(Color(0xFF007AFF))
                            )
                            val distInt = userDistanceKm.toInt()
                            val timeP = (userDistanceKm / 6.0).toInt()
                            val timeS = (userDistanceKm / 3.5).toInt()
                            Text(
                                text = "📍 A $distInt km de tu ubicación (~${timeP}s Onda P / ~${timeS}s Onda S)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF93C5FD)
                            )
                        }
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Simular Onda
                    Button(
                        onClick = onToggleWaveSim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x333186FF)
                        ),
                        border = BorderStroke(1.dp, Color(0x663186FF)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF60A5FA)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simular Onda",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Botón Ver Análisis
                    Button(
                        onClick = onViewDetail,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(text = "Ver Análisis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
