package eu.darken.apl.alerts.core.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.alerts.core.db.history.AlertCheckDao
import eu.darken.apl.alerts.core.db.types.CallsignAlertsDao
import eu.darken.apl.alerts.core.db.types.HexAlertsDao
import eu.darken.apl.alerts.core.db.types.SquawkAlertsDao
import eu.darken.apl.common.debug.logging.logTag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val database by lazy {
        Room.databaseBuilder(
            context,
            AlertsRoomDb::class.java, "alerts"
        ).build()
    }

    val callsignAlerts: CallsignAlertsDao
        get() = database.callsignAlerts()

    val squawkAlerts: SquawkAlertsDao
        get() = database.squawkAlerts()

    val hexAlerts: HexAlertsDao
        get() = database.hexAlerts()

    val checks: AlertCheckDao
        get() = database.checks()

    companion object {
        internal val TAG = logTag("Alerts", "Database")
    }
}