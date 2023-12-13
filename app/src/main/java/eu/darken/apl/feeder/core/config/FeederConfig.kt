package eu.darken.apl.feeder.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.feeder.core.ReceiverId
import java.time.Duration

@JsonClass(generateAdapter = true)
data class FeederConfig(
    @Json(name = "receiverId") val receiverId: ReceiverId,
    @Json(name = "user") val user: String? = null,
    @Json(name = "position") val position: FeederPosition? = null,
    @Json(name = "offlineCheckTimeout") val offlineCheckTimeout: Duration? = null,
)