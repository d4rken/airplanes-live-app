package eu.darken.apl.watch.core.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.watch.core.WatchId
import eu.darken.apl.watch.core.db.history.WatchCheckDao
import eu.darken.apl.watch.core.db.types.AircraftWatchEntity
import eu.darken.apl.watch.core.db.types.BaseWatchEntity
import eu.darken.apl.watch.core.db.types.FlightWatchEntity
import eu.darken.apl.watch.core.db.types.SquawkWatchEntity
import eu.darken.apl.watch.core.db.types.WatchDao
import eu.darken.apl.watch.core.types.AircraftWatch
import eu.darken.apl.watch.core.types.FlightWatch
import eu.darken.apl.watch.core.types.SquawkWatch
import eu.darken.apl.watch.core.types.Watch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchDatabase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val database by lazy {
        Room.databaseBuilder(
            context,
            WatchRoomDb::class.java, "watch"
        ).build()
    }

    private val watchDao: WatchDao
        get() = database.watches()

    val watches: Flow<List<Watch>>
        get() = watchDao.current().map { bases ->
            bases.map { base ->
                when (base.watchType) {
                    AircraftWatchEntity.TYPE_KEY -> AircraftWatch(base, watchDao.getAircraft(base.id)!!)

                    FlightWatchEntity.TYPE_KEY -> FlightWatch(base, watchDao.getFlight(base.id)!!)

                    SquawkWatchEntity.TYPE_KEY -> SquawkWatch(base, watchDao.getSquawk(base.id)!!)

                    else -> throw IllegalArgumentException("Unexpected type: ${base.watchType}")
                }
            }
        }

    suspend fun createAircraft(hex: AircraftHex, note: String): AircraftWatch = withContext(NonCancellable) {
        log(TAG) { "createAircraft($hex, $note)" }
        val base = BaseWatchEntity(
            watchType = AircraftWatchEntity.TYPE_KEY,
            userNote = note,
        )
        val specific = AircraftWatchEntity(
            id = base.id,
            hexCode = hex,
        )
        watchDao.insertAircraftWatch(base, specific)
        AircraftWatch(base, specific)
    }

    suspend fun createFlight(callsign: Callsign, note: String): FlightWatch = withContext(NonCancellable) {
        log(TAG) { "createFlight($callsign, $note)" }
        val base = BaseWatchEntity(
            watchType = FlightWatchEntity.TYPE_KEY,
            userNote = note,
        )
        val specific = FlightWatchEntity(
            id = base.id,
            callsign = callsign,
        )
        watchDao.insertFlightWatch(base, specific)
        FlightWatch(base, specific)
    }

    suspend fun createSquawk(code: SquawkCode, note: String): SquawkWatch = withContext(NonCancellable) {
        log(TAG) { "createSquawk($code, $note)" }
        val base = BaseWatchEntity(
            watchType = SquawkWatchEntity.TYPE_KEY,
            userNote = note,
        )
        val specific = SquawkWatchEntity(
            id = base.id,
            code = code,
        )
        watchDao.insertSquawkWatch(base, specific)
        SquawkWatch(base, specific)
    }

    suspend fun deleteWatch(id: WatchId) = withContext(NonCancellable) {
        log(TAG) { "deleteWatch($id)" }
        watchDao.delete(id)
    }

    suspend fun updateNote(id: WatchId, note: String) {
        log(TAG) { "updateNote($id, $note)" }
        watchDao.updateNoteIfDifferent(id, note)
    }

    suspend fun updateNotification(id: WatchId, enabled: Boolean) {
        log(TAG) { "updateNotification($id, $enabled)" }
        watchDao.updateNotification(id, enabled)
    }

    val checks: WatchCheckDao
        get() = database.checks()

    companion object {
        internal val TAG = logTag("Watch", "Database")
    }
}
