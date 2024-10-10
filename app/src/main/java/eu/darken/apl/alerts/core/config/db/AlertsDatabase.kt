package eu.darken.apl.alerts.core.config.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
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

    val squawkAlerts: SquawkAlertsDao
        get() = database.squawkAlerts()

    val hexAlerts: HexAlertsDao
        get() = database.hexAlerts()

    companion object {
        internal val TAG = logTag("Alerts", "Database")
    }
}