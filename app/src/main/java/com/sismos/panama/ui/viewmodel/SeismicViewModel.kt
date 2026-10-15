package com.sismos.panama.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.data.repository.EarthquakeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SeismicUiState(
    val isLoading: Boolean = false,
    val earthquakes: List<Earthquake> = emptyList(),
    val filteredEarthquakes: List<Earthquake> = emptyList(),
    val errorMessage: String? = null,
    val selectedMinMag: Double? = null,
    val selectedRegion: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

class SeismicViewModel(
    private val repository: EarthquakeRepository = EarthquakeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeismicUiState())
    val uiState: StateFlow<SeismicUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        loadEarthquakes()
        startAutoRefresh()
    }

    fun loadEarthquakes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.fetchPanamaEarthquakes()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    earthquakes = list,
                    filteredEarthquakes = applyFilters(
                        list,
                        _uiState.value.selectedMinMag,
                        _uiState.value.selectedRegion
                    ),
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Error al conectar con la red sísmica"
                )
            }
        }
    }

    fun setMinMagnitudeFilter(minMag: Double?) {
        _uiState.value = _uiState.value.copy(
            selectedMinMag = minMag,
            filteredEarthquakes = applyFilters(
                _uiState.value.earthquakes,
                minMag,
                _uiState.value.selectedRegion
            )
        )
    }

    fun setRegionFilter(region: String?) {
        _uiState.value = _uiState.value.copy(
            selectedRegion = region,
            filteredEarthquakes = applyFilters(
                _uiState.value.earthquakes,
                _uiState.value.selectedMinMag,
                region
            )
        )
    }

    private fun applyFilters(
        list: List<Earthquake>,
        minMag: Double?,
        region: String?
    ): List<Earthquake> {
        return list.filter { eq ->
            val matchesMag = minMag == null || eq.magnitude >= minMag
            val matchesRegion = region == null || eq.panamaRegion.equals(region, ignoreCase = true)
            matchesMag && matchesRegion
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000) // Actualizar cada 60 segundos
                loadEarthquakes()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
