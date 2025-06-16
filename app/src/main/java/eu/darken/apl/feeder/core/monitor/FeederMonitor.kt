package eu.darken.apl.feeder.core.monitor

import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.debug.logging.Logging.Priority.ERROR
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.feeder.core.FeederRepo
import eu.darken.apl.feeder.core.config.FeederSettings
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeederMonitor @Inject constructor(
    private val settings: FeederSettings,
    private val feederRepo: FeederRepo,
    private val notifications: FeederMonitorNotifications,
) {
    suspend fun check() {
        log(TAG) { "check()" }

        try {
            feederRepo.refresh()
        } catch (e: Exception) {
            log(TAG, ERROR) { "Failed to refresh ${e.asLog()}" }
        }

        val offlineDevices = feederRepo.feeders.first()
            .filter { it.config.offlineCheckTimeout != null }
            .filter {
                if (it.lastSeen == null) return@filter false

                val timeSinceUpdate = Duration.between(settings.lastUpdate.value(), Instant.now())
                if (timeSinceUpdate > it.config.offlineCheckTimeout) return@filter false

                Duration.between(it.lastSeen, Instant.now()) > it.config.offlineCheckTimeout
            }
            .filter { it.config.offlineCheckSnoozedAt == null }
            .onEach { log(TAG, INFO) { "Feeder has been offline for a while... $it" } }

        notifications.notifyOfOfflineDevices(offlineDevices)
    }

    companion object {
        private val TAG = logTag("Feeder", "Monitor")
    }
}
