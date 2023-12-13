package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AircraftHex
import eu.darken.apl.alerts.core.api.AlertsApi
import eu.darken.apl.alerts.core.config.HexAlertConfig

data class HexAlert(
    val config: HexAlertConfig,
    val infos: Set<AlertsApi.Alerts.Hex>,
) : AircraftAlert {
    val hex: AircraftHex
        get() = config.hexCode

    override val id: String
        get() = hex

    val label: String
        get() = config.label
}
