package eu.darken.apl.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.debug.logging.Logging.Priority.ERROR
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.debug.recorder.core.RecorderModule
import eu.darken.apl.common.navigation.findNavController
import eu.darken.apl.common.theming.Theming
import eu.darken.apl.common.uix.Activity2
import eu.darken.apl.databinding.MainActivityBinding
import eu.darken.apl.feeder.core.monitor.FeederMonitorNotifications
import eu.darken.apl.feeder.ui.add.NewFeederQR
import eu.darken.apl.main.ui.main.MainFragmentDirections
import eu.darken.apl.watch.core.alerts.WatchAlertNotifications
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : Activity2() {

    private val vm: MainActivityVM by viewModels()
    private lateinit var ui: MainActivityBinding
    val navController by lazy { supportFragmentManager.findNavController(R.id.nav_host) }
    @Inject lateinit var theming: Theming

    var showSplashScreen = true

    @Inject lateinit var recorderModule: RecorderModule
    @Inject lateinit var feederMonitorNotifications: FeederMonitorNotifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        theming.notifySplashScreenDone(this)
        splashScreen.setKeepOnScreenCondition { showSplashScreen && savedInstanceState == null }

        ui = MainActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        vm.readyState.observe { showSplashScreen = false }
        feederMonitorNotifications.clearOfflineNotifications()

        handleIntent(intent)

        vm.onGo()

        vm.events.observe { events ->
            when (events) {
                is MainActivityEvents.BottomNavigation -> {
                    bottomNavController?.navigate(events.directions)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(B_KEY_SPLASH, showSplashScreen)
        super.onSaveInstanceState(outState)
    }

    private val bottomNavController: NavController?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host) // Activity -> NavHostFragment
            ?.childFragmentManager?.fragments?.firstOrNull() // NavHostFragment1 -> MainFragment
            ?.childFragmentManager?.fragments?.firstOrNull() // MainFragment -> NavHostFragment2
            ?.findNavController()

    fun goToSettings() {
        navController.navigate(MainFragmentDirections.actionMainFragmentToSettingsContainerFragment())
    }

    fun goSponsor() {
        vm.goSponsor()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        log(TAG) { "Handling intent $intent" }
        when (intent.action) {
            Intent.ACTION_MAIN -> {
                // NOOP
            }

            Intent.ACTION_VIEW -> {
                // Handle feeder QR codes
                val data = intent.data
                if (data != null && NewFeederQR.isValid(data.toString())) {
                    log(TAG) { "Received feeder QR code: $data" }
                    val directions = MainFragmentDirections.actionMainFragmentToAddFeederFragment(data.toString())
                    navController.navigate(directions)
                } else {
                    log(TAG, WARN) { "Invalid or unsupported VIEW intent data: $data" }
                }
            }

            WatchAlertNotifications.ALERT_SHOW_ACTION -> {
                val watchId = intent.getStringExtra(WatchAlertNotifications.ARG_WATCHID)
                if (watchId == null) {
                    log(TAG, ERROR) { "watchId was null" }
                } else {
                    vm.showWatchAlert(watchId)
                }
            }

            else -> log(TAG, WARN) { "Unknown intent type: ${intent.action}" }
        }
    }

    companion object {
        private const val B_KEY_SPLASH = "showSplashScreen"
        private val TAG = logTag("Main", "Activity")
    }
}
