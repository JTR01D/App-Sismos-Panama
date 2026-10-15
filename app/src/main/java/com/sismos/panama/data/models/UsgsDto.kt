package com.sismos.panama.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelos DTO para deserializar el feed oficial GeoJSON del USGS Earthquake Hazards API
 */
data class UsgsFeatureCollection(
    @SerializedName("type") val type: String?,
    @SerializedName("metadata") val metadata: UsgsMetadata?,
    @SerializedName("features") val features: List<UsgsFeature>?
)

data class UsgsMetadata(
    @SerializedName("generated") val generated: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("count") val count: Int?
)

data class UsgsFeature(
    @SerializedName("id") val id: String,
    @SerializedName("properties") val properties: UsgsProperties,
    @SerializedName("geometry") val geometry: UsgsGeometry
)

data class UsgsProperties(
    @SerializedName("mag") val mag: Double?,
    @SerializedName("place") val place: String?,
    @SerializedName("time") val time: Long?,
    @SerializedName("updated") val updated: Long?,
    @SerializedName("tz") val tz: Int?,
    @SerializedName("url") val url: String?,
    @SerializedName("detail") val detail: String?,
    @SerializedName("felt") val felt: Int?,
    @SerializedName("cdi") val cdi: Double?,
    @SerializedName("mmi") val mmi: Double?,
    @SerializedName("alert") val alert: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("tsunami") val tsunami: Int?,
    @SerializedName("sig") val sig: Int?,
    @SerializedName("net") val net: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("ids") val ids: String?,
    @SerializedName("sources") val sources: String?,
    @SerializedName("types") val types: String?,
    @SerializedName("nst") val nst: Int?,
    @SerializedName("dmin") val dmin: Double?,
    @SerializedName("rms") val rms: Double?,
    @SerializedName("gap") val gap: Double?,
    @SerializedName("magType") val magType: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String?
)

data class UsgsGeometry(
    @SerializedName("type") val type: String?,
    // [longitud, latitud, profundidad_en_km]
    @SerializedName("coordinates") val coordinates: List<Double>?
)
