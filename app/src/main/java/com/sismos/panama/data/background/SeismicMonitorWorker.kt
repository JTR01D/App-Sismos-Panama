package com.sismos.panama.data.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sismos.panama.MainActivity
import com.sismos.panama.R
import com.sismos.panama.data.repository.EarthquakeRepository

class SeismicMonitorWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository = EarthquakeRepository()

    override suspend fun doWork(): Result {
        return try {
            val result = repository.fetchPanamaEarthquakes()
            if (result.isSuccess) {
                val earthquakes = result.getOrNull() ?: emptyList()
                if (earthquakes.isNotEmpty()) {
                    checkAndNotifyNewEarthquake(earthquakes.first())
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkAndNotifyNewEarthquake(latest: com.sismos.panama.data.models.Earthquake) {
        val prefs = appContext.getSharedPreferences("sismos_prefs", Context.MODE_PRIVATE)
        val lastNotifiedId = prefs.getString("last_notified_event_id", null)

        // Si es un evento nuevo y la magnitud es perceptible (M >= 3.5)
        if (latest.id != lastNotifiedId && latest.magnitude >= 3.5) {
            sendNotification(latest)
            prefs.edit().putString("last_notified_event_id", latest.id).apply()
        }
    }

    private fun sendNotification(eq: com.sismos.panama.data.models.Earthquake) {
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "seismic_alerts_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                appContext.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = appContext.getString(R.string.channel_description)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "Alerta Sísmica Panamá: M${eq.magnitude} en ${eq.nearestCity}"
        val content = "${eq.place} | Profundidad: ${eq.depthKm.toInt()} km | Falla: ${eq.nearestFault}"

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(eq.id.hashCode(), builder.build())
    }
}
