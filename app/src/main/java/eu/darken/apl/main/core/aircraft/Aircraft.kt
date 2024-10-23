package eu.darken.apl.main.core.aircraft

import android.location.Location
import java.time.Instant

interface Aircraft {
    val hex: AircraftHex
    val airframe: Airframe?
    val registration: Registration?
    val callsign: Callsign?
    val squawk: SquawkCode?
    val description: String?
    val altitude: String?
    val operator: String?

    val id: String
        get() = registration ?: hex

    val label: String
        get() = registration ?: hex

    val location: Location?

    val seenAt: Instant
}
