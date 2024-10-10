package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.main.core.aircraft.Aircraft
import java.time.Instant

sealed interface AircraftAlert {
    val id: AlertId
    val note: String

    sealed interface Status {
        val alert: AircraftAlert
        val id: AlertId
            get() = alert.id
        val note: String
            get() = alert.note

        val tracked: Set<Tracked>

        data class Tracked(
            val triggeredAt: Instant,
            val updatedAt: Instant,
            val aircraft: Aircraft,
        )
    }
}