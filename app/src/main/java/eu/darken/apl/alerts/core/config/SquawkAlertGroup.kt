package eu.darken.apl.alerts.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SquawkAlertGroup(
    @Json(name = "configs") val configs: Set<SquawkAlertConfig> = emptySet(),
)