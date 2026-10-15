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
            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                "seismic_alerts_channel",
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350, 150, 500)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
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
