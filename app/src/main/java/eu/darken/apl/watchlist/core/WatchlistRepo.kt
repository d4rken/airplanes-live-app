package eu.darken.apl.watchlist.core

import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.combine
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.watchlist.core.db.WatchlistDatabase
import eu.darken.apl.watchlist.core.db.types.AircraftWatchEntity
import eu.darken.apl.watchlist.core.db.types.FlightWatchEntity
import eu.darken.apl.watchlist.core.db.types.SquawkWatchEntity
import eu.darken.apl.watchlist.core.history.WatchHistoryRepo
import eu.darken.apl.watchlist.core.types.AircraftWatch
import eu.darken.apl.watchlist.core.types.FlightWatch
import eu.darken.apl.watchlist.core.types.SquawkWatch
import eu.darken.apl.watchlist.core.types.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepo @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val settings: WatchlistSettings,
    private val watchlistDatabase: WatchlistDatabase,
    private val watchHistory: WatchHistoryRepo,
    private val aircraftRepo: AircraftRepo,
) {

    private val refreshTrigger = MutableStateFlow(UUID.randomUUID())
    val isRefreshing = MutableStateFlow(false)
    private val lock = Mutex()

    val watches: Flow<List<Watch>> = watchlistDatabase.watches.current()
        .map { ws -> ws.map { AircraftWatch(it) } }
        .replayingShare(appScope)

    val status: Flow<Collection<Watch.Status>> = combine(
        refreshTrigger,
        watchHistory.firehose,
        aircraftRepo.aircraft,
        watches
    ) { _, _, aircraft, watches ->
        log(TAG) { "Search cache size ${aircraft.size}" }

        val status = mutableSetOf<Watch.Status>()
        acWatches
            .map { watch ->
                AircraftWatch.Status(
                    watch = watch,
                    lastCheck = watchHistory.getLastCheck(watch.id),
                    lastHit = watchHistory.getLastHit(watch.id),
                    tracked = aircraft.values
                        .filter { it.hex == watch.hex }
                        .toSet()
                        .also { if (it.isNotEmpty()) log(TAG) { "Matched $watch to $it" } }
                )
            }
            .run {
                log(TAG) { "Got ${this.size} hex alerts" }
                status.addAll(this)
            }
        flightWatches
            .map { watch ->
                FlightWatch.Status(
                    watch = watch,
                    lastCheck = watchHistory.getLastCheck(watch.id),
                    lastHit = watchHistory.getLastHit(watch.id),
                    tracked = aircraft.values
                        .filter { it.callsign == watch.callsign }
                        .toSet()
                        .also { if (it.isNotEmpty()) log(TAG) { "Matched $watch to $it" } }
                )
            }
            .run {
                log(TAG) { "Got ${this.size} callsign watches" }
                status.addAll(this)
            }
        squawkWatches
            .map { watch ->
                SquawkWatch.Status(
                    watch = watch,
                    lastCheck = watchHistory.getLastCheck(watch.id),
                    lastHit = watchHistory.getLastHit(watch.id),
                    tracked = aircraft.values
                        .filter { it.squawk == watch.code }
                        .toSet()
                        .also { if (it.isNotEmpty()) log(TAG) { "Matched $watch to $it" } }
                )
            }
            .run {
                log(TAG) { "Got ${this.size} squawk watches" }
                status.addAll(this)
            }
        status
    }
        .replayingShare(appScope)

    suspend fun refresh() {
        log(TAG) { "refresh()" } // TODO
        refreshTrigger.value = UUID.randomUUID()
    }

    suspend fun createFlight(callsign: Callsign, note: String = ""): FlightWatch {
        log(TAG) { "createFlight($callsign, $note)" }
        val entity = FlightWatchEntity(
            callsign = callsign,
            userNote = note,
        )
        watchlistDatabase.flightWatch.insert(entity)
        return FlightWatch(
            specific = entity,
        ).also {
            log(TAG, INFO) { "createFlight(...): Created $it" }
        }
    }

    suspend fun createAircraft(hex: AircraftHex, note: String = ""): AircraftWatch {
        log(TAG) { "createAircraft($hex, $note)" }
        val entity = AircraftWatchEntity(
            hexCode = hex,
            userNote = note,
        )
        watchlistDatabase.aircraftWatch.insert(entity)
        return AircraftWatch(
            specific = entity,
        ).also {
            log(TAG, INFO) { "createAircraft(...): Created $it" }
        }
    }

    suspend fun createSquawk(code: SquawkCode, note: String = "") {
        log(TAG) { "createSquawk($code, $note)" }

        lock.withLock {
            withContext(NonCancellable) {
                val entity = SquawkWatchEntity(
                    code = code,
                    userNote = note,
                )
                watchlistDatabase.squawkWatch.insert(entity)
            }
        }

        refresh()
    }

    suspend fun delete(id: WatchId) = lock.withLock {
        log(TAG) { "delete($id)" }

        withContext(NonCancellable) {
            when {
                id.isHex() -> {
                    watchlistDatabase.aircraftWatch.delete(id)
                    log(TAG) { "delete(...): Deleted aircraft $id" }
                }

                id.isCallsign() -> {
                    watchlistDatabase.flightWatch.delete(id)
                    log(TAG) { "delete(...): Deleted flight $id" }
                }

                id.isSquawk() -> {
                    watchlistDatabase.squawkWatch.delete(id)
                    log(TAG) { "delete(...): Deleted squawk $id" }
                }

                else -> throw IllegalArgumentException("Invalid watchId: $id")
            }
        }

        refresh()
    }

    suspend fun updateNote(id: WatchId, note: String) = lock.withLock {
        log(TAG) { "updateNote($id,$note)" }

        withContext(NonCancellable) {
            watchlistDatabase.run {
                when {
                    id.isHex() -> aircraftWatch.updateNoteIfDifferent(id, note)

                    id.isCallsign() -> flightWatch.updateNoteIfDifferent(id, note)

                    id.isSquawk() -> squawkWatch.updateNoteIfDifferent(id, note)

                    else -> throw IllegalArgumentException("Invalid watchId: $id")
                }
            }
        }
    }

    companion object {
        private val TAG = logTag("Watchlist", "Repo")
    }
}