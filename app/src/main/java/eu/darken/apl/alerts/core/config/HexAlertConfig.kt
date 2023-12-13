package eu.darken.apl.alerts.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.alerts.core.AircraftHex
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.types.AircraftAlert

@JsonClass(generateAdapter = true)
data class HexAlertConfig(
    @Json(name = "label") val label: String,
    @Json(name = "hexCode") val hexCode: AircraftHex,
) : AircraftAlert.Config {

    override val id: AlertId
        get() = hexCode

    fun matches(hex: String): Boolean = hex.lowercase() == hexCode.lowercase()
}