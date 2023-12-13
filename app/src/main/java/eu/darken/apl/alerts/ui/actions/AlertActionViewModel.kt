package eu.darken.apl.alerts.ui.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
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
    private val webpageTool: WebpageTool,
) : ViewModel3(dispatcherProvider) {

    private val navArgs by handle.navArgs<AlertActionDialogArgs>()
    private val alertId: AlertId = navArgs.alertId
    private val trigger = MutableStateFlow(UUID.randomUUID())
    val events = SingleLiveEvent<AlertActionEvents>()

    init {
        alertsRepo.alerts
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
        alertsRepo.alerts.mapNotNull { data -> data.singleOrNull { it.id == alertId } },
    ) { _, alert ->


        State(
            alert = alert,
        )
    }
        .asLiveData2()

    fun removeAlert(confirmed: Boolean = false) = launch {
        log(TAG) { "removeFeeder()" }
        if (!confirmed) {
            events.postValue(AlertActionEvents.RemovalConfirmation(alertId))
            return@launch
        }

        alertsRepo.removeAlert(state.value!!.alert)
    }

    fun showOnMap() = launch {
        log(TAG) { "showOnMap()" }

        when (val alert = state.value!!.alert) {
            is HexAlert -> {
                webpageTool.open("https://globe.airplanes.live/?icao=${alert.hex}")
            }

            is SquawkAlert -> {
                // TODO better URL
                webpageTool.open("https://globe.airplanes.live/")
            }
        }
    }

    data class State(
        val alert: AircraftAlert,
    )

    companion object {
        private val TAG = logTag("Alerts", "Action", "Dialog", "ViewModel")
    }

}