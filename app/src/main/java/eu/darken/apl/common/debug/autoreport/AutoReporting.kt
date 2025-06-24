package eu.darken.apl.common.debug.autoreport

import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.GeneralSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoReporting @Inject constructor(
    private val generalSettings: GeneralSettings,
) {

    fun setup() {
        val isEnabled = generalSettings.isAutoReportingEnabled.flow
        log(TAG) { "setup(): isEnabled=$isEnabled" }
    }

    companion object {
        private val TAG = logTag("Debug", "AutoReporting")
    }
}