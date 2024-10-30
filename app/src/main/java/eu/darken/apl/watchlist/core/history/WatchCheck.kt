package eu.darken.apl.watchlist.core.history

import eu.darken.apl.watchlist.core.WatchId
import java.time.Instant

data class WatchCheck(
    val watchId: WatchId,
    val checkAt: Instant,
    val aircraftCount: Int,
)