package com.sismos.panama.data.background

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.sismos.panama.MainActivity
import com.sismos.panama.R
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.data.repository.EarthquakeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receptor de alarma de alta prioridad que garantiza consultas sísmicas rápidas cada 2 minutos
 * incluso cuando la app está completamente cerrada o el dispositivo entra en modo Doze (suspensión).
 */
class SeismicAlarmReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = EarthquakeRepository()

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                val result = repository.fetchPanamaEarthquakes()
                if (result.isSuccess) {
                    val list = result.getOrNull() ?: emptyList()
                    if (list.isNotEmpty()) {
                        checkAndNotifyNew(context, list.first())
                    }
                }
            } catch (_: Exception) {
            } finally {
                // Reprogramar la siguiente alarma para dentro de 2 minutos
                scheduleNext(context, INTERVAL_MS)
                pendingResult.finish()
            }
        }
    }

    private fun checkAndNotifyNew(context: Context, latest: Earthquake) {
        val settings = com.sismos.panama.data.models.AlertSettings.load(context)
        if (settings.isCompletelyDisabled()) return

        val prefs = context.getSharedPreferences("sismos_prefs", Context.MODE_PRIVATE)
        val lastNotifiedId = prefs.getString("last_notified_event_id", null)

        // Si es un evento no notificado y cumple el umbral del usuario o es aviso tsunami
        if (latest.id != lastNotifiedId && (latest.magnitude >= settings.minMagnitude || latest.isTsunamiWarning)) {
            sendHighPriorityNotification(context, latest, settings)
            prefs.edit().putString("last_notified_event_id", latest.id).apply()
        }
    }

    companion object {
        const val INTERVAL_MS = 120_000L // 2 minutos para monitoreo rápido

        fun scheduleNext(context: Context, delayMs: Long = INTERVAL_MS) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, SeismicAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = System.currentTimeMillis() + delayMs

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerAtMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerAtMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (_: Exception) {}
        }

        fun sendHighPriorityNotification(
            context: Context,
            eq: Earthquake,
            customSettings: com.sismos.panama.data.models.AlertSettings? = null
        ) {
            val settings = customSettings ?: com.sismos.panama.data.models.AlertSettings.load(context)
            if (settings.isCompletelyDisabled()) return

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Seleccionar canal exacto según preferencias de sonido y vibración
            val channelId = when {
                settings.soundEnabled && settings.vibrateEnabled -> "seismic_alerts_all"
                settings.soundEnabled && !settings.vibrateEnabled -> "seismic_alerts_sound_only"
                !settings.soundEnabled && settings.vibrateEnabled -> "seismic_alerts_vibrate_only"
                else -> "seismic_alerts_messages_only"
            }

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("screen", "home")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                eq.id.hashCode(),
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val modeTag = when {
                settings.soundEnabled && settings.vibrateEnabled -> "⚡"
                settings.soundEnabled -> "🔊"
                settings.vibrateEnabled -> "📳"
                else -> "💬"
            }

            val title = "$modeTag ALERTA SÍSMICA PANAMÁ: M${String.format(java.util.Locale.US, "%.1f", eq.magnitude)}"
            val content = "${eq.place} | Prof: ${eq.depthKm.toInt()} km • ${eq.nearestFault}"

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (settings.soundEnabled) {
                builder.setSound(soundUri)
                builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            } else {
                builder.setSound(null)
                builder.setCategory(NotificationCompat.CATEGORY_EVENT)
            }

            if (settings.vibrateEnabled) {
                builder.setVibrate(longArrayOf(0, 350, 150, 350, 150, 500))
            } else {
                builder.setVibrate(longArrayOf(0))
            }

            if (settings.soundEnabled || settings.vibrateEnabled) {
                builder.setPriority(NotificationCompat.PRIORITY_MAX)
            } else {
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
            }

            notificationManager.notify(eq.id.hashCode(), builder.build())
        }

        /**
         * Envía una notificación de prueba instantánea con la configuración actual del usuario
         */
        fun sendTestNotification(
            context: Context,
            settings: com.sismos.panama.data.models.AlertSettings
        ) {
            val testEarthquake = Earthquake(
                id = "test_alert_${System.currentTimeMillis()}",
                magnitude = 4.5,
                place = "Prueba de Alerta Sísmica - Panamá",
                timeMillis = System.currentTimeMillis(),
                latitude = 8.98,
                longitude = -79.52,
                depthKm = 10.0,
                nearestCity = "Ciudad de Panamá",
                distanceToNearestCityKm = 12,
                nearestFault = "Falla de Pedro Miguel (Canal)",
                faultMechanism = "Rumbo lateral izquierdo",
                panamaRegion = "Canal / Metro",
                isTsunamiWarning = false,
                feltReports = 85,
                officialUrl = "https://www.igc.up.ac.pa",
                sourceName = "Prueba de Configuración Sísmica"
            )
            sendHighPriorityNotification(context, testEarthquake, settings)
        }
    }
}
