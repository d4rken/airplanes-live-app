package eu.darken.apl.alerts.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.darken.apl.alerts.core.db.history.AlertCheckDao
import eu.darken.apl.alerts.core.db.history.AlertCheckEntity
import eu.darken.apl.alerts.core.db.types.CallsignAlertEntity
import eu.darken.apl.alerts.core.db.types.CallsignAlertsDao
import eu.darken.apl.alerts.core.db.types.HexAlertEntity
import eu.darken.apl.alerts.core.db.types.HexAlertsDao
import eu.darken.apl.alerts.core.db.types.SquawkAlertEntity
import eu.darken.apl.alerts.core.db.types.SquawkAlertsDao
import eu.darken.apl.common.room.InstantConverter

@Database(
    entities = [
        CallsignAlertEntity::class,
        SquawkAlertEntity::class,
        HexAlertEntity::class,
        AlertCheckEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class)
abstract class AlertsRoomDb : RoomDatabase() {
    abstract fun callsignAlerts(): CallsignAlertsDao
    abstract fun squawkAlerts(): SquawkAlertsDao
    abstract fun hexAlerts(): HexAlertsDao
    abstract fun checks(): AlertCheckDao
}