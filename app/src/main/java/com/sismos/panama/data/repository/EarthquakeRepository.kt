package com.sismos.panama.data.repository

import com.sismos.panama.data.api.RetrofitClient
import com.sismos.panama.data.api.SeismicApiService
import com.sismos.panama.data.models.Earthquake
import com.sismos.panama.data.models.PanamaFaultsCatalog
import com.sismos.panama.data.models.PanamaRegionsCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun fetchPanamaEarthquakes(minMagnitude: Double? = null): Result<List<Earthquake>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPanamaEarthquakes(
                    minMag = minMagnitude,
                    minLat = 6.5,
                    maxLat = 10.3,
                    minLon = -83.5,
                    maxLon = -77.0,
                    limit = 60
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

                    // Filtro estricto: comprobar que esté dentro de la zona de Panamá
                    if (!isWithinPanamaBounds(lat, lon, place)) {
                        return@mapNotNull null
                    }

                    // Determinar ciudad panameña más cercana
                    val (nearestCity, distanceKm) = findNearestCity(lat, lon)

                    // Determinar falla tectónica y región de Panamá más cercana
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
                        officialUrl = feat.properties.url ?: "https://earthquake.usgs.gov/earthquakes/eventpage/${feat.id}"
                    )
                }

                Result.success(earthquakes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Valida que el epicentro corresponda geográficamente a la República de Panamá
     * o a su zona limítrofe marina inmediata (ZFP, Burica, CDSP, CDNP)
     */
    private fun isWithinPanamaBounds(lat: Double, lon: Double, place: String): Boolean {
        // Coordenadas extremas de Panamá continental y marítimo
        if (lat !in 6.5..10.3) return false
        if (lon !in -83.5..-77.0) return false

        val lowerPlace = place.lowercase()
        // Si el reporte explícitamente sitúa el evento tierra adentro de otro país, se filtra
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
        val r = 6371.0 // Radio terrestre en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
