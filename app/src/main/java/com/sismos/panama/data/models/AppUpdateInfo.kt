package com.sismos.panama.data.models

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val releasePageUrl: String,
    val publishedDate: String,
    val apkSizeBytes: Long = 0L
) {
    fun getFormattedApkSize(): String {
        if (apkSizeBytes <= 0L) return ""
        val mb = apkSizeBytes / (1024.0 * 1024.0)
        return String.format(java.util.Locale.US, "%.1f MB", mb)
    }
}
