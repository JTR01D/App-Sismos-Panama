package com.sismos.panama.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.BuildConfig
import com.sismos.panama.data.models.AlertSettings
import com.sismos.panama.data.models.AppUpdateInfo
import com.sismos.panama.data.repository.UpdateRepository
import com.sismos.panama.data.repository.InAppUpdateManager
import java.io.File
import com.sismos.panama.ui.components.NotificationSettingsModal
import com.sismos.panama.ui.theme.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showNotificationSettings by remember { mutableStateOf(false) }
    var alertSettings by remember { mutableStateOf(AlertSettings.load(context)) }

    val topBarBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF161F30),
            Color(0xFF0F1724)
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
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Acerca de la Aplicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Sismos Panamá • Información y Tecnología",
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
            // Hero Banner de la Aplicación con Glassmorphism
            item {
                AppHeroCard()
            }

            // Actualizaciones Oficiales desde GitHub
            item {
                AboutGitHubUpdatesCard(context = context)
            }

            // Tarjeta de Configuración de Alertas Sísmicas (Acceso Directo)
            item {
                AboutAlertSettingsCard(
                    alertSettings = alertSettings,
                    onClick = { showNotificationSettings = true }
                )
            }

            // Propósito y Misión Ciudadana
            item {
                AboutMissionCard()
            }

            // Módulos y Tecnologías Principales
            item {
                AboutModulesCard()
            }

            // Fuentes Oficiales y Científicas
            item {
                AboutSourcesCard(context = context)
            }

            // Números y Líneas de Emergencia en Panamá
            item {
                EmergencyContactsCard(context = context)
            }

            // Créditos y Reconocimientos
            item {
                AboutCreditsCard(context = context)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showNotificationSettings) {
        NotificationSettingsModal(
            onDismiss = { showNotificationSettings = false },
            onSettingsChanged = { alertSettings = it }
        )
    }
}

