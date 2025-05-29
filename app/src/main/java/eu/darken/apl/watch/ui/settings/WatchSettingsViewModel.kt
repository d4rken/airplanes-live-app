package eu.darken.apl.watch.ui.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.watch.core.WatchSettings
import eu.darken.apl.watch.core.alerts.WatchWorkerHelper
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class WatchSettingsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val settings: WatchSettings,
    private val watchWorkerHelper: WatchWorkerHelper,
) : ViewModel3(
    dispatcherProvider,
    tag = logTag("Settings", "Watch", "VM"),
) {
    fun updateWatchInterval(interval: Duration) = launch {
        log(tag) { "updateWatchInterval($interval)" }
        settings.watchMonitorInterval.value(interval)
        watchWorkerHelper.updateWorker()
    }

    fun resetWatchInterval() = launch {
        log(tag) { "resetWatchInterval()" }
        settings.watchMonitorInterval.value(WatchSettings.DEFAULT_CHECK_INTERVAL)
        watchWorkerHelper.updateWorker()
    }
}