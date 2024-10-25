package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.db.types.CallsignAlertEntity
import eu.darken.apl.alerts.core.history.AlertCheck
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.Callsign


data class CallsignAlert(
    private val entity: CallsignAlertEntity,
) : AircraftAlert {
    override val id: AlertId
        get() = entity.id

    override val note: String
        get() = entity.userNote

    val callsign: Callsign
        get() = entity.callsign

    override fun matches(ac: Aircraft): Boolean {
        return ac.callsign?.lowercase() == callsign.lowercase()
    }

    data class Status(
        override val alert: CallsignAlert,
        override val lastCheck: AlertCheck?,
        override val lastHit: AlertCheck?,
        override val tracked: Set<Aircraft> = emptySet(),
    ) : AircraftAlert.Status {

        val callsign: Callsign
            get() = alert.callsign
    }
}