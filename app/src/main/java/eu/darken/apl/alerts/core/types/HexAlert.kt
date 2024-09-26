package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.config.HexAlertConfig
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.api.AplApi

data class HexAlert(
    val config: HexAlertConfig,
    val infos: Set<AplApi.Aircraft>,
) : AircraftAlert {
    val hex: AircraftHex
        get() = config.hexCode

    override val id: String
        get() = hex

    val label: String
        get() = config.label
}
