package eu.darken.apl.search.ui.actions

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.flow.combine
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.ui.actions.FeederActionEvents
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.getByHex
import eu.darken.apl.map.core.MapOptions
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject


@HiltViewModel
class SearchActionViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    @ApplicationContext private val context: Context,
    private val aircraftRepo: AircraftRepo,
    private val alertRepo: AlertsRepo,
    private val locationManager2: LocationManager2,
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
        locationManager2.state,
    ) { hexAlerts, ac, locationState ->
        State(
            aircraft = ac,
            distanceInMeter = run {
                if (locationState !is LocationManager2.State.Available) return@run null
                val location = ac.location ?: return@run null
                locationState.location.distanceTo(location)
            },
            alert = hexAlerts.firstOrNull { it.matches(ac) }
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

    fun showAlert() = launch {
        log(TAG) { "showAlert()" }
        val alert = state.value?.alert
        if (alert != null) {
            SearchActionDialogDirections.actionSearchActionToAlertActionDialog(alert.id)
        } else {
            SearchActionDialogDirections.actionSearchActionToCreateHexAlertFragment(aircraftHex)
        }.navigate()
    }

    data class State(
        val aircraft: Aircraft,
        val distanceInMeter: Float?,
        val alert: AircraftAlert?,
    )
}