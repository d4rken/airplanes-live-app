package eu.darken.apl.main.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.darken.apl.common.room.InstantConverter
import eu.darken.apl.common.room.LocationConverter

@Database(
    entities = [
        CachedAircraftEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class, LocationConverter::class)
abstract class AircraftRoomDb : RoomDatabase() {
    abstract fun aircraft(): CachedAircraftDao
}