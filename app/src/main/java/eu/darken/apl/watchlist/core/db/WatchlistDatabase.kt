package eu.darken.apl.watchlist.core.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.watchlist.core.db.history.WatchCheckDao
import eu.darken.apl.watchlist.core.db.types.AircraftWatchDao
import eu.darken.apl.watchlist.core.db.types.FlightWatchDao
import eu.darken.apl.watchlist.core.db.types.SquawkWatchDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val database by lazy {
        Room.databaseBuilder(
            context,
            WatchlistRoomDb::class.java, "watchlist"
        ).build()
    }

    val aircraftWatch: AircraftWatchDao
        get() = database.aircraftWatch()

    val flightWatch: FlightWatchDao
        get() = database.flightWatch()

    val squawkWatch: SquawkWatchDao
        get() = database.squawkWatch()

    val checks: WatchCheckDao
        get() = database.checks()

    companion object {
        internal val TAG = logTag("Watchlist", "Database")
    }
}