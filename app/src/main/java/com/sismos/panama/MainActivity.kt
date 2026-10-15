package com.sismos.panama

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.screens.DashboardScreen
import com.sismos.panama.ui.screens.DetailScreen
import com.sismos.panama.ui.screens.FaultsScreen
import com.sismos.panama.ui.screens.HomeScreen
import com.sismos.panama.ui.theme.SismosPanamaTheme
import com.sismos.panama.ui.viewmodel.SeismicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SeismicViewModel by viewModels()
    private val currentScreenState: MutableState<Screen> = mutableStateOf(Screen.Home)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    private val adbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val screen = intent.getStringExtra("screen")
            val index = intent.getIntExtra("index", 0)
            val minMag = intent.getStringExtra("min_magnitude")?.toDoubleOrNull() ?: intent.getDoubleExtra("min_magnitude", -1.0)
            val region = intent.getStringExtra("region")

            if (minMag >= 0.0) {
                viewModel.setMinMagnitudeFilter(minMag)
            }
            if (region != null) {
                viewModel.setRegionFilter(region)
            }

            when (screen?.lowercase()) {
                "home" -> currentScreenState.value = Screen.Home
                "dashboard" -> currentScreenState.value = Screen.Dashboard
                "faults" -> currentScreenState.value = Screen.Faults
                "detail" -> {
                    val list = viewModel.uiState.value.filteredEarthquakes
                    if (list.isNotEmpty()) {
                        val safeIndex = index.coerceIn(0, list.lastIndex)
                        currentScreenState.value = Screen.Detail(list[safeIndex])
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()

        val initialScreen = intent?.getStringExtra("screen")
        when (initialScreen?.lowercase()) {
            "dashboard" -> currentScreenState.value = Screen.Dashboard
            "faults" -> currentScreenState.value = Screen.Faults
            else -> currentScreenState.value = Screen.Home
        }

        val filter = IntentFilter("com.sismos.panama.COMMAND")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(adbReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(adbReceiver, filter)
        }

        setContent {
            SismosPanamaTheme {
                SismosAppNavigation(
                    viewModel = viewModel,
                    screenState = currentScreenState
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(adbReceiver)
        } catch (_: Exception) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (intent.getStringExtra("screen")?.lowercase()) {
            "dashboard" -> currentScreenState.value = Screen.Dashboard
            "faults" -> currentScreenState.value = Screen.Faults
            "home" -> currentScreenState.value = Screen.Home
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
    }
}

sealed class Screen {
    object Home : Screen()
    data class Detail(val earthquake: Earthquake) : Screen()
    object Faults : Screen()
    object Dashboard : Screen()
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
                }
            )
        }
        is Screen.Dashboard -> {
            DashboardScreen(
                viewModel = viewModel,
                onBack = { screenState.value = Screen.Home }
            )
        }
        is Screen.Detail -> {
            DetailScreen(
                earthquake = screen.earthquake,
                onBack = { screenState.value = Screen.Home }
            )
        }
        is Screen.Faults -> {
            FaultsScreen(
                onBack = { screenState.value = Screen.Home }
            )
        }
    }
}
