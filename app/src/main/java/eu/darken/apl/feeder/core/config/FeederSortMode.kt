package eu.darken.apl.feeder.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FeederSortMode {
    @SerialName("by_label")
    BY_LABEL,

    @SerialName("by_message_rate")
    BY_MESSAGE_RATE
}