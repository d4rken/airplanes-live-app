package eu.darken.apl.watchlist.core.alerts

import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.watchlist.core.WatchlistRepo
import eu.darken.apl.watchlist.core.WatchlistSettings
import eu.darken.apl.watchlist.core.history.WatchHistoryRepo
import eu.darken.apl.watchlist.core.types.AircraftWatch
import eu.darken.apl.watchlist.core.types.FlightWatch
import eu.darken.apl.watchlist.core.types.SquawkWatch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistMonitor @Inject constructor(
    private val settings: WatchlistSettings,
    private val watchlistRepo: WatchlistRepo,
    private val historyRepo: WatchHistoryRepo,
    private val searchRepo: SearchRepo,
) {
    suspend fun check() {
        log(TAG) { "check()" }
        val currentWatches = watchlistRepo.watches.first()

        currentWatches.filterIsInstance<AircraftWatch>().let { ws ->
            val results = searchRepo.search(SearchQuery.Hex(ws.map { it.hex }.toSet()))
            // TODO filter for position
            ws.forEach { alert ->
                val result = results.aircraft.firstOrNull { alert.matches(it) }
                historyRepo.addCheck(alert.id, if (result != null) 1 else 0)
            }
        }

        currentWatches.filterIsInstance<FlightWatch>().let { ws ->
            val results = searchRepo.search(SearchQuery.Callsign(ws.map { it.callsign }.toSet()))
            // TODO filter for position
            ws.forEach { alert ->
                val result = results.aircraft.firstOrNull { alert.matches(it) }
                historyRepo.addCheck(alert.id, if (result != null) 1 else 0)
            }
        }

        currentWatches.filterIsInstance<SquawkWatch>().let { ws ->
            val results = searchRepo.search(SearchQuery.Squawk(ws.map { it.code }.toSet()))
            // TODO filter for position
            ws.forEach { alert ->
                val result = results.aircraft.firstOrNull { alert.matches(it) }
                historyRepo.addCheck(alert.id, if (result != null) 1 else 0)
            }
        }

    }

    companion object {
        private val TAG = logTag("Watchlist", "Monitor")
    }
}