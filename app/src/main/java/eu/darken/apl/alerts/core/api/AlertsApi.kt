package eu.darken.apl.alerts.core.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface AlertsApi {

    @JsonClass(generateAdapter = true)
    data class Alerts(
        @Json(name = "hexes") val hexes: List<Hex> = emptyList(),
        @Json(name = "squawk") val squawks: List<Squawk> = emptyList(),
    ) {

        @JsonClass(generateAdapter = true)
        data class Hex(
            @Json(name = "hex") val hex: String, // "461f31",
            @Json(name = "flight") val flight: String?, // "FIN6BC",
            @Json(name = "r") val registration: String?, // "OH-LVD",
            @Json(name = "t") val aircraftType: String?, // "A319",
            @Json(name = "desc") val description: String?, // "AIRBUS A-319",
            @Json(name = "squawk") val squawk: String?, // "1000",
            @Json(name = "alt_baro") val altitude: String?, // 17975
        )

        @JsonClass(generateAdapter = true)
        data class Squawk(
            @Json(name = "squawk") val squawk: String, // "7700"
        )
    }

    @GET("companion/alerts/")
    suspend fun getAlerts(
        @Query("hex", encoded = true) hexes: String,
        @Query("squawk", encoded = true) squawks: String,
    ): Alerts

    @GET("companion/alerts/")
    suspend fun getHexAlerts(@Query("hex", encoded = true) hexes: String): Alerts

    @GET("companion/alerts/")
    suspend fun getSquawkAlerts(@Query("squawk", encoded = true) squawks: String): Alerts

}