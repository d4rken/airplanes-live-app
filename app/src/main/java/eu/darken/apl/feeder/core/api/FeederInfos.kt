package eu.darken.apl.feeder.core.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.feeder.core.ReceiverId
import eu.darken.apl.feeder.core.config.FeederPosition

@JsonClass(generateAdapter = true)
data class FeederInfos(
    @Json(name = "beast") val beastInfos: List<Beast>,
    @Json(name = "mlat") val mlatInfos: List<Mlat>,
    @Json(name = "anywhere") val anywhereLink: String?,
) {

    @JsonClass(generateAdapter = true)
    data class Beast(
        @Json(name = "receiverId") val receiverId: ReceiverId,
        @Json(name = "bandwidth") val bandwidth: Double,
        @Json(name = "connTime") val connTime: Long,
        @Json(name = "messageRate") val messageRate: Double,
        @Json(name = "positionRate") val positionRate: Double,
//        @Json(name = "recentRtt") val recentRtt: Long,
        @Json(name = "positions") val positions: Int,

        )

    @JsonClass(generateAdapter = true)
    data class Mlat(
        @Json(name = "user") val user: String,
        @Json(name = "uuid") val uuid: ReceiverId,
        @Json(name = "lat") val lat: Double?,
        @Json(name = "lon") val lon: Double?,
        @Json(name = "privacy") val privacy: Boolean,
        @Json(name = "messageRate") val messageRate: Double,
        @Json(name = "peerCount") val peerCount: Int,
        @Json(name = "badSyncTimeout") val badSyncTimeout: Long,
        @Json(name = "outlierPercent") val outlierPercent: Float,
    ) {
        val position: FeederPosition?
            get() = if (lat != null && lon != null) FeederPosition(lat, lon) else null
    }
}
