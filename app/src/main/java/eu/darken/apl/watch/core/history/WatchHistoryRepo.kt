package eu.darken.apl.watch.core.history

import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.watch.core.WatchId
import eu.darken.apl.watch.core.WatchSettings
import eu.darken.apl.watch.core.db.WatchDatabase
import eu.darken.apl.watch.core.db.history.WatchCheckDao
import eu.darken.apl.watch.core.db.history.WatchCheckEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepo @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val settings: WatchSettings,
    private val database: WatchDatabase,
) {

    private val watchCheckDao: WatchCheckDao
        get() = database.checks

    val firehose: Flow<WatchCheck?> = watchCheckDao.firehose().map {
        if (it == null) return@map null
        it.toAlertCheck()
    }

    suspend fun getLastCheck(watchId: WatchId): WatchCheck? {
        return watchCheckDao.getLastCheck(watchId)?.toAlertCheck()
    }

    suspend fun getLastHit(watchId: WatchId): WatchCheck? {
        return watchCheckDao.getLastHit(watchId)?.toAlertCheck()
    }

    suspend fun addCheck(watchId: WatchId, aircraftCount: Int) {
        log(TAG) { "addCheck($watchId, $aircraftCount)" }
        val entity = WatchCheckEntity(
            watchId = watchId,
            aircraftcount = aircraftCount
        )
        watchCheckDao.insert(entity)
    }

    private fun WatchCheckEntity.toAlertCheck() = WatchCheck(
        watchId = this.watchId,
        checkAt = this.checkedAt,
        aircraftCount = this.aircraftcount,
    )

    companion object {
        internal val TAG = logTag("Watch", "History", "Repo")
    }
}