@Composable
private fun AboutAlertSettingsCard(
    alertSettings: AlertSettings,
    onClick: () -> Unit
) {
    val accentColor = when {
        alertSettings.isCompletelyDisabled() -> Color(0xFFEF4444)
        alertSettings.soundEnabled && alertSettings.vibrateEnabled -> Color(0xFF00E5FF)
        alertSettings.vibrateEnabled -> Color(0xFFF59E0B)
        alertSettings.soundEnabled -> Color(0xFF38BDF8)
        else -> Color(0xFFA78BFA)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f))
                            .border(0.8.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alertSettings.isCompletelyDisabled()) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "ALERTAS Y NOTIFICACIONES",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Modo: ${alertSettings.getModeDescription()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x223186FF),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0x553186FF))
                ) {
                    Text(
                        text = "Configurar ❯",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges de estado individual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlertStatusPill(
                    label = "🔊 Sonido",
                    isActive = alertSettings.soundEnabled && alertSettings.notificationsEnabled,
                    activeColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                AlertStatusPill(
                    label = "📳 Vibrar",
                    isActive = alertSettings.vibrateEnabled && alertSettings.notificationsEnabled,
                    activeColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                AlertStatusPill(
                    label = "💬 Mensajes",
                    isActive = alertSettings.messagesEnabled && alertSettings.notificationsEnabled,
                    activeColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AlertStatusPill(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) activeColor.copy(alpha = 0.15f) else Color(0x11FFFFFF)
    val border = if (isActive) activeColor.copy(alpha = 0.4f) else Color(0x1AFFFFFF)
    val textCol = if (isActive) activeColor else Color(0xFF64748B)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.6.dp, border, RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label: ${if (isActive) "ON" else "OFF"}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

@Composable
private fun AboutGitHubUpdatesCard(
    context: Context,
    modifier: Modifier = Modifier
) {
    val updateRepo = remember { UpdateRepository() }
    val updateManager = remember { InAppUpdateManager(context) }
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadPercent by remember { mutableStateOf(0) }
    var downloadStatusText by remember { mutableStateOf<String?>(null) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isChecking = true
        checkError = null
        val result = updateRepo.checkForUpdates()
        result.onSuccess {
            updateInfo = it
            isChecking = false
        }.onFailure {
            checkError = it.message
            isChecking = false
        }
    }

    val cardBorderBrush = when {
        updateInfo?.isUpdateAvailable == true -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFFB300)))
        else -> Brush.linearGradient(listOf(Color(0x333186FF), Color(0x2200E5FF)))
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Permiso de Instalación",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Para instalar la actualización directamente desde Sismos Panamá sin usar el navegador, Android requiere que permitas 'Instalar aplicaciones desconocidas' para esta aplicación.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        updateManager.openInstallPermissionSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Abrir Ajustes", color = Color(0xFF070B13), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderBrush),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x2200E5FF))
                            .border(0.8.dp, Color(0x6600E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "ACTUALIZACIONES GITHUB",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isChecking) "Comprobando lanzamientos..."
                            else if (updateInfo?.isUpdateAvailable == true) "¡Nueva versión ${updateInfo?.latestVersion} disponible!"
                            else "Estás al día (v${BuildConfig.VERSION_NAME})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (updateInfo?.isUpdateAvailable == true) Color(0xFF00E5FF) else Color(0xFF10B981)
                        )
                    }
                }

                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF00E5FF),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isChecking = true
                                checkError = null
                                updateRepo.checkForUpdates()
                                    .onSuccess { updateInfo = it }
                                    .onFailure { checkError = it.message }
                                isChecking = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Buscar actualizaciones",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (updateInfo?.isUpdateAvailable == true) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1A00E5FF))
                        .border(0.8.dp, Color(0x4400E5FF), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = updateInfo?.releaseTitle ?: "Nueva versión",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (!updateInfo?.releaseNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateInfo?.releaseNotes ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1A00E5FF))
                            .border(0.8.dp, Color(0x4400E5FF), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Descargando actualización...",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$downloadPercent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0x3300E5FF)
                        )
                        if (downloadStatusText != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = downloadStatusText ?: "",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else if (downloadStatusText != null && downloadedApkFile == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = downloadStatusText ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (downloadedApkFile != null && downloadedApkFile!!.exists()) {
                        Button(
                            onClick = {
                                if (updateManager.canInstallApks()) {
                                    updateManager.installApk(downloadedApkFile!!)
                                } else {
                                    showPermissionDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Instalar Actualización",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                val url = updateInfo?.apkDownloadUrl ?: return@Button
                                isDownloading = true
                                downloadPercent = 0
                                downloadStatusText = "Iniciando descarga..."
                                scope.launch {
                                    updateManager.downloadApk(url) { percent, downloaded, total ->
                                        downloadPercent = percent
                                        val downloadedMb = String.format(java.util.Locale.US, "%.1f", downloaded / (1024f * 1024f))
                                        val totalMb = if (total > 0) String.format(java.util.Locale.US, "%.1f", total / (1024f * 1024f)) else "?"
                                        downloadStatusText = "$percent% ($downloadedMb MB de $totalMb MB)"
                                    }.onSuccess { file ->
                                        isDownloading = false
                                        downloadedApkFile = file
                                        downloadStatusText = "¡Descarga completa!"
                                        if (updateManager.canInstallApks()) {
                                            updateManager.installApk(file)
                                        } else {
                                            showPermissionDialog = true
                                        }
                                    }.onFailure { err ->
                                        isDownloading = false
                                        downloadStatusText = "Error al descargar: ${err.message}"
                                    }
                                }
                            },
                            enabled = !isDownloading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color(0xFF070B13),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDownloading) "Descargando..." else "Descargar e Instalar" + (if (updateInfo?.getFormattedApkSize()?.isNotBlank() == true) " (${updateInfo?.getFormattedApkSize()})" else ""),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF070B13)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo?.releasePageUrl))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x66FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Ver en GitHub", fontSize = 11.sp)
                    }
                }
            } else if (checkError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: $checkError (Se conectará cuando el repositorio esté público).",
                    fontSize = 10.5.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Canal oficial: github.com/JTR01D/App-Sismos-Panama",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Ver repositorio ❯",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JTR01D/App-Sismos-Panama"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeroCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseHero")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF131D2E),
                        Color(0xFF0F1826)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF3186FF).copy(alpha = pulseAlpha),
                        Color(0xFF00E5FF).copy(alpha = 0.2f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emblema Sísmico Circular con Pulso
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF3186FF).copy(alpha = 0.35f),
                                Color(0xFF0D1726)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFF3186FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "Emblema Sísmico",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SISMOS PANAMÁ",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Red Sismológica & Explorador Tectónico Oficial",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Versión y Estado de Compilación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x333186FF),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x663186FF))
                ) {
                    Text(
                        text = "v2.5.0 Pro Tectónica",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x3300E676),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x6600E676))
                ) {
                    Text(
                        text = "En Vivo • 24/7",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutMissionCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, StrokeDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PROPÓSITO Y CIENCIA CIUDADANA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "La República de Panamá se sitúa en un entorno geológico único en el mundo: una zona de convergencia activa entre cuatro entidades tectónicas principales (la Placa de Nazca, la Placa de Cocos, la Placa del Caribe y la Microplaca de Panamá).\n\nEsta aplicación nace con el objetivo de democratizar la información sismológica y acercarla a la población panameña de forma clara, moderna e interactiva, facilitando la comprensión de enjambres sísmicos, fallas geológicas y propagación de ondas.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
private fun AboutModulesCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, StrokeDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MÓDULOS DEL SISTEMA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA78BFA),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ModuleItem(
                icon = Icons.Default.Explore,
                iconColor = Color(0xFF00E5FF),
                title = "Explorador Tectónico 2D de Panamá",
                description = "Mapa cartográfico interactivo que visualiza las fallas activas (Soná-Tonosí, Gatún, Zona de Fractura de Panamá) y sismicidad histórica y en tiempo real."
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleItem(
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFFFD600),
                title = "Detector Inteligente de Enjambres",
                description = "Agrupamiento de sismos por zona y tiempo con identificación del sismo más reciente (★ #1 RECIENTE) mediante pulsos concéntricos de radar."
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleItem(
                icon = Icons.Default.Waves,
                iconColor = Color(0xFF38BDF8),
                title = "Simulador Físico de Ondas P & S",
                description = "Modelo animado en el detalle de cada sismo que calcula los frentes de onda primarios (compresión) y secundarios (corte) con velocidades de la corteza panameña."
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleItem(
                icon = Icons.Default.Equalizer,
                iconColor = Color(0xFF3186FF),
                title = "Dashboard y Estadísticas en Vivo",
                description = "Gráficos de distribución de magnitudes, histogramas de profundidad hipocentral (someros vs profundos) y comparativas de actividad en 24h y 7 días."
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleItem(
                icon = Icons.Default.NotificationsActive,
                iconColor = Color(0xFFFF5252),
                title = "Sistema de Alertas y Notificaciones",
                description = "Canal de alerta temprana con prioridad alta en segundo plano, clasificado según la escala de potencial destructivo y alerta de tsunami."
            )
        }
    }
}

@Composable
private fun ModuleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f))
                .border(0.8.dp, iconColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun AboutSourcesCard(context: Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, StrokeDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF4ADE80),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REDES CIENTÍFICAS Y FUENTES OFICIALES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Los datos sísmicos provienen de redes sismológicas reconocidas y calibradas internacionalmente:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            SourceLinkItem(
                name = "IGC - Instituto de Geociencias (UP)",
                subtitle = "Red Sismológica Nacional de referencia oficial en Panamá",
                url = "http://www.igc.up.ac.pa",
                context = context
            )

            Spacer(modifier = Modifier.height(8.dp))

            SourceLinkItem(
                name = "USGS (U.S. Geological Survey)",
                subtitle = "Monitoreo global y regional de sismos en tiempo real",
                url = "https://earthquake.usgs.gov",
                context = context
            )

            Spacer(modifier = Modifier.height(8.dp))

            SourceLinkItem(
                name = "RSN (Costa Rica) & NOAA / PTWC",
                subtitle = "Monitoreo fronterizo y alertas de tsunami en el Pacífico",
                url = "https://www.tsunami.gov",
                context = context
            )
        }
    }
}

