package com.sismos.panama.data.api

import com.sismos.panama.data.models.UsgsFeatureCollection
import retrofit2.http.GET
import retrofit2.http.Query

interface SeismicApiService {

    /**
     * Consulta el catálogo sísmico en tiempo real del USGS filtrado
     * de forma exclusiva por las coordenadas geográficas de la República de Panamá
     */
    @GET("query")
    suspend fun getPanamaEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("minlatitude") minLat: Double = 6.5,
        @Query("maxlatitude") maxLat: Double = 10.5,
        @Query("minlongitude") minLon: Double = -83.5,
        @Query("maxlongitude") maxLon: Double = -77.0,
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 60,
        @Query("minmagnitude") minMag: Double? = null
    ): UsgsFeatureCollection
}
