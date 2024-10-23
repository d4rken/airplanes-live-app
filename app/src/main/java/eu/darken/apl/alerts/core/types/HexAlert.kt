package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.db.types.HexAlertEntity
import eu.darken.apl.alerts.core.history.AlertCheck
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex


data class HexAlert(
    private val entity: HexAlertEntity,
) : AircraftAlert {
    override val id: AlertId
        get() = entity.id

    override val note: String
        get() = entity.userNote

    val hex: AircraftHex
        get() = entity.hexCode

    data class Status(
        override val alert: HexAlert,
        override val lastCheck: AlertCheck?,
        override val lastHit: AlertCheck?,
        override val tracked: Set<Aircraft> = emptySet(),
    ) : AircraftAlert.Status {

        val hex: AircraftHex
            get() = alert.hex
    }
}