@Composable
private fun SourceLinkItem(
    name: String,
    subtitle: String,
    url: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1724))
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Abrir enlace",
            tint = Color(0xFF60A5FA),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EmergencyContactsCard(context: Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x44FF5252)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LÍNEAS DE EMERGENCIA EN PANAMÁ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Toca cualquier número para marcar directamente en caso de emergencia sísmica o réplicas fuertes:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyButton(
                    modifier = Modifier.weight(1f),
                    title = "SINAPROC",
                    subtitle = "Protección Civil",
                    phone = "911",
                    color = Color(0xFFFF9100),
                    context = context
                )
                EmergencyButton(
                    modifier = Modifier.weight(1f),
                    title = "BOMBEROS",
                    subtitle = "Rescate y Fuego",
                    phone = "103",
                    color = Color(0xFFFF5252),
                    context = context
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyButton(
                    modifier = Modifier.weight(1f),
                    title = "CRUZ ROJA",
                    subtitle = "Ambulancia / Salud",
                    phone = "455",
                    color = Color(0xFFE53935),
                    context = context
                )
                EmergencyButton(
                    modifier = Modifier.weight(1f),
                    title = "POLICÍA",
                    subtitle = "Seguridad Nacional",
                    phone = "104",
                    color = Color(0xFF29B6F6),
                    context = context
                )
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    phone: String,
    color: Color,
    context: Context
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.8.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "$title ($phone)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun AboutCreditsCard(context: Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, StrokeDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de Desarrollador estilo Liquid Glass (similar a WhatsApp Sticker Maker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F1724))
                    .border(0.8.dp, Color(0x333186FF), RoundedCornerShape(14.dp))
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JTR01D"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x223186FF))
                        .border(1.dp, Color(0x553186FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DESARROLLADOR ÚNICO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "JTR01D",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF60A5FA),
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "github.com/JTR01D",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38BDF8)
                    )
                }

                // Insignia GitHub interactiva
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x2238BDF8),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x6638BDF8))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GitHub",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de Acción (GitHub del Proyecto + Compartir App)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JTR01D/App-Sismos-Panama"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x553186FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Repositorio",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        try {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Descarga Sismos Panamá por JTR01D: Monitor sísmico en tiempo real, explorador de fallas tectónicas y alertas tempranas para Panamá: https://github.com/JTR01D/App-Sismos-Panama"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir Sismos Panamá"))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x5500E5FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Compartir",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
