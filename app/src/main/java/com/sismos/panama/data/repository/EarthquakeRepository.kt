package com.sismos.panama.data.repository

import com.sismos.panama.data.api.RetrofitClient
import com.sismos.panama.data.api.SeismicApiService
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.data.models.PanamaFaultsCatalog
import com.sismos.panama.data.models.PanamaRegionsCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EarthquakeRepository(
    private val apiService: SeismicApiService = RetrofitClient.apiService
) {

    private data class PanamaCity(val name: String, val lat: Double, val lon: Double)

    private val panamaReferenceCities = listOf(
        PanamaCity("Ciudad de Panamá", 8.9824, -79.5199),
        PanamaCity("David, Chiriquí", 8.4274, -82.4309),
        PanamaCity("Puerto Armuelles, Chiriquí", 8.2825, -82.8631),
        PanamaCity("Santiago, Veraguas", 8.1042, -80.9717),
        PanamaCity("Chitré, Herrera", 7.9614, -80.4289),
        PanamaCity("Las Tablas, Los Santos", 7.7667, -80.2833),
        PanamaCity("Tonosí, Los Santos", 7.4042, -80.4458),
        PanamaCity("Colón", 9.3598, -79.9015),
        PanamaCity("Penonomé, Coclé", 8.5197, -80.3556),
        PanamaCity("Changuinola, Bocas del Toro", 9.4300, -82.5200),
        PanamaCity("La Palma, Darién", 8.4072, -78.1417)
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchPanamaEarthquakes(minMagnitude: Double? = null): Result<List<Earthquake>> =
        withContext(Dispatchers.IO) {
            // Intentar primero desde la fuente oficial del Instituto de Geociencias (IGC-UP)
            val igcResult = fetchFromIgcUp(minMagnitude)
            if (igcResult.isSuccess && !igcResult.getOrNull().isNullOrEmpty()) {
                return@withContext igcResult
            }

            // Fallback automático al feed satelital USGS filtrado para Panamá
            fetchFromUsgs(minMagnitude)
        }

    /**
     * Parsea directamente el portal oficial del Instituto de Geociencias (sismosgeociencias.up.ac.pa)
     */
    private fun fetchFromIgcUp(minMagnitude: Double?): Result<List<Earthquake>> {
        return try {
            val request = Request.Builder()
                .url("https://sismosgeociencias.up.ac.pa/")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error ${response.code}"))
            }

            val bodyString = response.body?.string() ?: return Result.failure(Exception("Empty body"))
            if (bodyString.contains("Estamos en mantenimiento", ignoreCase = true)) {
                return Result.failure(Exception("IGC-UP en mantenimiento"))
            }

            // Tomar los primeros 45,000 caracteres para procesar los eventos más recientes
            val slice = if (bodyString.length > 45000) bodyString.substring(0, 45000) else bodyString

            val rowPattern = Pattern.compile(
                "<tr>\\s*<td>([^<]*)</td>\\s*<td[^>]*>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td><a href=\"([^\"]*)\""
            )
            val matcher = rowPattern.matcher(slice)

            val list = mutableListOf<Earthquake>()
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ssa", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/Panama")
            }

            while (matcher.find() && list.size < 40) {
                val estado = matcher.group(1)?.trim() ?: "OFICIAL"
                val fecha = matcher.group(2)?.trim() ?: ""
                val hora = matcher.group(3)?.trim() ?: ""
                val latStr = matcher.group(4)?.trim() ?: ""
                val lonStr = matcher.group(5)?.trim() ?: ""
                val profStr = matcher.group(6)?.trim() ?: ""
                val magStr = matcher.group(7)?.trim() ?: ""
                val regionStr = matcher.group(8)?.trim() ?: "Panamá"
                val link = matcher.group(9)?.trim() ?: ""

                val lat = latStr.toDoubleOrNull() ?: continue
                val lon = lonStr.toDoubleOrNull() ?: continue
                val mag = magStr.toDoubleOrNull() ?: continue
                val depth = profStr.replace("km", "").replace(" ", "").toDoubleOrNull() ?: 10.0

                if (minMagnitude != null && mag < minMagnitude) continue

                var timeMillis = System.currentTimeMillis()
                try {
                    val cleanHora = hora.replace(" ", "")
                    val dateParsed = sdf.parse("$fecha $cleanHora")
                    if (dateParsed != null) timeMillis = dateParsed.time
                } catch (e: Exception) {
                    // Fallback a hora actual si el formato difiere
                }

                val (nearestCity, distanceKm) = findNearestCity(lat, lon)
                val nearestFault = findNearestFault(lat, lon)
                val panamaRegion = findPanamaRegion(lat, lon)

                val cleanedPlace = regionStr
                    .replace("&aacute;", "á").replace("&eacute;", "é")
                    .replace("&iacute;", "í").replace("&oacute;", "ó")
                    .replace("&uacute;", "ú").replace("&ntilde;", "ñ")
                    .replace("Panamáá", "Panamá").replace("Panamá", "Panamá")
                    .replace("Sonáá", "Soná").replace("Soná", "Soná")
                    .replace("Tonosíí", "Tonosí").replace("Tonosí", "Tonosí")
                    .replace("Panam([^á\\w]|$)", "Panamá$1")
                    .replace("Son([^á\\w]|$)", "Soná$1")
                    .replace("Tonos([^í\\w]|$)", "Tonosí$1")

                val fullUrl = if (link.startsWith("http")) link else "https://sismosgeociencias.up.ac.pa/$link"

                list.add(
                    Earthquake(
                        id = "igc-$fecha-$hora-$lat-$lon",
                        magnitude = mag,
                        place = cleanedPlace,
                        timeMillis = timeMillis,
                        latitude = lat,
                        longitude = lon,
                        depthKm = depth,
                        nearestCity = nearestCity,
                        distanceToNearestCityKm = distanceKm,
                        nearestFault = nearestFault.name,
                        faultMechanism = nearestFault.mechanism,
                        panamaRegion = panamaRegion,
                        isTsunamiWarning = mag >= 6.5 && depth < 30.0,
                        feltReports = 0,
                        officialUrl = fullUrl,
                        sourceName = "Instituto de Geociencias (IGC-UP) • $estado"
                    )
                )
            }

            if (list.isNotEmpty()) Result.success(list) else Result.failure(Exception("Sin datos IGC"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fallback con el feed oficial USGS filtrado por Bounding Box exclusivo de Panamá
     */
    private suspend fun fetchFromUsgs(minMagnitude: Double?): Result<List<Earthquake>> {
        return try {
            val response = apiService.getPanamaEarthquakes(
                minMag = minMagnitude,
                minLat = 6.5,
                maxLat = 10.3,
                minLon = -83.5,
                maxLon = -77.0,
                limit = 50
            )

            val features = response.features ?: emptyList()

            val earthquakes = features.mapNotNull { feat ->
                val coords = feat.geometry.coordinates ?: return@mapNotNull null
                if (coords.size < 2) return@mapNotNull null

                val lon = coords[0]
                val lat = coords[1]
                val depth = if (coords.size >= 3) coords[2] else 10.0
                val mag = feat.properties.mag ?: return@mapNotNull null
                val time = feat.properties.time ?: System.currentTimeMillis()
                val place = feat.properties.place ?: "Panamá"

                if (!isWithinPanamaBounds(lat, lon, place)) return@mapNotNull null

                val (nearestCity, distanceKm) = findNearestCity(lat, lon)
                val nearestFault = findNearestFault(lat, lon)
                val panamaRegion = findPanamaRegion(lat, lon)

                Earthquake(
                    id = feat.id,
                    magnitude = mag,
                    place = cleanPanamaPlace(place, nearestCity, distanceKm),
                    timeMillis = time,
                    latitude = lat,
                    longitude = lon,
                    depthKm = depth,
                    nearestCity = nearestCity,
                    distanceToNearestCityKm = distanceKm,
                    nearestFault = nearestFault.name,
                    faultMechanism = nearestFault.mechanism,
                    panamaRegion = panamaRegion,
                    isTsunamiWarning = (feat.properties.tsunami ?: 0) > 0,
                    feltReports = feat.properties.felt ?: 0,
                    officialUrl = feat.properties.url ?: "https://earthquake.usgs.gov/earthquakes/eventpage/${feat.id}",
                    sourceName = "Red Sísmica Internacional (USGS)"
                )
            }

            Result.success(earthquakes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isWithinPanamaBounds(lat: Double, lon: Double, place: String): Boolean {
        if (lat !in 6.5..10.3) return false
        if (lon !in -83.5..-77.0) return false

        val lowerPlace = place.lowercase()
        if (lowerPlace.contains("costa rica") && lon < -83.1 && lat > 8.5) return false
        if (lowerPlace.contains("colombia") && lon > -77.1 && lat < 7.8) return false
        return true
    }

    private fun findNearestCity(lat: Double, lon: Double): Pair<String, Int> {
        var closestCity = panamaReferenceCities.first().name
        var minDistance = Double.MAX_VALUE

        for (city in panamaReferenceCities) {
            val dist = haversineDistanceKm(lat, lon, city.lat, city.lon)
            if (dist < minDistance) {
                minDistance = dist
                closestCity = city.name
            }
        }
        return Pair(closestCity, minDistance.toInt())
    }

    private fun findNearestFault(lat: Double, lon: Double) =
        PanamaFaultsCatalog.faults.minByOrNull { fault ->
            haversineDistanceKm(lat, lon, fault.representativeLat, fault.representativeLon)
        } ?: PanamaFaultsCatalog.faults.first()

    private fun findPanamaRegion(lat: Double, lon: Double): String {
        val region = PanamaRegionsCatalog.regions.minByOrNull { reg ->
            haversineDistanceKm(lat, lon, reg.centroidLat, reg.centroidLon)
        }
        return region?.name ?: "Golfo de Chiriquí / Burica"
    }

    private fun cleanPanamaPlace(originalPlace: String, nearestCity: String, distanceKm: Int): String {
        return if (originalPlace.contains("Panama", ignoreCase = true)) {
            originalPlace
                .replace("Panama", "Panamá")
                .replace("km of", "km de")
                .replace("km N of", "km al N de")
                .replace("km S of", "km al S de")
                .replace("km E of", "km al E de")
                .replace("km W of", "km al O de")
                .replace("km NE of", "km al NE de")
                .replace("km NW of", "km al NO de")
                .replace("km SE of", "km al SE de")
                .replace("km SW of", "km al SO de")
                .replace("km SSW of", "km al SSO de")
                .replace("km WNW of", "km al ONO de")
                .replace("km ESE of", "km al ESE de")
        } else {
            "A $distanceKm km de $nearestCity"
        }
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
