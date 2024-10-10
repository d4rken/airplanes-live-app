package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.config.db.HexAlertEntity
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
        override val tracked: Set<AircraftAlert.Status.Tracked> = emptySet(),
    ) : AircraftAlert.Status {

        val hex: AircraftHex
            get() = alert.hex
    }
}