package eu.darken.apl.watch.core.alerts

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.debug.logging.Logging.Priority.ERROR
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.watch.core.WatchSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WatchWorkerHelper @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val monitor: WatchMonitor,
    private val watchSettings: WatchSettings,
) {

    private var isInit = false
    fun setup() {
        log(TAG) { "setup()" }
        require(!isInit)
        isInit = true

        appScope.launch { updateWorker() }

        triggerNow()
    }

    fun triggerNow() {
        log(TAG) { "runNow()" }
        appScope.launch {
            try {
                monitor.check()
            } catch (e: Exception) {
                log(TAG, ERROR) { "Failed to refresh: ${e.asLog()}" }
            }
        }
    }

    suspend fun updateWorker() {
        val interval = watchSettings.watchMonitorInterval.value()
        log(TAG) { "updateWorker() to $interval" }

        val workRequest = PeriodicWorkRequestBuilder<WatchWorker>(
            interval,
            Duration.ofMinutes(10)
        ).apply {
            setInputData(Data.Builder().build())
        }.build()

        val operation = workManager.enqueueUniquePeriodicWork(
            "alerts.monitor.worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )

        operation.await()
    }

    companion object {
        val TAG = logTag("Watch", "Worker", "Helper")
    }
}