package eu.darken.apl.search.ui.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.ui.actions.FeederActionEvents
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.search.core.SearchRepo
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@HiltViewModel
class SearchActionViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val searchRepo: SearchRepo,
) : ViewModel3(dispatcherProvider) {

    private val navArgs by handle.navArgs<SearchActionDialogArgs>()
    private val aircraftHex: AircraftHex
        get() = navArgs.hex

    val events = SingleLiveEvent<FeederActionEvents>()

    init {
        log(TAG) { "Loading for $aircraftHex" }
//        feederRepo.feeders
//            .map { feeders -> feeders.singleOrNull { it.id == feederId } }
//            .filter { it == null }
//            .take(1)
//            .onEach {
//                log(TAG) { "App data for $feederId is no longer available" }
//                popNavStack()
//            }
//            .launchInViewModel()
    }


    val state = searchRepo.getByHex(aircraftHex)
        .filterNotNull()
        .map { ac ->
            State(
                aircraft = ac,
            )
        }
        .asLiveData2()

    fun showMap() {
        log(TAG) { "showMap()" }
        SearchActionDialogDirections.actionSearchActionToMap(
            mapOptions = MapOptions(
                filter = MapOptions.Filter(icaos = setOf(aircraftHex))
            )
        ).navigate()
    }

    data class State(
        val aircraft: Aircraft,
    )
}