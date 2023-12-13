package eu.darken.apl.alerts.core.types

import eu.darken.apl.alerts.core.AlertId

sealed interface AircraftAlert {
    val id: AlertId

    interface Config {
        val id: AlertId
    }
}