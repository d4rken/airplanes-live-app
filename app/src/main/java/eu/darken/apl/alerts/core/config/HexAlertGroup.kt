package eu.darken.apl.alerts.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HexAlertGroup(
    @Json(name = "configs") val configs: Set<HexAlertConfig> = emptySet(),
)