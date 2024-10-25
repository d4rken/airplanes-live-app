package eu.darken.apl.common.planespotters.coil

import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Registration

data class AircraftThumbnailQuery(
    val hex: AircraftHex,
    val registration: Registration? = null,
)