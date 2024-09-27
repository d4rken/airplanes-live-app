package eu.darken.apl.main.core.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Airframe
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.Registration
import eu.darken.apl.main.core.aircraft.SquawkCode
import retrofit2.http.GET
import retrofit2.http.Path

interface AirplanesLiveApi {

    interface BaseResponse {
        val message: String
        val now: Long
        val ctime: Long
        val ptime: Int
    }

    @JsonClass(generateAdapter = true)
    data class HexesResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "now") override val now: Long,
        @Json(name = "msg") override val message: String,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("hex/{hexes}")
    suspend fun getAircraftByHex(@Path("hexes", encoded = true) hexes: String): HexesResponse

    @JsonClass(generateAdapter = true)
    data class SquawksResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("squawk/{codes}")
    suspend fun getAircraftBySquawk(@Path("codes", encoded = true) codes: String): SquawksResponse

    @JsonClass(generateAdapter = true)
    data class CallsignsResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("callsign/{signs}")
    suspend fun getAircraftByCallsign(@Path("signs", encoded = true) codes: String): CallsignsResponse

    @JsonClass(generateAdapter = true)
    data class RegistrationsResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("reg/{regs}")
    suspend fun getAircraftByRegistration(@Path("regs", encoded = true) codes: String): RegistrationsResponse

    @JsonClass(generateAdapter = true)
    data class AirframesResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("type/{types}")
    suspend fun getAircraftByAirframe(@Path("types", encoded = true) types: String): AirframesResponse

    @JsonClass(generateAdapter = true)
    data class Aircraft(
        @Json(name = "hex") override val hex: AircraftHex,
        @Json(name = "type") val type: String?,
        @Json(name = "flight") override val callsign: Callsign?,
        @Json(name = "r") override val registration: Registration?,
        @Json(name = "t") override val airframe: Airframe?,
        @Json(name = "desc") override val description: String?,
        @Json(name = "ownOp") override val operator: String?,
        @Json(name = "alt_baro") override val altitude: String?, // Altitude in feet or "ground"
        @Json(name = "baro_rate") val rateBaro: Int?,
        @Json(name = "geom_rate") val rateGeometric: Int?,
        @Json(name = "gs") val groundSpeed: Float?, // ground speed in knots
        @Json(name = "ias") val indicatedAirSpeed: Int?, // indicated air speed in knots
        @Json(name = "tas") val trueAirSpeed: Int?, // true air speed in knots
        @Json(name = "mach") val mach: Double?,
        @Json(name = "oat") val oat: Int?, // outer/static air temperature (C)
        @Json(name = "tat") val tat: Int?, // total air temperature (C)
        @Json(name = "track") val track: Float?, // true track over ground in degrees (0-359)
        @Json(name = "mag_heading") val headingMagnetic: Double?, // Heading, degrees clockwise from magnetic north
        @Json(name = "true_heading") val headingTrue: Double?, // Heading, degrees clockwise from true north
        @Json(name = "squawk") override val squawk: SquawkCode?,
        @Json(name = "emergency") val emergency: String?,
        @Json(name = "category") val category: String?,
        @Json(name = "rr_lat") val roughLat: Double?, // If no ADS-B or MLAT position available, a rough estimated position for the aircraft based on the receiver’s estimated coordinates.
        @Json(name = "rr_lon") val roughLon: Double?, // If no ADS-B or MLAT position available, a rough estimated position for the aircraft based on the receiver’s estimated coordinates.
        @Json(name = "lastPosition") val lastPosition: LastPosition?,
        @Json(name = "version") val version: Int?,
        @Json(name = "messages") val messages: Int,
        @Json(name = "seen") val seen: Double,
        @Json(name = "rssi") val rssi: Double
    ) : eu.darken.apl.main.core.aircraft.Aircraft {

        @JsonClass(generateAdapter = true)
        data class LastPosition(
            @Json(name = "lat") val lat: Double,
            @Json(name = "lon") val lon: Double,
            @Json(name = "nic") val nic: Int,
            @Json(name = "rc") val rc: Int,
            @Json(name = "seen_pos") val lastSeen: Double
        )
    }

}