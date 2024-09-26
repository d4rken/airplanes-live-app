package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.config.SquawkAlertConfig
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.main.core.api.AplApi

data class SquawkAlert(
    val config: SquawkAlertConfig,
    val infos: Set<AplApi.Aircraft>,
) : AircraftAlert {
    override val id: String
        get() = squawk

    val squawk: SquawkCode
        get() = config.code
}
