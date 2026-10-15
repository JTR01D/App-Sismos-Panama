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
            // Asegurar que la alarma esté siempre activa
            SeismicAlarmReceiver.scheduleNext(appContext, SeismicAlarmReceiver.INTERVAL_MS)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkAndNotifyNewEarthquake(latest: com.sismos.panama.data.models.Earthquake) {
        val prefs = appContext.getSharedPreferences("sismos_prefs", Context.MODE_PRIVATE)
        val lastNotifiedId = prefs.getString("last_notified_event_id", null)

        // Si es un evento nuevo y la magnitud es perceptible (M >= 3.0) o aviso tsunami
        if (latest.id != lastNotifiedId && (latest.magnitude >= 3.0 || latest.isTsunamiWarning)) {
            SeismicAlarmReceiver.sendHighPriorityNotification(appContext, latest)
            prefs.edit().putString("last_notified_event_id", latest.id).apply()
        }
    }
}
