package com.sismos.panama.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
 * Otorga libertad total para seleccionar Solo Vibrar, Solo Sonido, Solo Mensajes, o Desactivar Todas.
 */
@Composable
fun NotificationSettingsModal(
    onDismiss: () -> Unit,
    onSettingsChanged: (AlertSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val initialSettings = remember { AlertSettings.load(context) }

    var notifEnabled by remember { mutableStateOf(initialSettings.notificationsEnabled) }
    var soundEnabled by remember { mutableStateOf(initialSettings.soundEnabled) }
    var vibrateEnabled by remember { mutableStateOf(initialSettings.vibrateEnabled) }
    var messagesEnabled by remember { mutableStateOf(initialSettings.messagesEnabled) }
    var minMag by remember { mutableFloatStateOf(initialSettings.minMagnitude) }

    val currentSettings = AlertSettings(
        notificationsEnabled = notifEnabled,
        soundEnabled = soundEnabled,
        vibrateEnabled = vibrateEnabled,
        messagesEnabled = messagesEnabled,
        minMagnitude = minMag
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(headerGlowColor.copy(alpha = 0.18f))
                                .border(1.dp, headerGlowColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = headerGlowColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ALERTAS Y NOTIFICACIONES",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Modo: ${currentSettings.getModeDescription().uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = headerGlowColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECCIÓN 1: MODOS RÁPIDOS EN 1 TOQUE (PRESETS)
                Text(
                    text = "ACCESOS RÁPIDOS (1 TOQUE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Botón Completa
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

                    // Botón Solo Vibrar
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
                    // Botón Solo Sonido
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

                    // Botón Solo Mensajes
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

                Spacer(modifier = Modifier.height(18.dp))

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
                    Column(modifier = Modifier.padding(14.dp)) {
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

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN 3: UMBRAL DE MAGNITUD MÍNIMA
                Text(
                    text = "ALERTAR A PARTIR DE MAGNITUD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0.0f to "Todos",
                        3.0f to "M ≥ 3.0",
                        4.0f to "M ≥ 4.0",
                        5.0f to "M ≥ 5.0"
                    ).forEach { (mag, label) ->
                        val isSelected = minMag == mag
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(0xFF3186FF) else Color(0x1AFFFFFF)
                                )
                                .border(
                                    width = 0.8.dp,
                                    color = if (isSelected) Color(0xFF60A5FA) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { minMag = mag }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                        .height(44.dp),
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

                Spacer(modifier = Modifier.height(10.dp))

                // BOTÓN GUARDAR Y APLICAR
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
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3186FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Guardar y Aplicar",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
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
