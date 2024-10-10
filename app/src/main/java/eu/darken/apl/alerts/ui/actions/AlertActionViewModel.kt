package eu.darken.apl.alerts.ui.actions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.replayingShare
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
        status,
    ) { _, alert ->


        State(
            status = alert,
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
        TODO()
    }

    fun showInSearch() = launch {
        log(TAG) { "showInSearch()" }
        TODO()
    }

    fun updateNote(note: String) = launch {
        log(TAG) { "updateNote($note)" }
        alertsRepo.updateNote(alertId, note.trim())
    }

    data class State(
        val status: AircraftAlert.Status,
    )

    companion object {
        private val TAG = logTag("Alerts", "Action", "Dialog", "ViewModel")
    }

}