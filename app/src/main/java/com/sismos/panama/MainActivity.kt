package com.sismos.panama

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.components.PanamaBottomNavigationBar
import com.sismos.panama.ui.screens.AboutScreen
import com.sismos.panama.ui.screens.DashboardScreen
import com.sismos.panama.ui.screens.DetailScreen
import com.sismos.panama.ui.screens.FaultsScreen
import com.sismos.panama.ui.screens.HomeScreen
import com.sismos.panama.ui.screens.InteractiveMapScreen
import com.sismos.panama.ui.theme.BgDark
import com.sismos.panama.ui.theme.SismosPanamaTheme
import com.sismos.panama.ui.viewmodel.SeismicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SeismicViewModel by viewModels()
    private val currentScreenState: MutableState<Screen> = mutableStateOf(Screen.Home)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        askNotificationPermission()

        handleScreenIntent(intent)

        setContent {
            SismosPanamaTheme {
                val currentScreen = currentScreenState.value
                val showBottomBar = currentScreen !is Screen.Detail && currentScreen !is Screen.About

                Scaffold(
                    containerColor = BgDark,
                    bottomBar = {
                        if (showBottomBar) {
                            PanamaBottomNavigationBar(
                                currentScreen = currentScreen,
                                onNavigate = { target ->
                                    currentScreenState.value = target
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        SismosAppNavigation(
                            viewModel = viewModel,
                            screenState = currentScreenState
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleScreenIntent(intent)
    }

    private fun handleScreenIntent(intent: Intent?) {
        val target = intent?.getStringExtra("screen")?.lowercase() ?: return
        val simulate = intent.getBooleanExtra("simulate", false)
        when (target) {
            "dashboard" -> currentScreenState.value = Screen.Dashboard
            "faults" -> currentScreenState.value = Screen.Faults
            "map" -> currentScreenState.value = Screen.Map(initialWaveSimulation = simulate)
            "about" -> currentScreenState.value = Screen.About
            "home" -> currentScreenState.value = Screen.Home
            "detail" -> {
                val eq = viewModel.uiState.value.earthquakes.firstOrNull() ?: Earthquake(
                    id = "demo_panama_chiriqui",
                    magnitude = 6.2,
                    place = "Golfo de Chiriquí, Panamá",
                    timeMillis = System.currentTimeMillis() - 1000 * 60 * 25,
                    latitude = 7.95,
                    longitude = -82.65,
                    depthKm = 12.0,
                    nearestCity = "David, Chiriquí",
                    distanceToNearestCityKm = 48,
                    nearestFault = "Zona de Fractura de Panamá (ZFP)",
                    faultMechanism = "Rumbo lateral derecho / Subducción",
                    panamaRegion = "Pacífico Occidental / Chiriquí",
                    isTsunamiWarning = false,
                    feltReports = 240,
                    officialUrl = "https://earthquake.usgs.gov/"
                )
                currentScreenState.value = Screen.Detail(eq, autoSimulate = simulate)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        requestBatteryExemption()
    }

    private fun requestBatteryExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    data class Detail(val earthquake: Earthquake, val autoSimulate: Boolean = false) : Screen()
    object Faults : Screen()
    object Dashboard : Screen()
    data class Map(val initialWaveSimulation: Boolean = false) : Screen()
    object About : Screen()
}

@Composable
fun SismosAppNavigation(
    viewModel: SeismicViewModel,
    screenState: MutableState<Screen>
) {
    when (val screen = screenState.value) {
        is Screen.Home -> {
            HomeScreen(
                viewModel = viewModel,
                onEarthquakeSelected = { eq ->
                    screenState.value = Screen.Detail(eq)
                },
                onNavigateToFaults = {
                    screenState.value = Screen.Faults
                },
                onNavigateToDashboard = {
                    screenState.value = Screen.Dashboard
                },
                onNavigateToMap = {
                    screenState.value = Screen.Map()
                },
                onNavigateToAbout = {
                    screenState.value = Screen.About
                }
            )
        }
        is Screen.About -> {
            AboutScreen(
                onBack = { screenState.value = Screen.Home }
            )
        }
        is Screen.Map -> {
            InteractiveMapScreen(
                viewModel = viewModel,
                initialWaveSimulation = screen.initialWaveSimulation,
                onBack = { screenState.value = Screen.Home },
                onEarthquakeSelected = { eq ->
                    screenState.value = Screen.Detail(eq)
                }
            )
        }
        is Screen.Dashboard -> {
            DashboardScreen(
                viewModel = viewModel,
                onBack = { screenState.value = Screen.Home },
                onEarthquakeSelected = { eq ->
                    screenState.value = Screen.Detail(eq)
                }
            )
        }
        is Screen.Detail -> {
            DetailScreen(
                earthquake = screen.earthquake,
                initialSimulating = screen.autoSimulate,
                onBack = { screenState.value = Screen.Home },
                onOpenMap = { screenState.value = Screen.Map(initialWaveSimulation = true) }
            )
        }
        is Screen.Faults -> {
            FaultsScreen(
                onBack = { screenState.value = Screen.Home }
            )
        }
    }
}
