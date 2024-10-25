package eu.darken.apl.main.core.api

import android.location.Location
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Airframe
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.Registration
import eu.darken.apl.main.core.aircraft.SquawkCode
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.Instant

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
    data class MilitaryResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("mil")
    suspend fun getMilitary(): MilitaryResponse

    @JsonClass(generateAdapter = true)
    data class LADDResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("ladd")
    suspend fun getLADD(): LADDResponse

    @JsonClass(generateAdapter = true)
    data class PIAResponse(
        @Json(name = "ac") val ac: List<Aircraft>,
        @Json(name = "total") val total: Int,
        @Json(name = "msg") override val message: String,
        @Json(name = "now") override val now: Long,
        @Json(name = "ctime") override val ctime: Long,
        @Json(name = "ptime") override val ptime: Int
    ) : BaseResponse

    @GET("pia")
    suspend fun getPIA(): PIAResponse

    @GET("point/{lat}/{lon}/{radius}")
    suspend fun getAircraftsByLocation(
        @Path("lat") latitude: Double,
        @Path("lon") longitude: Double,
        @Path("radius") radius: Int
    ): AirframesResponse

    @JsonClass(generateAdapter = true)
    data class Aircraft(
        @Json(name = "hex") override val hex: AircraftHex,
        @Json(name = "r") override val registration: Registration?,
        @Json(name = "flight") override val callsign: Callsign?,

        @Json(name = "ownOp") override val operator: String?,
        @Json(name = "t") override val airframe: Airframe?,
        @Json(name = "desc") override val description: String?,

        @Json(name = "squawk") override val squawk: SquawkCode?,
        @Json(name = "emergency") override val emergency: String?,

        @Json(name = "oat") val oat: Int?, // outer/static air temperature (C)
        @Json(name = "tat") val tat: Int?, // total air temperature (C)
        @Json(name = "alt_baro") override val altitude: String?, // Altitude in feet or "ground"
        @Json(name = "baro_rate") val rateBaro: Int?,
        @Json(name = "geom_rate") val rateGeometric: Int?,
        @Json(name = "gs") override val groundSpeed: Float?, // ground speed in knots
        @Json(name = "ias") override val indicatedAirSpeed: Int?, // indicated air speed in knots
        @Json(name = "mag_heading") val headingMagnetic: Double?, // Heading, degrees clockwise from magnetic north
        @Json(name = "true_heading") val headingTrue: Double?, // Heading, degrees clockwise from true north
        @Json(name = "lat") val latitude: String?,
        @Json(name = "lon") val longitude: String?,
        @Json(name = "track") val track: Float?, // true track over ground in degrees (0-359)
        @Json(name = "rr_lat") val roughLat: Double?, // If no ADS-B or MLAT position available, a rough estimated position for the aircraft based on the receiver’s estimated coordinates.
        @Json(name = "rr_lon") val roughLon: Double?, // If no ADS-B or MLAT position available, a rough estimated position for the aircraft based on the receiver’s estimated coordinates.
        @Json(name = "lastPosition") val lastPosition: LastPosition?,

        @Json(name = "version") val version: Int?,

        @Json(name = "messages") override val messages: Int,
        @Json(name = "rssi") override val rssi: Double,
        @Json(name = "seen") val seenSecondsAgo: Double
    ) : eu.darken.apl.main.core.aircraft.Aircraft {

        override val seenAt: Instant = Instant.now().minusSeconds(seenSecondsAgo.toLong())

        override val outsideTemp: Int?
            get() = oat ?: tat

        override val altitudeRate: Int?
            get() = rateBaro ?: rateGeometric

        override val trackheading: Double?
            get() = headingMagnetic ?: headingTrue

        override val location: Location?
            get() {
                val convLat = this@Aircraft.latitude?.toDouble() ?: return null
                val convLong = this@Aircraft.longitude?.toDouble() ?: return null

                return Location("apl").apply {
                    latitude = convLat
                    longitude = convLong
                }
            }

        @JsonClass(generateAdapter = true)
        data class LastPosition(
            @Json(name = "lat") val lat: Double,
            @Json(name = "lon") val lon: Double,
            @Json(name = "nic") val nic: Int,
            @Json(name = "rc") val rc: Int,
            @Json(name = "seen_pos") val lastSeen: Double
        )

        override fun toString(): String {
            return "Aircraft($hex, $registration, $airframe)"
        }
    }

}