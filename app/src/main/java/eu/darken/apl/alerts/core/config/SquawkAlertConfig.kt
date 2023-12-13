package eu.darken.apl.alerts.core.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.SquawkCode
import eu.darken.apl.alerts.core.types.AircraftAlert

@JsonClass(generateAdapter = true)
data class SquawkAlertConfig(
    @Json(name = "transponderCode") val code: SquawkCode,
) : AircraftAlert.Config {

    override val id: AlertId
        get() = code
}