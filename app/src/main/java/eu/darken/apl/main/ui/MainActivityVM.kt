package eu.darken.apl.main.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.BottomNavigationDirections
import eu.darken.apl.common.SponsorHelper
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.uix.ViewModel2
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.watch.core.WatchId
import eu.darken.apl.watch.core.WatchlistRepo
import eu.darken.apl.watch.core.getStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val sponsorHelper: SponsorHelper,
    private val watchlistRepo: WatchlistRepo,
) : ViewModel2(dispatcherProvider = dispatcherProvider) {

    private val stateFlow = MutableStateFlow(State())
    val state = stateFlow
        .onEach { log(VERBOSE) { "New state: $it" } }
        .asLiveData2()

    private val readyStateInternal = MutableStateFlow(true)
    val readyState = readyStateInternal.asLiveData2()

    val events = SingleLiveEvent<MainActivityEvents>()

    fun onGo() {
        stateFlow.value = stateFlow.value.copy(ready = true)
    }

    fun goSponsor() = launch {
        sponsorHelper.openSponsorPage()
    }

    fun showWatchAlert(watchId: WatchId) = launch {
        val status = watchlistRepo.getStatus(watchId)
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
            events.postValue(MainActivityEvents.BottomNavigation(directions))
        }
    }

    data class State(
        val ready: Boolean = false
    )

}