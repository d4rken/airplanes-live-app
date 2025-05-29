package eu.darken.apl.main.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.BottomNavigationDirections
import eu.darken.apl.common.SponsorHelper
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.SingleEventFlow
import eu.darken.apl.common.uix.ViewModel2
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.watch.core.WatchId
import eu.darken.apl.watch.core.WatchRepo
import eu.darken.apl.watch.core.getStatus
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val sponsorHelper: SponsorHelper,
    private val watchRepo: WatchRepo,
) : ViewModel2(
    dispatcherProvider = dispatcherProvider,
    tag = logTag("Main", "ViewModel")
) {

    override var launchErrorHandler: CoroutineExceptionHandler? = null

    private val stateFlow = MutableStateFlow(State())
    val state = stateFlow
        .onEach { log(VERBOSE) { "New state: $it" } }
        .asStateFlow()

    private val readyStateInternal = MutableStateFlow(true)
    val readyState = readyStateInternal.asStateFlow()

    val events = SingleEventFlow<MainActivityEvents>()

    fun onGo() {
        stateFlow.value = stateFlow.value.copy(ready = true)
    }

    fun goSponsor() = launch {
        sponsorHelper.openSponsorPage()
    }

    fun showWatchAlert(watchId: WatchId) = launch {
        val status = watchRepo.getStatus(watchId)
        if (status == null) {
            log(TAG, WARN) { "Watch with id $watchId no longer exists" }
            return@launch
        }
        if (status.tracked.isEmpty()) {
            log(TAG) { "No aircraft: $status" }
        } else {
            val directions = BottomNavigationDirections.goToMap(
                mapOptions = MapOptions.focus(status.tracked.map { it.hex })
            )
            events.emit(MainActivityEvents.BottomNavigation(directions))
        }
    }

    data class State(
        val ready: Boolean = false
    )

    companion object {
        private val TAG = logTag("Feeder", "Action", "Dialog", "ViewModel")
    }
}