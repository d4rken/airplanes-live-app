package eu.darken.apl.feeder.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeederGroup(
    @Json(name = "configs") val configs: Set<FeederConfig> = emptySet(),
)
