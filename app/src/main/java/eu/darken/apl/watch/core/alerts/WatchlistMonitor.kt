package eu.darken.apl.watch.core.alerts

import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.watch.core.WatchlistRepo
import eu.darken.apl.watch.core.WatchlistSettings
import eu.darken.apl.watch.core.history.WatchHistoryRepo
import eu.darken.apl.watch.core.types.AircraftWatch
import eu.darken.apl.watch.core.types.FlightWatch
import eu.darken.apl.watch.core.types.SquawkWatch
import eu.darken.apl.watch.core.types.Watch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistMonitor @Inject constructor(
    private val settings: WatchlistSettings,
    private val watchlistRepo: WatchlistRepo,
    private val historyRepo: WatchHistoryRepo,
    private val searchRepo: SearchRepo,
    private val notifications: WatchAlertNotifications,
) {
    suspend fun check() {
        log(TAG) { "check()" }
        val currentWatches = watchlistRepo.watches.first()
        val alerts = mutableMapOf<Watch, Collection<Aircraft>>()

        suspend fun Watch.process(results: Collection<Aircraft>) {
            log(TAG, VERBOSE) { "Checking $this" }
            // TODO filter for position
            val matches = results.filter { matches(it) }

            if (matches.isNotEmpty()) {
                when {
                    !isNotificationEnabled -> {
                        log(TAG) { "Notifications are disabled for $this" }
                    }

                    historyRepo.getLastCheck(id)?.aircraftCount != 0 -> {
                        log(TAG, VERBOSE) { "Skipping snoozed alert" }
                    }

                    else -> {
                        log(TAG) { "Will notify about $this" }
                        alerts[this] = matches
                    }
                }
            } else {
                log(TAG, VERBOSE) { "No matching aircraft for ${id}" }
            }

            historyRepo.addCheck(id, matches.size)
        }

        currentWatches.filterIsInstance<AircraftWatch>().let { ws ->
            val batchResults = searchRepo.search(SearchQuery.Hex(ws.map { it.hex }.toSet()))
            ws.forEach { it.process(batchResults.aircraft) }
        }

        currentWatches.filterIsInstance<FlightWatch>().let { ws ->
            val batchResults = searchRepo.search(SearchQuery.Callsign(ws.map { it.callsign }.toSet()))
            ws.forEach { it.process(batchResults.aircraft) }
        }
        currentWatches.filterIsInstance<SquawkWatch>().let { ws ->
            val batchResults = searchRepo.search(SearchQuery.Squawk(ws.map { it.code }.toSet()))
            ws.forEach { it.process(batchResults.aircraft) }
        }

        log(TAG) { "Notifying of ${alerts.size} watch matches" }
        alerts.forEach { notifications.alert(it.key, it.value) }
    }

    companion object {
        private val TAG = logTag("Watchlist", "Monitor")
    }
}