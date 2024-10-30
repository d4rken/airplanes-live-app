package eu.darken.apl.watchlist.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.darken.apl.common.room.InstantConverter
import eu.darken.apl.watchlist.core.db.history.WatchCheckDao
import eu.darken.apl.watchlist.core.db.history.WatchCheckEntity
import eu.darken.apl.watchlist.core.db.types.AircraftWatchDao
import eu.darken.apl.watchlist.core.db.types.AircraftWatchEntity
import eu.darken.apl.watchlist.core.db.types.FlightWatchDao
import eu.darken.apl.watchlist.core.db.types.FlightWatchEntity
import eu.darken.apl.watchlist.core.db.types.SquawkWatchDao
import eu.darken.apl.watchlist.core.db.types.SquawkWatchEntity

@Database(
    entities = [
        FlightWatchEntity::class,
        SquawkWatchEntity::class,
        AircraftWatchEntity::class,
        WatchCheckEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class)
abstract class WatchlistRoomDb : RoomDatabase() {
    abstract fun flightWatch(): FlightWatchDao
    abstract fun squawkWatch(): SquawkWatchDao
    abstract fun aircraftWatch(): AircraftWatchDao
    abstract fun checks(): WatchCheckDao
}