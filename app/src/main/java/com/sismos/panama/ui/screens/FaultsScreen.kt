package com.sismos.panama.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.sismos.panama.data.models.FaultLine
import com.sismos.panama.data.models.PanamaFaultsCatalog
import com.sismos.panama.ui.components.GlassCard
import com.sismos.panama.ui.theme.BgDark
import com.sismos.panama.ui.theme.PrimaryBlue

/**
 * Pantalla informativa y didáctica de los Sistemas de Fallas Geológicas en Panamá.
 * Presenta el catálogo tectónico nacional con datos técnicos estructurados
 * (mecanismo, tasa de desplazamiento, recurrencia, región y coordenadas representativas)
 * sin incluir registros de eventos históricos.
 */
@Composable
fun FaultsScreen(
    onBack: () -> Unit
) {
    var selectedRegionFilter by remember { mutableStateOf<String?>(null) }
    var showGlossary by remember { mutableStateOf(false) }

    val regionFilters = listOf(
        "Todas" to null,
        "Pacífico / Chiriquí" to "Chiriquí",
        "Azuero / Veraguas" to "Azuero",
        "Canal / Metro" to "Canal",
        "Caribe Norte" to "Caribe",
        "Darién / Este" to "Darién"
    )

    val filteredFaults = remember(selectedRegionFilter) {
        if (selectedRegionFilter == null) {
            PanamaFaultsCatalog.faults
        } else {
            PanamaFaultsCatalog.faults.filter { fault ->
                fault.region.contains(selectedRegionFilter!!, ignoreCase = true) ||
                fault.name.contains(selectedRegionFilter!!, ignoreCase = true)
            }
        }
    }

    val topBarBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF131B2A),
            Color(0xFF0D121B)
        )
    )

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarBrush)
                    .statusBarsPadding()
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color.Transparent)),
                        shape = androidx.compose.ui.graphics.RectangleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "SISTEMAS DE FALLAS DE PANAMÁ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Catálogo Tectónico Nacional • Panel Informativo",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Panel de Introducción Tectónica
            item {
                TectonicIntroCard()
            }

            // Selector interactivo por región
            item {
                Column {
                    Text(
                        text = "FILTRAR POR ZONA TECTÓNICA",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(regionFilters) { (label, filterVal) ->
                            val isSelected = selectedRegionFilter == filterVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFF3186FF) else Color(0x18FFFFFF)
                                    )
                                    .border(
                                        0.8.dp,
                                        if (isSelected) Color(0xFF60A5FA) else Color(0x22FFFFFF),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedRegionFilter = filterVal }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }

            // Contador de estructuras listadas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESTRUCTURAS IDENTIFICADAS (${filteredFaults.size})",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Datos Geotectónicos IGC-UP",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Lista de Fallas Tectónicas Estructuradas (Sin eventos históricos)
            items(filteredFaults, key = { it.id }) { fault ->
                FaultStructuredCard(fault = fault)
            }

            // Glosario Geológico Desplegable
            item {
                TectonicGlossaryCard(
                    isExpanded = showGlossary,
                    onToggle = { showGlossary = !showGlossary }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Tarjeta de Introducción al Marco Tectónico de Panamá
 */
@Composable
private fun TectonicIntroCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x2200E5FF))
                        .border(1.dp, Color(0x5500E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "MARCO GEODINÁMICO DE PANAMÁ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "Microplaca de Panamá y Fallamiento Activo",
                        fontSize = 11.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "El territorio panameño se asienta sobre la Microplaca de Panamá, una unidad tectónica dinámica comprimida entre cuatro grandes placas: Caribe al norte, Nazca al sur, Cocos al suroeste y el Bloque Norandino/Chocó al sureste. Esta interacción genera complejos sistemas de fallas activas responsables de la sismicidad en el país.",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x183186FF),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0x443186FF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Este panel es informativo y describe la anatomía estructural de cada falla.",
                        fontSize = 10.sp,
                        color = Color(0xFF93C5FD)
                    )
                }
            }
        }
    }
}

/**
 * Ficha Tectónica Estructurada de una Falla Geológica (Excluye eventos históricos)
 */
