package com.sismos.panama.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.components.EarthquakeCard
import com.sismos.panama.ui.components.PanamaRegionsBanner
import com.sismos.panama.ui.theme.BgDark
import com.sismos.panama.ui.theme.MagGreen
import com.sismos.panama.ui.theme.PrimaryBlue
import com.sismos.panama.ui.theme.StrokeDark
import com.sismos.panama.ui.theme.SurfaceDark
import com.sismos.panama.ui.viewmodel.SeismicViewModel

@Composable
fun HomeScreen(
    viewModel: SeismicViewModel,
    onEarthquakeSelected: (Earthquake) -> Unit,
    onNavigateToFaults: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            HomeTopBar(
                isLoading = state.isLoading,
                onRefresh = { viewModel.loadEarthquakes() },
                onNavigateToFaults = onNavigateToFaults
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Status Banner
            LiveStatusBanner()

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros de Magnitud
            MagnitudeFilterRow(
                selectedMinMag = state.selectedMinMag,
                onSelectMinMag = { viewModel.setMinMagnitudeFilter(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Zonas sísmicas de Panamá (HTML Widget)
            PanamaRegionsBanner(
                selectedRegion = state.selectedRegion,
                onSelectRegion = { viewModel.setRegionFilter(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Sismos
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
                        onRetry = { viewModel.loadEarthquakes() }
                    )
                } else if (state.filteredEarthquakes.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredEarthquakes, key = { it.id }) { eq ->
                            EarthquakeCard(
                                earthquake = eq,
                                onClick = { onEarthquakeSelected(eq) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNavigateToFaults: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SISMOS PANAMÁ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Red Sismológica Nacional en Vivo",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateToFaults) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Fallas Geológicas",
                    tint = PrimaryBlue
                )
            }

            IconButton(onClick = onRefresh, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatusBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF102A45))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MagGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "EN TIEMPO REAL • FILTRADO EXCLUSIVO PARA PANAMÁ",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF93C5FD)
        )
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
            FilterChip(
                selected = isSelected,
                onClick = { onSelectMinMag(value) },
                label = { Text(text = label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceDark,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = StrokeDark,
                    selectedBorderColor = PrimaryBlue,
                    enabled = true,
                    selected = isSelected
                )
            )
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
