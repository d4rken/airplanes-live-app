package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AircraftHex

data class NewHexAlert(
    val label: String,
    val hexCode: AircraftHex,
)