package com.sismos.panama.data.models

import android.content.Context
import android.content.SharedPreferences

/**
 * Modelo de configuración y preferencias de alertas y notificaciones sísmicas.
 * Brinda total libertad al usuario para configurar sonido, vibración, mensajes visuales,
 * o desactivar completamente el sistema.
 */
data class AlertSettings(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val messagesEnabled: Boolean = true,
    val minMagnitude: Float = 3.0f
) {
    /**
     * Retorna verdadero si todas las alertas están apagadas por el usuario.
     */
    fun isCompletelyDisabled(): Boolean {
        return !notificationsEnabled || (!soundEnabled && !vibrateEnabled && !messagesEnabled)
    }

    /**
     * Devuelve el modo actual descriptivo para la interfaz.
     */
    fun getModeDescription(): String {
        return when {
            isCompletelyDisabled() -> "Desactivadas"
            soundEnabled && vibrateEnabled && messagesEnabled -> "Alerta Completa"
            soundEnabled && !vibrateEnabled && messagesEnabled -> "Solo Sonido"
            !soundEnabled && vibrateEnabled && messagesEnabled -> "Solo Vibrar"
            !soundEnabled && !vibrateEnabled && messagesEnabled -> "Solo Mensajes"
            soundEnabled && vibrateEnabled && !messagesEnabled -> "Sonido + Vibrar"
            soundEnabled && !vibrateEnabled && !messagesEnabled -> "Solo Sonido"
            !soundEnabled && vibrateEnabled && !messagesEnabled -> "Solo Vibrar"
            else -> "Personalizado"
        }
    }

    companion object {
        private const val PREFS_NAME = "sismos_prefs"
        private const val KEY_NOTIF_ENABLED = "alerts_notifications_enabled"
        private const val KEY_SOUND_ENABLED = "alerts_sound_enabled"
        private const val KEY_VIBRATE_ENABLED = "alerts_vibrate_enabled"
        private const val KEY_MESSAGES_ENABLED = "alerts_messages_enabled"
        private const val KEY_MIN_MAGNITUDE = "alerts_min_magnitude"

        fun load(context: Context): AlertSettings {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return AlertSettings(
                notificationsEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
                soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
                vibrateEnabled = prefs.getBoolean(KEY_VIBRATE_ENABLED, true),
                messagesEnabled = prefs.getBoolean(KEY_MESSAGES_ENABLED, true),
                minMagnitude = prefs.getFloat(KEY_MIN_MAGNITUDE, 3.0f)
            )
        }

        fun save(context: Context, settings: AlertSettings) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_NOTIF_ENABLED, settings.notificationsEnabled)
                .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
                .putBoolean(KEY_VIBRATE_ENABLED, settings.vibrateEnabled)
                .putBoolean(KEY_MESSAGES_ENABLED, settings.messagesEnabled)
                .putFloat(KEY_MIN_MAGNITUDE, settings.minMagnitude)
                .apply()
        }

        // Modos Rápidos (Presets)
        fun presetAll(minMag: Float = 3.0f) = AlertSettings(
            notificationsEnabled = true,
            soundEnabled = true,
            vibrateEnabled = true,
            messagesEnabled = true,
            minMagnitude = minMag
        )

        fun presetVibrateOnly(minMag: Float = 3.0f) = AlertSettings(
            notificationsEnabled = true,
            soundEnabled = false,
            vibrateEnabled = true,
            messagesEnabled = true,
            minMagnitude = minMag
        )

        fun presetSoundOnly(minMag: Float = 3.0f) = AlertSettings(
            notificationsEnabled = true,
            soundEnabled = true,
            vibrateEnabled = false,
            messagesEnabled = true,
            minMagnitude = minMag
        )

        fun presetMessagesOnly(minMag: Float = 3.0f) = AlertSettings(
            notificationsEnabled = true,
            soundEnabled = false,
            vibrateEnabled = false,
            messagesEnabled = true,
            minMagnitude = minMag
        )

        fun presetDisableAll(minMag: Float = 3.0f) = AlertSettings(
            notificationsEnabled = false,
            soundEnabled = false,
            vibrateEnabled = false,
            messagesEnabled = false,
            minMagnitude = minMag
        )
    }
}
