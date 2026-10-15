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
import com.sismos.panama.ui.screens.AboutScreen
import com.sismos.panama.ui.screens.DashboardScreen
import com.sismos.panama.ui.screens.DetailScreen
import com.sismos.panama.ui.screens.FaultsScreen
import com.sismos.panama.ui.screens.HomeScreen
import com.sismos.panama.ui.screens.InteractiveMapScreen
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

            val alertMode = intent.getStringExtra("alert_mode")
            if (alertMode != null && context != null) {
                val newSettings = when (alertMode.lowercase()) {
                    "vibrate_only" -> com.sismos.panama.data.models.AlertSettings.presetVibrateOnly()
                    "sound_only" -> com.sismos.panama.data.models.AlertSettings.presetSoundOnly()
                    "messages_only" -> com.sismos.panama.data.models.AlertSettings.presetMessagesOnly()
                    "disable_all" -> com.sismos.panama.data.models.AlertSettings.presetDisableAll()
                    else -> com.sismos.panama.data.models.AlertSettings.presetAll()
                }
                com.sismos.panama.data.models.AlertSettings.save(context, newSettings)
            }

            val testAlert = intent.getBooleanExtra("test_alert", false)
            if (testAlert && context != null) {
                com.sismos.panama.data.background.SeismicAlarmReceiver.sendTestNotification(
                    context,
                    com.sismos.panama.data.models.AlertSettings.load(context)
                )
            }

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
                "map" -> currentScreenState.value = Screen.Map
                "about" -> currentScreenState.value = Screen.About
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        askNotificationPermission()

        val initialScreen = intent?.getStringExtra("screen")
        when (initialScreen?.lowercase()) {
            "dashboard" -> currentScreenState.value = Screen.Dashboard
            "faults" -> currentScreenState.value = Screen.Faults
            "map" -> currentScreenState.value = Screen.Map
            "about" -> currentScreenState.value = Screen.About
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
            "map" -> currentScreenState.value = Screen.Map
            "about" -> currentScreenState.value = Screen.About
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
    data class Detail(val earthquake: Earthquake) : Screen()
    object Faults : Screen()
    object Dashboard : Screen()
    object Map : Screen()
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
                    screenState.value = Screen.Map
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
                onBack = { screenState.value = Screen.Home },
                onOpenMap = { screenState.value = Screen.Map }
            )
        }
        is Screen.Faults -> {
            FaultsScreen(
                onBack = { screenState.value = Screen.Home }
            )
        }
    }
}
