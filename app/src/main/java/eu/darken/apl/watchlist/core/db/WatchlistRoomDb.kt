package eu.darken.apl.watchlist.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.darken.apl.common.room.InstantConverter
import eu.darken.apl.watchlist.core.db.history.WatchCheckDao
import eu.darken.apl.watchlist.core.db.history.WatchCheckEntity
import eu.darken.apl.watchlist.core.db.types.AircraftWatchEntity
import eu.darken.apl.watchlist.core.db.types.BaseWatchEntity
import eu.darken.apl.watchlist.core.db.types.FlightWatchEntity
import eu.darken.apl.watchlist.core.db.types.SquawkWatchEntity
import eu.darken.apl.watchlist.core.db.types.WatchDao

@Database(
    entities = [
        BaseWatchEntity::class,
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
    abstract fun watches(): WatchDao
    abstract fun checks(): WatchCheckDao
}