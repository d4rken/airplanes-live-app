package eu.darken.apl.alerts.core.types

import eu.darken.apl.main.core.aircraft.AircraftHex

data class NewHexAlert(
    val label: String,
    val hexCode: AircraftHex,
)