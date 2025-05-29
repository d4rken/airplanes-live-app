package eu.darken.apl.feeder.core.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class FeedInfos(
    @Json(name = "beast_clients") val beast: List<Beast>,
    @Json(name = "mlat_clients") val mlat: List<Mlat>,
) {

    @JsonClass(generateAdapter = true)
    data class Beast(
        @Json(name = "uuid") val uuid: UUID,
        @Json(name = "avg_kbit_s") val avgKBitsPerSecond: Double,
        @Json(name = "conn_time") val connTime: Long,
        @Json(name = "msgs_s") val messageRate: Double,
        @Json(name = "pos_s") val positionRate: Double,
        @Json(name = "rtt") val recentRtt: Long,
        @Json(name = "pos") val positions: Int,
    )

    @JsonClass(generateAdapter = true)
    data class Mlat(
        @Json(name = "user") val user: String,
        @Json(name = "uuid") val uuid: UUID,
        @Json(name = "message_rate") val messageRate: Double,
        @Json(name = "peer_count") val peerCount: Int,
        @Json(name = "bad_sync_timeout") val badSyncTimeout: Long,
        @Json(name = "outlier_percent") val outlierPercent: Float,
        @Json(name = "bad_peer_list") val badPeerList: String,
        @Json(name = "sync_interest") val syncInterest: List<String>,
        @Json(name = "mlat_interest") val mlatInterest: List<String>,
    )
}
