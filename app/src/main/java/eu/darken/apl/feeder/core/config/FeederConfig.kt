package eu.darken.apl.feeder.core.config

import eu.darken.apl.feeder.core.ReceiverId
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

@Serializable
data class FeederConfig(
    @SerialName("receiverId") val receiverId: ReceiverId,
    @Contextual @SerialName("addedAt") val addedAt: Instant = Instant.now(),
    @SerialName("label") val label: String? = null,
    @SerialName("position") val position: FeederPosition? = null,
    @Contextual @SerialName("offlineCheckTimeout") val offlineCheckTimeout: Duration? = null,
    @Contextual @SerialName("offlineCheckSnoozedAt") val offlineCheckSnoozedAt: Instant? = null,
    @SerialName("address") val address: String? = null,
)