@Composable
private fun FaultStructuredCard(fault: FaultLine) {
    val accentColor = when (fault.id) {
        "zfp" -> Color(0xFF00E5FF)       // Transformante oceánica
        "burica" -> Color(0xFFF59E0B)    // Transpresiva
        "sona_tonosi" -> Color(0xFF38BDF8) // Rumbo cortical Azuero
        "pedro_miguel" -> Color(0xFFA78BFA) // Canal de Panamá
        "miraflores" -> Color(0xFF818CF8)  // Boca del Pacífico
        "limon" -> Color(0xFF34D399)       // Colón
        "cdnp" -> Color(0xFFEF4444)        // Cinturón Deformado Caribe
        "darien_sanson" -> Color(0xFFFB923C) // Darién
        else -> PrimaryBlue
    }

    val activityLevel = when (fault.id) {
        "zfp" -> "Muy Alta • Principal Motor Sísmico Nacional"
        "burica" -> "Muy Alta • Enjambres Sísmicos Continuos"
        "sona_tonosi" -> "Alta • Sismicidad Cortical Activa"
        "cdnp" -> "Alta • Cabalgamiento Submarino Caribe"
        "pedro_miguel" -> "Moderada • Monitoreo Estratégico Cuenca Canal"
        "miraflores" -> "Moderada • Monitoreo Continuo"
        "limon" -> "Moderada • Actividad Cuaternaria"
        "darien_sanson" -> "Moderada a Alta • Compresión Bloque Chocó"
        else -> "Monitoreo Oficial"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderWidth = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Encabezado: Nombre y badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fault.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = fault.region,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }

                // Badge de Nivel de Actividad Tectónica
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(0.8.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = fault.id.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Descripción geológica
            Text(
                text = fault.description,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // FICHA TÉCNICA ESTRUCTURADA EN GRID ADAPTABLE (2 columnas en Tablet, 1 columna en Teléfono)
            val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
            val isTablet = screenWidth >= 600

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x14FFFFFF))
                    .border(0.6.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TectonicMetricRow(
                                icon = Icons.Default.Tune,
                                label = "Mecanismo Tectónico",
                                value = fault.mechanism,
                                accentColor = accentColor
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TectonicMetricRow(
                                icon = Icons.Default.Speed,
                                label = "Tasa de Desplazamiento (Slip Rate)",
                                value = fault.slipRate,
                                accentColor = accentColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TectonicMetricRow(
                                icon = Icons.Default.Timeline,
                                label = "Periodo de Recurrencia Estimado",
                                value = fault.recurrence,
                                accentColor = accentColor
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TectonicMetricRow(
                                icon = Icons.Default.LocationOn,
                                label = "Ubicación Geográfica Representativa",
                                value = "Lat: ${String.format(java.util.Locale.US, "%.2f° N", fault.representativeLat)} • Lon: ${String.format(java.util.Locale.US, "%.2f° O", -fault.representativeLon)}",
                                accentColor = accentColor
                            )
                        }
                    }
                } else {
                    // Fila 1: Mecanismo Tectónico
                    TectonicMetricRow(
                        icon = Icons.Default.Tune,
                        label = "Mecanismo Tectónico",
                        value = fault.mechanism,
                        accentColor = accentColor
                    )

                    // Fila 2: Tasa de Desplazamiento (Slip Rate)
                    TectonicMetricRow(
                        icon = Icons.Default.Speed,
                        label = "Tasa de Desplazamiento (Slip Rate)",
                        value = fault.slipRate,
                        accentColor = accentColor
                    )

                    // Fila 3: Periodo de Recurrencia
                    TectonicMetricRow(
                        icon = Icons.Default.Timeline,
                        label = "Periodo de Recurrencia Estimado",
                        value = fault.recurrence,
                        accentColor = accentColor
                    )

                    // Fila 4: Coordenadas Tectónicas Representativas
                    TectonicMetricRow(
                        icon = Icons.Default.LocationOn,
                        label = "Ubicación Geográfica Representativa",
                        value = "Lat: ${String.format(java.util.Locale.US, "%.2f° N", fault.representativeLat)} • Lon: ${String.format(java.util.Locale.US, "%.2f° O", -fault.representativeLon)}",
                        accentColor = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indicador de nivel sismogénico
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = "Régimen: $activityLevel",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Fila individual de parámetro dentro de la ficha técnica
 */
@Composable
private fun TectonicMetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                modifier = Modifier.size(15.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Glosario didáctico de conceptos tectónicos para usuarios no geólogos
 */
@Composable
private fun TectonicGlossaryCard(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x333186FF)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "GLOSARIO Y GUÍA DE CONCEPTOS TECTÓNICOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.4.sp
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlossaryItem(
                        term = "Tasa de Desplazamiento (Slip Rate)",
                        definition = "Velocidad constante a la que los bloques de roca a cada lado de la falla se mueven entre sí. Se mide en milímetros por año (mm/año). A mayor tasa, más rápida es la acumulación de energía tectónica."
                    )
                    GlossaryItem(
                        term = "Periodo de Recurrencia",
                        definition = "Estimación temporal geológica que transcurre entre eventos sísmicos de magnitud similar en la misma estructura. No es una predicción, sino un intervalo promedio paleosísmico."
                    )
                    GlossaryItem(
                        term = "Mecanismo de Rumbo (Strike-Slip)",
                        definition = "Desplazamiento horizontal de las masas rocosas. Si el bloque opuesto se desplaza a la derecha es dextral (como la ZFP); si va a la izquierda es sinestral (como Soná-Tonosí o Pedro Miguel)."
                    )
                    GlossaryItem(
                        term = "Cabalgamiento / Falla Inversa",
                        definition = "Movimiento vertical donde un bloque asciende sobre el otro debido a la fuerte compresión entre placas, típico del margen Caribe (Cinturón Deformado del Norte de Panamá)."
                    )
                    GlossaryItem(
                        term = "Estructura Sismogénica",
                        definition = "Falla geológica activa que posee las dimensiones y esfuerzos necesarios para generar sismos detectables y liberar energía acumulada en la corteza terrestre."
                    )
                }
            }
        }
    }
}

@Composable
private fun GlossaryItem(term: String, definition: String) {
    Column {
        Text(
            text = "• $term",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF93C5FD)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = definition,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 16.sp
        )
    }
}
