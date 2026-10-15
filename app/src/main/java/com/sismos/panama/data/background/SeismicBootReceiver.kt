package com.sismos.panama.data.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SeismicBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Inicializar monitoreo periódico de alarma de 2 minutos
            SeismicAlarmReceiver.scheduleNext(context, 10_000L)
        }
    }
}
