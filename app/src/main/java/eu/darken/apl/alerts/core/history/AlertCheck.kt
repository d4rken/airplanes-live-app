package eu.darken.apl.alerts.core.history

import eu.darken.apl.alerts.core.AlertId
import java.time.Instant

data class AlertCheck(
    val alertId: AlertId,
    val checkAt: Instant,
    val aircraftCount: Int,
)