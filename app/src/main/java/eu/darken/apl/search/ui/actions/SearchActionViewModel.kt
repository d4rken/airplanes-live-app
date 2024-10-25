package eu.darken.apl.search.ui.actions

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.flow.combine
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.ui.actions.FeederActionEvents
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.getByHex
import eu.darken.apl.map.core.MapOptions
import getBasicAlertNote
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject


@HiltViewModel
class SearchActionViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    @ApplicationContext private val context: Context,
    private val aircraftRepo: AircraftRepo,
    private val alertRepo: AlertsRepo,
) : ViewModel3(dispatcherProvider) {

    private val navArgs by handle.navArgs<SearchActionDialogArgs>()
    private val aircraftHex: AircraftHex
        get() = navArgs.hex

    private val aircraft = aircraftRepo.getByHex(aircraftHex)
        .filterNotNull()
        .replayingShare(viewModelScope)

    val events = SingleLiveEvent<FeederActionEvents>()

    init {
        log(TAG) { "Loading for $aircraftHex" }
    }

    val state = combine(
        alertRepo.hexAlerts,
        aircraft,
    ) { hexAlerts, ac ->
            State(
                aircraft = ac,
                hasAlert = hexAlerts.any { it.matches(ac) }
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

    fun addAlert() = launch {
        log(TAG) { "addAlert()" }
        val ac = aircraft.first()
        SearchActionDialogDirections.actionSearchActionToCreateHexAlertFragment(
            hex = aircraftHex.uppercase(),
            note = ac.getBasicAlertNote(context)
        ).navigate()
    }

    data class State(
        val aircraft: Aircraft,
        val hasAlert: Boolean,
    )
}