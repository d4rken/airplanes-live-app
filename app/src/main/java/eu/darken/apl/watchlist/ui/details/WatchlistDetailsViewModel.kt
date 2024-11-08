package eu.darken.apl.watchlist.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.findByCallsign
import eu.darken.apl.main.core.findByHex
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.WatchlistRepo
import eu.darken.apl.watchlist.core.types.AircraftWatch
import eu.darken.apl.watchlist.core.types.FlightWatch
import eu.darken.apl.watchlist.core.types.SquawkWatch
import eu.darken.apl.watchlist.core.types.Watch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class WatchlistDetailsViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val watchlistRepo: WatchlistRepo,
    private val searchRepo: SearchRepo,
    private val aircraftRepo: AircraftRepo,
    private val locationManager2: LocationManager2,
) : ViewModel3(dispatcherProvider) {

    private val navArgs by handle.navArgs<WatchlistDetailsFragmentArgs>()
    private val watchId: WatchId = navArgs.watchId

    val events = SingleLiveEvent<WatchlistDetailsEvents>()

    private val trigger = MutableStateFlow(UUID.randomUUID())

    private val status = watchlistRepo.status
        .mapNotNull { data -> data.singleOrNull { it.id == watchId } }
        .replayingShare(viewModelScope)

    init {
        watchlistRepo.status
            .map { alerts -> alerts.singleOrNull { it.id == watchId } }
            .filter { it == null }
            .take(1)
            .onEach {
                log(TAG) { "Alert data for $watchId is no longer available" }
                popNavStack()
            }
            .launchInViewModel()
    }

    val state = combine(
        trigger,
        locationManager2.state,
        status,
    ) { _, locationState, alert ->

        val aircraft = when (alert) {
            is AircraftWatch.Status -> alert.tracked.firstOrNull() ?: aircraftRepo.findByHex(alert.hex)
            is FlightWatch.Status -> alert.tracked.firstOrNull() ?: aircraftRepo.findByCallsign(alert.callsign)
            is SquawkWatch.Status -> null
        }

        State(
            status = alert,
            aircraft = aircraft,
            distanceInMeter = run {
                if (locationState !is LocationManager2.State.Available) return@run null
                val location = aircraft?.location ?: return@run null
                locationState.location.distanceTo(location)
            }
        )
    }
        .asLiveData2()

    fun removeAlert(confirmed: Boolean = false) = launch {
        log(TAG) { "removeAlert()" }
        if (!confirmed) {
            events.postValue(WatchlistDetailsEvents.RemovalConfirmation(watchId))
            return@launch
        }

        watchlistRepo.delete(state.value!!.status.id)
    }

    fun showOnMap() = launch {
        log(TAG) { "showOnMap()" }
        WatchlistDetailsFragmentDirections.actionWatchlistDetailsFragmentToMap(
            mapOptions = when (val watchStatus = status.first()) {
                is AircraftWatch.Status -> {
                    MapOptions.focus(watchStatus.hex)
                }

                is SquawkWatch.Status -> {
                    val hexes = searchRepo.search(SearchQuery.Squawk(watchStatus.squawk))
                    MapOptions.focusAircraft(hexes.aircraft)
                }

                is FlightWatch.Status -> {
                    val hexes = searchRepo.search(SearchQuery.Callsign(watchStatus.callsign))
                    MapOptions.focusAircraft(hexes.aircraft)
                }
            }
        ).navigate()
    }

    fun updateNote(note: String) = launch {
        log(TAG) { "updateNote($note)" }
        watchlistRepo.updateNote(watchId, note.trim())
    }

    data class State(
        val status: Watch.Status,
        val aircraft: Aircraft?,
        val distanceInMeter: Float?,
    )

    companion object {
        private val TAG = logTag("Watchlist", "Action", "Dialog", "ViewModel")
    }

}