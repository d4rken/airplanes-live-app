package eu.darken.apl.alerts.ui.actions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.alerts.core.types.CallsignAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
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
class AlertActionViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val alertsRepo: AlertsRepo,
    private val searchRepo: SearchRepo,
    private val aircraftRepo: AircraftRepo,
    private val locationManager2: LocationManager2,
) : ViewModel3(dispatcherProvider) {

    private val navArgs by handle.navArgs<AlertActionDialogArgs>()
    private val alertId: AlertId = navArgs.alertId

    val events = SingleLiveEvent<AlertActionEvents>()

    private val trigger = MutableStateFlow(UUID.randomUUID())

    private val status = alertsRepo.status
        .mapNotNull { data -> data.singleOrNull { it.id == alertId } }
        .replayingShare(viewModelScope)

    init {
        alertsRepo.status
            .map { alerts -> alerts.singleOrNull { it.id == alertId } }
            .filter { it == null }
            .take(1)
            .onEach {
                log(TAG) { "Alert data for $alertId is no longer available" }
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
            is HexAlert.Status -> alert.tracked.firstOrNull() ?: aircraftRepo.findByHex(alert.hex)
            is CallsignAlert.Status -> alert.tracked.firstOrNull() ?: aircraftRepo.findByCallsign(alert.callsign)
            is SquawkAlert.Status -> null
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
            events.postValue(AlertActionEvents.RemovalConfirmation(alertId))
            return@launch
        }

        alertsRepo.deleteAlert(state.value!!.status.id)
    }

    fun showOnMap() = launch {
        log(TAG) { "showOnMap()" }
        AlertActionDialogDirections.actionAlertActionDialogToMap(
            mapOptions = MapOptions(
                filter = when (val alertStatus = status.first()) {
                    is HexAlert.Status -> {
                        MapOptions.Filter(icaos = setOf(alertStatus.hex))
                    }

                    is SquawkAlert.Status -> {
                        val hexes = searchRepo.search(SearchQuery.Squawk(alertStatus.squawk))
                        MapOptions.Filter(icaos = hexes.aircraft.map { it.hex }.toSet())
                    }

                    is CallsignAlert.Status -> {
                        val hexes = searchRepo.search(SearchQuery.Callsign(alertStatus.callsign))
                        MapOptions.Filter(icaos = hexes.aircraft.map { it.hex }.toSet())
                    }
                }
            )
        ).navigate()
    }

    fun updateNote(note: String) = launch {
        log(TAG) { "updateNote($note)" }
        alertsRepo.updateNote(alertId, note.trim())
    }

    data class State(
        val status: AircraftAlert.Status,
        val aircraft: Aircraft?,
        val distanceInMeter: Float?,
    )

    companion object {
        private val TAG = logTag("Alerts", "Action", "Dialog", "ViewModel")
    }

}