package eu.darken.apl.main.core.aircraft

import android.location.Location
import java.time.Instant

interface Aircraft {
    val hex: AircraftHex
    val registration: Registration?
    val callsign: Callsign?

    val operator: String?
    val airframe: Airframe?
    val description: String?

    val id: String
        get() = registration ?: hex

    val label: String
        get() = registration ?: hex

    val squawk: SquawkCode?
    val emergency: String?

    val outsideTemp: Int?
    val altitude: String?
    val altitudeRate: Int?
    val groundSpeed: Float?
    val indicatedAirSpeed: Int?
    val trackheading: Double?
    val location: Location?

    val messages: Int
    val seenAt: Instant
    val rssi: Double
}
