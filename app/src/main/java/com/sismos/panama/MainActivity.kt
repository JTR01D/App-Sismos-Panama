package com.sismos.panama

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.ui.screens.DetailScreen
import com.sismos.panama.ui.screens.FaultsScreen
import com.sismos.panama.ui.screens.HomeScreen
import com.sismos.panama.ui.theme.SismosPanamaTheme
import com.sismos.panama.ui.viewmodel.SeismicViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permiso procesado
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        setContent {
            SismosPanamaTheme {
                SismosAppNavigation()
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
    }
}

sealed class Screen {
    object Home : Screen()
    data class Detail(val earthquake: Earthquake) : Screen()
    object Faults : Screen()
}

@Composable
fun SismosAppNavigation() {
    val viewModel: SeismicViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                viewModel = viewModel,
                onEarthquakeSelected = { eq ->
                    currentScreen = Screen.Detail(eq)
                },
                onNavigateToFaults = {
                    currentScreen = Screen.Faults
                }
            )
        }
        is Screen.Detail -> {
            DetailScreen(
                earthquake = screen.earthquake,
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.Faults -> {
            FaultsScreen(
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
}
