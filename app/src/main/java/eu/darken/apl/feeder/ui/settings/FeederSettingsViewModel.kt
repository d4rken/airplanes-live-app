package eu.darken.apl.feeder.ui.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.config.FeederSettings
import eu.darken.apl.feeder.core.monitor.FeederWorkerHelper
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class FeederSettingsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val settings: FeederSettings,
    private val feederWorkerHelper: FeederWorkerHelper,
) : ViewModel3(
    dispatcherProvider,
    tag = logTag("Settings", "Feeder", "VM"),
) {

    fun updateFeederInterval(interval: Duration) = launch {
        log(tag) { "updateFeederInterval($interval)" }
        settings.feederMonitorInterval.value(interval)
        feederWorkerHelper.updateWorker()
    }

    fun resetFeederInterval() = launch {
        log(tag) { "resetFeederInterval()" }
        settings.feederMonitorInterval.value(FeederSettings.DEFAULT_CHECK_INTERVAL)
        feederWorkerHelper.updateWorker()
    }
}