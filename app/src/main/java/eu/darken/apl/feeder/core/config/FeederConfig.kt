package eu.darken.apl.feeder.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.feeder.core.ReceiverId
import java.time.Duration
import java.time.Instant

@JsonClass(generateAdapter = true)
data class FeederConfig(
    @Json(name = "receiverId") val receiverId: ReceiverId,
    @Json(name = "addedAt") val addedAt: Instant = Instant.now(),
    @Json(name = "label") val label: String? = null,
    @Json(name = "position") val position: FeederPosition? = null,
    @Json(name = "offlineCheckTimeout") val offlineCheckTimeout: Duration? = null,
    @Json(name = "address") val address: String? = null,
)