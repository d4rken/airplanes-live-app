package eu.darken.apl.alerts.core.history

import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.AlertSettings
import eu.darken.apl.alerts.core.db.AlertsDatabase
import eu.darken.apl.alerts.core.db.history.AlertCheckDao
import eu.darken.apl.alerts.core.db.history.AlertCheckEntity
import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertHistoryRepo @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val settings: AlertSettings,
    private val database: AlertsDatabase,
) {

    private val alertsDao: AlertCheckDao
        get() = database.checks

    val firehose: Flow<AlertCheck?> = alertsDao.firehose().map {
        if (it == null) return@map null
        it.toAlertCheck()
    }

    suspend fun getLastCheck(alertId: AlertId): AlertCheck? {
        log(TAG, VERBOSE) { "getLastCheck($alertId)" }
        return alertsDao.getLastCheck(alertId)?.toAlertCheck()
    }

    suspend fun getLastHit(alertId: AlertId): AlertCheck? {
        log(TAG, VERBOSE) { "getLastHit($alertId)" }
        return alertsDao.getLastHit(alertId)?.toAlertCheck()
    }

    suspend fun addCheck(alertId: AlertId, aircraftCount: Int) {
        log(TAG) { "addCheck($alertId, $aircraftCount)" }
        val entity = AlertCheckEntity(
            alertId = alertId,
            aircraftcount = aircraftCount
        )
        alertsDao.insert(entity)
    }

    private fun AlertCheckEntity.toAlertCheck() = AlertCheck(
        alertId = this.alertId,
        checkAt = this.checkedAt,
        aircraftCount = this.aircraftcount,
    )

    companion object {
        internal val TAG = logTag("Alerts", "History", "Repo")
    }
}