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
        SeismicAlertCoordinator.checkAndNotify(appContext, latest, callerTag = "WorkManager")
    }
}
