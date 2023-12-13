package eu.darken.apl.feeder.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeederPosition(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
)
