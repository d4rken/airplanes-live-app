package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.history.AlertCheck
import eu.darken.apl.main.core.aircraft.Aircraft

sealed interface AircraftAlert {
    val id: AlertId
    val note: String

    sealed interface Status {
        val alert: AircraftAlert
        val id: AlertId
            get() = alert.id
        val note: String
            get() = alert.note

        val lastCheck: AlertCheck?
        val lastHit: AlertCheck?

        val tracked: Set<Aircraft>
    }
}