package com.sismos.panama.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sismos.panama.data.background.SeismicAlarmReceiver
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.data.repository.EarthquakeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SeismicUiState(
    val isLoading: Boolean = false,
    val earthquakes: List<Earthquake> = emptyList(),
    val filteredEarthquakes: List<Earthquake> = emptyList(),
    val errorMessage: String? = null,
    val selectedMinMag: Double? = null,
    val selectedRegion: String? = null,
    val selectedFaultId: String? = null,
    val isSwarmOnly: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

class SeismicViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = EarthquakeRepository()
    private val _uiState = MutableStateFlow(SeismicUiState())
    val uiState: StateFlow<SeismicUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var previousTopEventId: String? = null

    init {
        loadEarthquakes()
        startAutoRefresh()
    }

    fun loadEarthquakes(isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isManualRefresh || _uiState.value.earthquakes.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            val result = repository.fetchPanamaEarthquakes()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                if (list.isNotEmpty()) {
                    val latest = list.first()
                    // Si llega un sismo nuevo mientras la app está abierta, alertar inmediatamente
                    if (previousTopEventId != null && previousTopEventId != latest.id && (latest.magnitude >= 3.0 || latest.isTsunamiWarning)) {
                        val prefs = getApplication<Application>().getSharedPreferences("sismos_prefs", Context.MODE_PRIVATE)
                        val lastNotified = prefs.getString("last_notified_event_id", null)
                        if (lastNotified != latest.id) {
                            SeismicAlarmReceiver.sendHighPriorityNotification(getApplication(), latest)
                            prefs.edit().putString("last_notified_event_id", latest.id).apply()
                        }
                    }
                    previousTopEventId = latest.id
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    earthquakes = list,
                    filteredEarthquakes = applyFilters(
                        list,
                        _uiState.value.selectedMinMag,
                        _uiState.value.selectedRegion,
                        _uiState.value.selectedFaultId,
                        _uiState.value.isSwarmOnly
                    ),
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (_uiState.value.earthquakes.isEmpty()) {
                        result.exceptionOrNull()?.localizedMessage ?: "Error al conectar con la red sísmica"
                    } else null
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
                _uiState.value.selectedRegion,
                _uiState.value.selectedFaultId,
                _uiState.value.isSwarmOnly
            )
        )
    }

    fun setRegionFilter(region: String?) {
        _uiState.value = _uiState.value.copy(
            selectedRegion = region,
            filteredEarthquakes = applyFilters(
                _uiState.value.earthquakes,
                _uiState.value.selectedMinMag,
                region,
                _uiState.value.selectedFaultId,
                _uiState.value.isSwarmOnly
            )
        )
    }

    fun setFaultFilter(faultId: String?) {
        val newFault = if (_uiState.value.selectedFaultId == faultId) null else faultId
        _uiState.value = _uiState.value.copy(
            selectedFaultId = newFault,
            isSwarmOnly = false,
            filteredEarthquakes = applyFilters(
                _uiState.value.earthquakes,
                _uiState.value.selectedMinMag,
                _uiState.value.selectedRegion,
                newFault,
                false
            )
        )
    }

    fun toggleSwarmFilter() {
        val newSwarm = !_uiState.value.isSwarmOnly
        _uiState.value = _uiState.value.copy(
            isSwarmOnly = newSwarm,
            selectedFaultId = null,
            filteredEarthquakes = applyFilters(
                _uiState.value.earthquakes,
                _uiState.value.selectedMinMag,
                _uiState.value.selectedRegion,
                null,
                newSwarm
            )
        )
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            selectedMinMag = null,
            selectedRegion = null,
            selectedFaultId = null,
            isSwarmOnly = false,
            filteredEarthquakes = applyFilters(
                _uiState.value.earthquakes,
                null,
                null,
                null,
                false
            )
        )
    }

    private fun applyFilters(
        list: List<Earthquake>,
        minMag: Double?,
        region: String?,
        faultId: String?,
        isSwarmOnly: Boolean
    ): List<Earthquake> {
        return list.filter { eq ->
            val matchesMag = minMag == null || eq.magnitude >= minMag
            val matchesRegion = region == null || eq.panamaRegion.equals(region, ignoreCase = true)
            val matchesFault = faultId == null || when (faultId) {
                "sona_tonosi" -> eq.nearestFault.contains("Soná", ignoreCase = true) ||
                        eq.nearestFault.contains("Tonosí", ignoreCase = true) ||
                        (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
                "zfp" -> eq.nearestFault.contains("Fractura", ignoreCase = true) ||
                        eq.nearestFault.contains("ZFP", ignoreCase = true)
                "burica" -> eq.nearestFault.contains("Burica", ignoreCase = true) ||
                        eq.nearestFault.contains("Armuelles", ignoreCase = true)
                "pedro_miguel" -> eq.nearestFault.contains("Pedro Miguel", ignoreCase = true) ||
                        eq.nearestFault.contains("Canal", ignoreCase = true)
                "limon" -> eq.nearestFault.contains("Limón", ignoreCase = true) ||
                        eq.nearestFault.contains("Limon", ignoreCase = true)
                "cdnp" -> eq.nearestFault.contains("CDNP", ignoreCase = true) ||
                        eq.nearestFault.contains("Caribe", ignoreCase = true)
                "chucunaque" -> eq.nearestFault.contains("Chucunaque", ignoreCase = true) ||
                        eq.nearestFault.contains("Darién", ignoreCase = true)
                else -> eq.nearestFault.contains(faultId, ignoreCase = true)
            }
            val matchesSwarm = !isSwarmOnly || (eq.latitude in 7.0..8.2 && eq.longitude in -82.0..-80.2)
            matchesMag && matchesRegion && matchesFault && matchesSwarm
        }.sortedByDescending { it.timeMillis }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(15_000) // Consulta ultra-rápida cada 15 segundos
                loadEarthquakes(isManualRefresh = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
