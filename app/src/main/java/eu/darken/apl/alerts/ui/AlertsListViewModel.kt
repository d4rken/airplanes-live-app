package eu.darken.apl.alerts.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.alerts.ui.types.HexAlertVH
import eu.darken.apl.alerts.ui.types.SquawkAlertVH
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.SquawkCode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class AlertsListViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val alertsRepo: AlertsRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args by handle.navArgs<AlertsListFragmentArgs>()
    private val targetAircraft: Set<AircraftHex>?
        get() = args.targetAircraft?.toSet()

    init {
        log(TAG, INFO) { "targetAircraft=$targetAircraft" }
    }

    private val refreshTimer = callbackFlow {
        while (isActive) {
            send(Unit)
            delay(1000)
        }
        awaitClose()
    }

    val state = combine(
        refreshTimer,
        alertsRepo.status,
        alertsRepo.isRefreshing
    ) { _, alerts, isRefreshing ->
        val items = alerts.map { alert ->
            when (alert) {
                is HexAlert.Status -> HexAlertVH.Item(
                    status = alert,
                    onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                )

                is SquawkAlert.Status -> SquawkAlertVH.Item(
                    status = alert,
                    onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                )
            }

        }
        State(
            items = items,
            isRefreshing = isRefreshing,
        )
    }.asLiveData2()

    fun addHexAlert(hex: AircraftHex, note: String) = launch {
        log(TAG) { "addHexAlert($hex, $note)" }
        alertsRepo.createHexAlert(hex.uppercase(), note.trim())
    }

    fun addSquawkAlert(squawk: SquawkCode, note: String) = launch {
        log(TAG) { "addSquawkAlert($squawk, $note)" }
        alertsRepo.createSquawkAlert(squawk, note.trim())
    }

    fun refresh() = launch {
        log(TAG) { "refresh()" }
        alertsRepo.refresh()
    }

    data class State(
        val items: List<AlertsListAdapter.Item>,
        val isRefreshing: Boolean = false,
    )
}