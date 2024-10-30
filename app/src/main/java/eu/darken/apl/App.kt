package eu.darken.apl

import android.app.Application
import android.util.Log.VERBOSE
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import eu.darken.apl.common.BuildConfigWrap
import eu.darken.apl.common.debug.autoreport.AutoReporting
import eu.darken.apl.common.debug.logging.LogCatLogger
import eu.darken.apl.common.debug.logging.Logging
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.theming.Theming
import eu.darken.apl.feeder.core.monitor.FeederMonitorService
import eu.darken.apl.watchlist.core.alerts.WatchlistMonitorManager
import javax.inject.Inject

@HiltAndroidApp
open class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var bugReporter: AutoReporting
    @Inject lateinit var theming: Theming
    @Inject lateinit var feederMonitorService: FeederMonitorService
    @Inject lateinit var watchlistMonitorManager: WatchlistMonitorManager
    @Inject lateinit var imageLoaderFactory: ImageLoaderFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfigWrap.DEBUG) {
            Logging.install(LogCatLogger())
            log(TAG) { "BuildConfig.DEBUG=true" }
        }

        bugReporter.setup()

        theming.setup()

        Coil.setImageLoader(imageLoaderFactory)

        feederMonitorService.setup()
        watchlistMonitorManager.setup()

        log(TAG) { "onCreate() done! ${Exception().asLog()}" }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(VERBOSE)
        .setWorkerFactory(workerFactory)
        .build()

    companion object {
        internal val TAG = logTag("App")
    }
}
