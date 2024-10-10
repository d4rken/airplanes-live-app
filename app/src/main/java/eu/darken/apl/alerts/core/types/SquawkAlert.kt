package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.config.db.SquawkAlertEntity
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

    data class Status(
        override val alert: SquawkAlert,
        override val tracked: Set<AircraftAlert.Status.Tracked> = emptySet(),
    ) : AircraftAlert.Status {
        val squawk: SquawkCode
            get() = alert.code
    }
}