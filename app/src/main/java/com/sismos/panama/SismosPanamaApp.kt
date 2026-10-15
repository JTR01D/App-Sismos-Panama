package com.sismos.panama

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sismos.panama.data.background.SeismicMonitorWorker
import java.util.concurrent.TimeUnit

class SismosPanamaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupBackgroundSeismicMonitoring()
        // Iniciar inmediatamente monitoreo rápido de 2 minutos vía AlarmManager
        com.sismos.panama.data.background.SeismicAlarmReceiver.scheduleNext(this, 3_000L)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return
            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            val vibrationPattern = longArrayOf(0, 350, 150, 350, 150, 500)

            // Canal 1: Alerta Completa (Sonido + Vibración)
            val channelAll = NotificationChannel(
                "seismic_alerts_all",
                "Alertas Sísmicas (Sonido y Vibración)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de emergencia con alarma sonora y vibración sísmica"
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }

            // Canal 2: Solo Sonido
            val channelSound = NotificationChannel(
                "seismic_alerts_sound_only",
                "Alertas Sísmicas (Solo Sonido)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de emergencia solo con alarma sonora (sin vibrar)"
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }

            // Canal 3: Solo Vibrar
            val channelVibrate = NotificationChannel(
                "seismic_alerts_vibrate_only",
                "Alertas Sísmicas (Solo Vibrar)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones discretas de emergencia solo con vibración táctil"
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }

            // Canal 4: Solo Mensajes (Silencioso en pantalla)
            val channelMessages = NotificationChannel(
                "seismic_alerts_messages_only",
                "Alertas Sísmicas (Solo Mensajes)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones visuales en pantalla y barra de estado sin interrumpir"
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableLights(false)
            }

            // Canal Legacy / Compatibilidad
            val channelLegacy = NotificationChannel(
                "seismic_alerts_channel",
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_description)
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }

            manager.createNotificationChannel(channelAll)
            manager.createNotificationChannel(channelSound)
            manager.createNotificationChannel(channelVibrate)
            manager.createNotificationChannel(channelMessages)
            manager.createNotificationChannel(channelLegacy)
        }
    }

    private fun setupBackgroundSeismicMonitoring() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<SeismicMonitorWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "seismic_monitor_work",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }
}
