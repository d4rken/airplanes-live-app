package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.db.types.SquawkAlertEntity
import eu.darken.apl.alerts.core.history.AlertCheck
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.SquawkCode

data class SquawkAlert(
    private val entity: SquawkAlertEntity,
) : AircraftAlert {
    override val id: AlertId
        get() = entity.id
    override val note: String
        get() = entity.userNote

    val code: SquawkCode
        get() = entity.code

    override fun matches(ac: Aircraft): Boolean {
        return ac.squawk?.uppercase() == code.uppercase()
    }

    data class Status(
        override val alert: SquawkAlert,
        override val lastCheck: AlertCheck?,
        override val lastHit: AlertCheck?,
        override val tracked: Set<Aircraft> = emptySet(),
    ) : AircraftAlert.Status {
        val squawk: SquawkCode
            get() = alert.code
    }
}