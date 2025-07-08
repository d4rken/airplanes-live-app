package eu.darken.apl.feeder.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeederGroup(
    @SerialName("configs") val configs: Set<FeederConfig> = emptySet(),
)
