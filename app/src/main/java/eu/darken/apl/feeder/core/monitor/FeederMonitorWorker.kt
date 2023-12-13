package eu.darken.apl.feeder.core.monitor

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.Bugs
import eu.darken.apl.common.debug.logging.Logging.Priority.ERROR
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.feeder.core.FeederRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Duration
import java.time.Instant


@HiltWorker
class FeederMonitorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dispatcherProvider: DispatcherProvider,
    private val feederRepo: FeederRepo,
    private val monitorNotifications: FeederMonitorNotifications,
) : CoroutineWorker(context, params) {

    private val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var finishedWithError = false

    init {
        log(TAG, VERBOSE) { "init(): workerId=$id" }
    }

    override suspend fun doWork(): Result = try {
        val start = System.currentTimeMillis()
        log(TAG, VERBOSE) { "Executing $inputData now (runAttemptCount=$runAttemptCount)" }

        doDoWork()

        val duration = System.currentTimeMillis() - start

        log(TAG, VERBOSE) { "Execution finished after ${duration}ms, $inputData" }

        Result.success(inputData)
    } catch (e: Exception) {
        if (e !is CancellationException) {
            Bugs.report(e)
            finishedWithError = true
            Result.failure(inputData)
        } else {
            Result.success()
        }
    } finally {
        this.workerScope.cancel("Worker finished (withError?=$finishedWithError).")
    }

    private suspend fun doDoWork() = withContext(dispatcherProvider.IO) {
        try {
            withTimeout(30 * 1000) {
                try {
                    feederRepo.refresh()
                } catch (e: Exception) {
                    log(TAG, ERROR) { "Failed to refresh ${e.asLog()}" }
                }
            }

            val offlineDevices = feederRepo.feeders.first()
                .filter { it.config.offlineCheckTimeout != null }
                .filter { Duration.between(it.lastSeen, Instant.now()) > it.config.offlineCheckTimeout }
                .onEach { log(TAG, INFO) { "Feeder has been offline for a while... $it" } }

            monitorNotifications.notifyOfOfflineDevices(offlineDevices)
        } catch (e: TimeoutCancellationException) {
            log(TAG) { "Worker ran into timeout" }
        }
    }

    companion object {
        val TAG = logTag("Feeder", "Monitor", "Worker")
    }
}
