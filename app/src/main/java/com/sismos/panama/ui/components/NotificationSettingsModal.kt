package com.sismos.panama.ui.components

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sismos.panama.data.background.SeismicAlarmReceiver
import com.sismos.panama.data.models.AlertSettings

/**
 * Modal Liquid Glass interactivo para la gestión completa de alertas y notificaciones sísmicas.
 * Diseño responsivo adaptativo que garantiza visibilidad y navegación perfecta en cualquier
 * orientación (vertical u horizontal), tamaño de pantalla y escala de texto.
 */
@Composable
fun NotificationSettingsModal(
    onDismiss: () -> Unit,
    onSettingsChanged: (AlertSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenWidthDp = configuration.screenWidthDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || screenWidthDp > screenHeightDp

    val initialSettings = remember { AlertSettings.load(context) }

    var notifEnabled by remember { mutableStateOf(initialSettings.notificationsEnabled) }
    var soundEnabled by remember { mutableStateOf(initialSettings.soundEnabled) }
    var vibrateEnabled by remember { mutableStateOf(initialSettings.vibrateEnabled) }
    var messagesEnabled by remember { mutableStateOf(initialSettings.messagesEnabled) }
    var minMag by remember { mutableFloatStateOf(if (initialSettings.minMagnitude <= 0.0f) 2.0f else initialSettings.minMagnitude) }

    val currentSettings = AlertSettings(
        notificationsEnabled = notifEnabled,
        soundEnabled = soundEnabled,
        vibrateEnabled = vibrateEnabled,
        messagesEnabled = messagesEnabled,
        minMagnitude = if (minMag <= 2.0f) 0.0f else minMag
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.88f else 0.94f)
                .widthIn(max = if (isLandscape) 640.dp else 440.dp)
                .fillMaxHeight(if (isLandscape) 0.94f else 0.90f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF131D2E),
                            Color(0xFF0F172A),
                            Color(0xFF0A0F1D)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x6600E5FF), Color(0x223186FF), Color(0x10FFFFFF))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(
                    horizontal = if (isLandscape) 16.dp else 20.dp,
                    vertical = if (isLandscape) 12.dp else 16.dp
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val headerIcon = when {
                            currentSettings.isCompletelyDisabled() -> Icons.Default.NotificationsOff
                            soundEnabled && vibrateEnabled -> Icons.Default.NotificationsActive
                            vibrateEnabled -> Icons.Default.Vibration
                            soundEnabled -> Icons.Default.VolumeUp
                            else -> Icons.Default.Message
                        }

                        val headerGlowColor = when {
                            currentSettings.isCompletelyDisabled() -> Color(0xFFEF4444)
                            soundEnabled && vibrateEnabled -> Color(0xFF00E5FF)
                            vibrateEnabled -> Color(0xFFF59E0B)
                            soundEnabled -> Color(0xFF38BDF8)
                            else -> Color(0xFFA78BFA)
                        }

                        Box(
                            modifier = Modifier
                                .size(if (isLandscape) 38.dp else 44.dp)
                                .clip(CircleShape)
                                .background(headerGlowColor.copy(alpha = 0.18f))
                                .border(1.dp, headerGlowColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = headerGlowColor,
                                modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ALERTAS Y NOTIFICACIONES",
                                fontSize = if (isLandscape) 14.sp else 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Modo: ${currentSettings.getModeDescription().uppercase()}",
                                fontSize = if (isLandscape) 10.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = headerGlowColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(if (isLandscape) 30.dp else 34.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(if (isLandscape) 16.dp else 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 14.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // SECCIÓN 1: MODOS RÁPIDOS EN 1 TOQUE (PRESETS)
                    Text(
                        text = "ACCESOS RÁPIDOS (1 TOQUE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isLandscape) {
                        // En horizontal: 4 accesos rápidos en 1 sola fila para ahorrar espacio vertical
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip(
                                title = "⚡ Completa",
                                subtitle = "Sonido + Vibrar",
                                isSelected = notifEnabled && soundEnabled && vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFF00E5FF),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = true
                                    vibrateEnabled = true
                                    messagesEnabled = true
                                }
                            )

                            PresetChip(
                                title = "📳 Solo Vibrar",
                                subtitle = "Táctil discreto",
                                isSelected = notifEnabled && !soundEnabled && vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = false
                                    vibrateEnabled = true
                                    messagesEnabled = true
                                }
                            )

                            PresetChip(
                                title = "🔊 Solo Sonido",
                                subtitle = "Alarma sin vibrar",
                                isSelected = notifEnabled && soundEnabled && !vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = true
                                    vibrateEnabled = false
                                    messagesEnabled = true
                                }
                            )

                            PresetChip(
                                title = "💬 Solo Mensajes",
                                subtitle = "Silencioso pantalla",
                                isSelected = notifEnabled && !soundEnabled && !vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFFA78BFA),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = false
                                    vibrateEnabled = false
                                    messagesEnabled = true
                                }
                            )
                        }
                    } else {
                        // En vertical: 2 filas de 2 accesos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip(
                                title = "⚡ Completa",
                                subtitle = "Sonido + Vibrar",
                                isSelected = notifEnabled && soundEnabled && vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFF00E5FF),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = true
                                    vibrateEnabled = true
                                    messagesEnabled = true
                                }
                            )

                            PresetChip(
                                title = "📳 Solo Vibrar",
                                subtitle = "Táctil discreto",
                                isSelected = notifEnabled && !soundEnabled && vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = false
                                    vibrateEnabled = true
                                    messagesEnabled = true
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip(
                                title = "🔊 Solo Sonido",
                                subtitle = "Alarma sin vibrar",
                                isSelected = notifEnabled && soundEnabled && !vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = true
                                    vibrateEnabled = false
                                    messagesEnabled = true
                                }
                            )

                            PresetChip(
                                title = "💬 Solo Mensajes",
                                subtitle = "Silencioso en pantalla",
                                isSelected = notifEnabled && !soundEnabled && !vibrateEnabled && messagesEnabled,
                                activeColor = Color(0xFFA78BFA),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    notifEnabled = true
                                    soundEnabled = false
                                    vibrateEnabled = false
                                    messagesEnabled = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Botón Desactivar Todas
                    PresetChip(
                        title = "🔕 Desactivar Todas",
                        subtitle = "Silencio absoluto / Desactivar notificaciones",
                        isSelected = currentSettings.isCompletelyDisabled(),
                        activeColor = Color(0xFFEF4444),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            notifEnabled = false
                            soundEnabled = false
                            vibrateEnabled = false
                            messagesEnabled = false
                        }
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 18.dp))

                    // SECCIÓN 2: INTERRUPTORES INDIVIDUALES CON LIBERTAD TOTAL
                    Text(
                        text = "SELECCIÓN MANUAL INDIVIDUAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        borderWidth = 0.8.dp
                    ) {
                        Column(modifier = Modifier.padding(if (isLandscape) 10.dp else 14.dp)) {
                            // Toggle 1: Notificaciones Sísmicas (General)
                            SettingToggleRow(
                                icon = Icons.Default.Notifications,
                                title = "Sistema de Notificaciones",
                                description = if (notifEnabled) "Monitoreo activo las 24 horas" else "Desactivadas por completo",
                                checked = notifEnabled,
                                activeColor = Color(0xFF00E5FF),
                                onCheckedChange = { isChecked ->
                                    notifEnabled = isChecked
                                    if (!isChecked) {
                                        soundEnabled = false
                                        vibrateEnabled = false
                                        messagesEnabled = false
                                    } else {
                                        messagesEnabled = true
                                        soundEnabled = true
                                        vibrateEnabled = true
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

                            // Toggle 2: Sonido de Alarma
                            SettingToggleRow(
                                icon = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                title = "Sonido de Alarma Sísmica",
                                description = "Alerta audible de alta prioridad (nivel USAGE_ALARM)",
                                checked = soundEnabled,
                                enabled = notifEnabled,
                                activeColor = Color(0xFF38BDF8),
                                onCheckedChange = { isChecked ->
                                    soundEnabled = isChecked
                                    if (isChecked && !notifEnabled) notifEnabled = true
                                }
                            )

                            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

                            // Toggle 3: Solo Vibrar / Vibración
                            SettingToggleRow(
                                icon = Icons.Default.Vibration,
                                title = "Vibración Táctil de Emergencia",
                                description = "Patrón de pulso sísmico (0, 350, 150, 350, 500 ms)",
                                checked = vibrateEnabled,
                                enabled = notifEnabled,
                                activeColor = Color(0xFFF59E0B),
                                onCheckedChange = { isChecked ->
                                    vibrateEnabled = isChecked
                                    if (isChecked && !notifEnabled) notifEnabled = true
                                }
                            )

                            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

                            // Toggle 4: Solo Mensajes / Visuales
                            SettingToggleRow(
                                icon = Icons.Default.Message,
                                title = "Mensajes y Banners Visuales",
                                description = "Avisos emergentes (Heads-up) y en pantalla de bloqueo",
                                checked = messagesEnabled,
                                enabled = notifEnabled,
                                activeColor = Color(0xFFA78BFA),
                                onCheckedChange = { isChecked ->
                                    messagesEnabled = isChecked
                                    if (isChecked && !notifEnabled) notifEnabled = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 16.dp))

                    // SECCIÓN 3: BARRA INTERACTIVA DE MAGNITUD MÍNIMA (SLIDER)
                    val sliderSeverityColor = when {
                        minMag <= 2.0f -> Color(0xFF00E676)
                        minMag < 4.0f -> Color(0xFF00E5FF)
                        minMag < 5.0f -> Color(0xFFFFD600)
                        minMag < 6.0f -> Color(0xFFFF6D00)
                        else -> Color(0xFFFF1744)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RANGO DE ALERTA POR MAGNITUD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.8.sp
                        )

                        // Badge interactivo de magnitud actual
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(sliderSeverityColor.copy(alpha = 0.2f))
                                .border(1.dp, sliderSeverityColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (minMag <= 2.0f) "Todos los sismos" else "M ≥ ${String.format(Locale.US, "%.1f", minMag)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = sliderSeverityColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        borderWidth = 0.8.dp
                    ) {
                        Column(modifier = Modifier.padding(if (isLandscape) 10.dp else 14.dp)) {
                            val magSeverityDesc = when {
                                minMag <= 2.0f -> "🟢 Sin filtro: Recibirás alerta de absolutamente todos los sismos detectados."
                                minMag < 4.0f -> "🔵 Leves en adelante: Sentidos en reposo o pisos altos (M ≥ ${String.format(Locale.US, "%.1f", minMag)})."
                                minMag < 5.0f -> "🟡 Moderados en adelante: Sentidos ampliamente por la población (M ≥ ${String.format(Locale.US, "%.1f", minMag)})."
                                minMag < 6.0f -> "🟠 Fuertes en adelante: Sacudida notable y potencial daño local (M ≥ ${String.format(Locale.US, "%.1f", minMag)})."
                                else -> "🔴 Severos únicamente: Sismos destructivos o de gran magnitud (M ≥ ${String.format(Locale.US, "%.1f", minMag)})."
                            }

                            Text(
                                text = magSeverityDesc,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Barra Slider para mover la magnitud libremente
                            Slider(
                                value = minMag.coerceIn(2.0f, 6.5f),
                                onValueChange = { newValue ->
                                    minMag = round(newValue * 10f) / 10f
                                },
                                valueRange = 2.0f..6.5f,
                                steps = 44, // Saltos de 0.1 en 0.1
                                colors = SliderDefaults.colors(
                                    thumbColor = sliderSeverityColor,
                                    activeTrackColor = sliderSeverityColor,
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Indicadores en extremos de la barra
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "2.0 (Todos)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "4.0",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "6.5+ (Severos)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Chips de acceso rápido para saltar a valores clave
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    2.0f to "Todos",
                                    3.0f to "M ≥ 3.0",
                                    4.0f to "M ≥ 4.0",
                                    5.0f to "M ≥ 5.0",
                                    6.0f to "M ≥ 6.0"
                                ).forEach { (mag, label) ->
                                    val isSelected = abs(minMag - mag) < 0.05f
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color(0x333186FF) else Color(0x12FFFFFF)
                                            )
                                            .border(
                                                width = 0.8.dp,
                                                color = if (isSelected) Color(0xFF60A5FA) else Color(0x1AFFFFFF),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { minMag = mag }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF93C5FD) else Color(0xFF94A3B8),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 16.dp))

                    // BOTÓN DE PRUEBA INMEDIATA
                    Button(
                        onClick = {
                            if (currentSettings.isCompletelyDisabled()) {
                                Toast.makeText(
                                    context,
                                    "⚠️ Las notificaciones están desactivadas. Activa una opción para probar.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                SeismicAlarmReceiver.sendTestNotification(context, currentSettings)
                                Toast.makeText(
                                    context,
                                    "⚡ Alerta de prueba enviada en modo: ${currentSettings.getModeDescription()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 40.dp else 44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x2200E5FF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Probar Alerta Ahora",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } // FIN DEL CUERPO CON SCROLL

                Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

                // BOTONES CERRAR Y GUARDAR (FOOTER FIJO ANCLADO AL FONDO)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x18FFFFFF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLandscape) 42.dp else 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cerrar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            AlertSettings.save(context, currentSettings)
                            onSettingsChanged(currentSettings)
                            Toast.makeText(
                                context,
                                "✓ Configuración de alertas guardada",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(if (isLandscape) 42.dp else 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3186FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Guardar",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) activeColor.copy(alpha = 0.22f) else Color(0x12FFFFFF)
    val border = if (isSelected) activeColor else Color(0x1AFFFFFF)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) activeColor else Color.White
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color(0xFF94A3B8),
            maxLines = 1
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    activeColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (checked && enabled) activeColor.copy(alpha = 0.2f) else Color(0x14FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked && enabled) activeColor else Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) Color.White else Color(0xFF64748B)
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0x22FFFFFF),
                disabledCheckedTrackColor = activeColor.copy(alpha = 0.4f),
                disabledUncheckedTrackColor = Color(0x11FFFFFF)
            )
        )
    }
}
