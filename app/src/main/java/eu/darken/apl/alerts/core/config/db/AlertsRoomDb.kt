package eu.darken.apl.alerts.core.config.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.darken.apl.common.room.InstantConverter

@Database(
    entities = [
        SquawkAlertEntity::class,
        HexAlertEntity::class,
    ],
    version = 1,
    autoMigrations = [
//        AutoMigration(1, 2)
    ],
    exportSchema = true,
)
@TypeConverters(InstantConverter::class)
abstract class AlertsRoomDb : RoomDatabase() {
    abstract fun squawkAlerts(): SquawkAlertsDao
    abstract fun hexAlerts(): HexAlertsDao
}