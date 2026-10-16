package com.sismos.panama.data.background

import android.content.Context
import android.util.Log
import com.sismos.panama.data.models.AlertSettings
import com.sismos.panama.data.models.Earthquake
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Coordinador centralizado de alertas sísmicas para Panamá.
 *
 * Previene alertas duplicadas y falsas alarmas causadas por:
 * 1. Desconexión y reconexión a Internet (switching entre IGC-UP y USGS).
 * 2. Múltiples ejecutores concurrentes (ViewModel, AlarmReceiver, WorkManager).
 * 3. Eventos históricos o desactualizados (antigüedad > 20 minutos).
 * 4. Discrepancias entre identificadores de fuentes sísmicas distintas.
 */
object SeismicAlertCoordinator {

    private const val TAG = "SeismicAlertCoordinator"
    private const val PREFS_NAME = "sismos_prefs"

    // Claves de SharedPreferences
    private const val KEY_LAST_NOTIFIED_ID = "last_notified_event_id"
    private const val KEY_NOTIFIED_IDS_SET = "notified_event_ids_set"
    private const val KEY_NOTIFIED_EVENTS_CACHE = "notified_events_cache"

    // Límites de tiempo y distancia para deduplicación
    const val MAX_ALERT_AGE_MS = 20 * 60 * 1000L // 20 minutos: sismos más antiguos no activan sirena/alarma
    const val SAME_EVENT_MAX_TIME_DIFF_MS = 20 * 60 * 1000L // 20 minutos entre fuentes distintas
    const val SAME_EVENT_MAX_DIST_KM = 80.0 // 80 km de tolerancia entre epicentros IGC y USGS
    const val SAME_EVENT_MAX_MAG_DIFF = 1.2 // 1.2 unidades de magnitud entre escalas distintas

    private val lock = Any()

    /**
     * Evalúa si un evento sísmico es nuevo, vigente y cumple con los filtros del usuario.
     * Si califica, emite la notificación de alta prioridad y actualiza el registro histórico.
     *
     * @return true si la alerta fue disparada, false si fue descartada (duplicada, obsoleta o filtrada).
     */
    fun checkAndNotify(context: Context, latest: Earthquake, callerTag: String = ""): Boolean {
        synchronized(lock) {
            val settings = AlertSettings.load(context)
            if (settings.isCompletelyDisabled()) {
                Log.d(TAG, "[$callerTag] Alertas completamente deshabilitadas por el usuario.")
                return false
            }

            // 1. Filtro de Magnitud y Tsunami
            if (latest.magnitude < settings.minMagnitude && !latest.isTsunamiWarning) {
                Log.d(TAG, "[$callerTag] Sismo M${latest.magnitude} por debajo del umbral M${settings.minMagnitude}.")
                return false
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastNotifiedId = prefs.getString(KEY_LAST_NOTIFIED_ID, null)
            val notifiedIds = prefs.getStringSet(KEY_NOTIFIED_IDS_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
            val cachedEvents = prefs.getStringSet(KEY_NOTIFIED_EVENTS_CACHE, emptySet())?.toMutableSet() ?: mutableSetOf()

            // 2. Primera ejecución / Cold Start: no asustar con sismos pasados
            if (lastNotifiedId == null && notifiedIds.isEmpty()) {
                Log.i(TAG, "[$callerTag] Inicialización inicial de monitoreo. Registrando evento top sin disparar alarma sonora.")
                saveNotifiedEvent(prefs, latest, notifiedIds, cachedEvents)
                return false
            }

            // 3. Verificación de antigüedad del evento (Frescura)
            val now = System.currentTimeMillis()
            val ageMs = now - latest.timeMillis
            if (ageMs > MAX_ALERT_AGE_MS || ageMs < -5 * 60 * 1000L) {
                Log.d(TAG, "[$callerTag] Evento antiguo descartado para alarma viva. Antigüedad: ${ageMs / 60000} min (ID: ${latest.id})")
                // Guardar en IDs conocidos para no reevaluar
                if (!notifiedIds.contains(latest.id)) {
                    notifiedIds.add(latest.id)
                    prefs.edit().putStringSet(KEY_NOTIFIED_IDS_SET, notifiedIds).apply()
                }
                return false
            }

            // 4. Verificación de ID exacto
            if (latest.id == lastNotifiedId || notifiedIds.contains(latest.id)) {
                Log.d(TAG, "[$callerTag] Evento ya notificado previamente por ID exacto: ${latest.id}")
                return false
            }

            // 5. Deduplicación cruzada entre redes sísmicas (IGC-UP vs USGS)
            // Analiza si este evento coincide espacial y temporalmente con uno ya alertado
            for (cachedRaw in cachedEvents) {
                // Formato: id|timeMillis|lat|lon|magnitude
                val parts = cachedRaw.split("|")
                if (parts.size >= 5) {
                    val cTime = parts[1].toLongOrNull() ?: continue
                    val cLat = parts[2].toDoubleOrNull() ?: continue
                    val cLon = parts[3].toDoubleOrNull() ?: continue
                    val cMag = parts[4].toDoubleOrNull() ?: continue

                    val timeDiff = abs(latest.timeMillis - cTime)
                    val distKm = haversineDistanceKm(latest.latitude, latest.longitude, cLat, cLon)
                    val magDiff = abs(latest.magnitude - cMag)

                    if (timeDiff <= SAME_EVENT_MAX_TIME_DIFF_MS &&
                        distKm <= SAME_EVENT_MAX_DIST_KM &&
                        magDiff <= SAME_EVENT_MAX_MAG_DIFF
                    ) {
                        Log.i(TAG, "[$callerTag] Evento duplicado cruzado detectado (IGC vs USGS). Sismo coincidente en $distKm km y ${timeDiff / 1000}s. Registrando alias ${latest.id}")
                        // Registrar este nuevo ID como alias para que no vuelva a ser evaluado
                        notifiedIds.add(latest.id)
                        prefs.edit().putStringSet(KEY_NOTIFIED_IDS_SET, notifiedIds).apply()
                        return false
                    }
                }
            }

            // 6. ¡Sismo confirmado nuevo y vigente! Emitir alerta de alta prioridad
            Log.w(TAG, "[$callerTag] DISPARANDO ALERTA SÍSMICA: M${latest.magnitude} - ${latest.place} (ID: ${latest.id})")
            saveNotifiedEvent(prefs, latest, notifiedIds, cachedEvents)
            SeismicAlarmReceiver.sendHighPriorityNotification(context, latest, settings)
            return true
        }
    }

    private fun saveNotifiedEvent(
        prefs: android.content.SharedPreferences,
        eq: Earthquake,
        notifiedIds: MutableSet<String>,
        cachedEvents: MutableSet<String>
    ) {
        notifiedIds.add(eq.id)
        val trimmedIds = if (notifiedIds.size > 50) {
            notifiedIds.toList().takeLast(50).toSet()
        } else notifiedIds

        val eventSignature = "${eq.id}|${eq.timeMillis}|${eq.latitude}|${eq.longitude}|${eq.magnitude}"
        cachedEvents.add(eventSignature)
        val trimmedCache = if (cachedEvents.size > 20) {
            cachedEvents.toList().takeLast(20).toSet()
        } else cachedEvents

        prefs.edit()
            .putString(KEY_LAST_NOTIFIED_ID, eq.id)
            .putStringSet(KEY_NOTIFIED_IDS_SET, trimmedIds)
            .putStringSet(KEY_NOTIFIED_EVENTS_CACHE, trimmedCache)
            .apply()
    }

    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
