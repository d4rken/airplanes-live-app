package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.SquawkCode
import eu.darken.apl.alerts.core.api.AlertsApi
import eu.darken.apl.alerts.core.config.SquawkAlertConfig

data class SquawkAlert(
    val config: SquawkAlertConfig,
    val infos: Set<AlertsApi.Alerts.Squawk>,
) : AircraftAlert {
    override val id: String
        get() = squawk

    val squawk: SquawkCode
        get() = config.code
}
