package com.sismos.panama.data.repository

import com.sismos.panama.BuildConfig
import com.sismos.panama.data.models.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UpdateRepository(
    private val repoOwner: String = "JTR01D",
    private val repoName: String = "App-Sismos-Panama"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val currentVersion = BuildConfig.VERSION_NAME
        val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"

        try {
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "SismosPanama-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 404) {
                    // No releases published yet on repository
                    return@withContext Result.success(
                        AppUpdateInfo(
                            isUpdateAvailable = false,
                            latestVersion = currentVersion,
                            currentVersion = currentVersion,
                            releaseTitle = "Versión en desarrollo",
                            releaseNotes = "Estás utilizando la versión compilada más reciente de Sismos Panamá.",
                            apkDownloadUrl = "https://github.com/$repoOwner/$repoName/releases",
                            releasePageUrl = "https://github.com/$repoOwner/$repoName/releases",
                            publishedDate = ""
                        )
                    )
                }

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Error al consultar GitHub: HTTP ${response.code}"))
                }

                val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía de GitHub"))
                val json = JSONObject(bodyString)

                val tagName = json.optString("tag_name", "v$currentVersion")
                val releaseTitle = json.optString("name", "Sismos Panamá $tagName")
                val releaseNotes = json.optString("body", "Actualización disponible en GitHub.")
                val releasePageUrl = json.optString("html_url", "https://github.com/$repoOwner/$repoName/releases")
                val publishedAt = json.optString("published_at", "")

                // Extraer la versión real remota (buscando en tag_name, name y body)
                val remoteVersion = extractVersion(tagName, releaseTitle, releaseNotes) ?: currentVersion

                // Buscar el APK dentro de assets
                var apkUrl = "https://github.com/$repoOwner/$repoName/releases/download/latest/SismosPanama.apk"
                var apkSize = 0L

                val assetsArray = json.optJSONArray("assets")
                if (assetsArray != null) {
                    for (i in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url", apkUrl)
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                val hasNewer = isNewerVersion(remoteVersion = remoteVersion, currentVersion = currentVersion)

                Result.success(
                    AppUpdateInfo(
                        isUpdateAvailable = hasNewer,
                        latestVersion = remoteVersion,
                        currentVersion = currentVersion,
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        apkDownloadUrl = apkUrl,
                        releasePageUrl = releasePageUrl,
                        publishedDate = publishedAt,
                        apkSizeBytes = apkSize
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractVersion(tag: String, title: String, body: String): String? {
        val versionRegex = Regex("""[vV]?(\d+\.\d+(?:\.\d+)?)""")

        // 1. Intentar con tag si no es 'latest'
        if (!tag.equals("latest", ignoreCase = true)) {
            val match = versionRegex.find(tag)
            if (match != null) return match.groupValues[1]
        }

        // 2. Intentar buscar en el título del release (ej: "Sismos Panamá v2.5.1")
        val titleMatch = versionRegex.find(title)
        if (titleMatch != null) return titleMatch.groupValues[1]

        // 3. Intentar buscar en el cuerpo del release (ej: "Versión: v2.5.1")
        val bodyMatch = versionRegex.find(body)
        if (bodyMatch != null) return bodyMatch.groupValues[1]

        return null
    }

    private fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        val cleanRemote = remoteVersion.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

        if (cleanRemote.equals("latest", ignoreCase = true) || cleanRemote.isBlank()) {
            return false
        }

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        if (remoteParts.isEmpty() || currentParts.isEmpty()) return false

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
