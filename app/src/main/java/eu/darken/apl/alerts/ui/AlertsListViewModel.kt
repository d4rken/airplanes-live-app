package eu.darken.apl.alerts.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.NewHexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.alerts.core.types.UnsupportedSquawkError
import eu.darken.apl.alerts.ui.types.HexAlertVH
import eu.darken.apl.alerts.ui.types.SquawkAlertVH
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.aircraft.SquawkCode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AlertsListViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val alertsRepo: AlertsRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val refreshTimer = callbackFlow {
        while (isActive) {
            send(Unit)
            delay(1000)
        }
        awaitClose()
    }

    val state = combine(
        refreshTimer,
        alertsRepo.alerts,
        alertsRepo.isRefreshing
    ) { _, alerts, isRefreshing ->
        val items = alerts.map { alert ->
            when (alert) {
                is HexAlert -> HexAlertVH.Item(
                    alert = alert,
                    onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                    onLongPress = {

                    }
                )

                is SquawkAlert -> SquawkAlertVH.Item(
                    alert = alert,
                    onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                    onLongPress = {

                    }
                )
            }

        }
        State(
            items = items,
            isRefreshing = isRefreshing,
        )
    }.asLiveData2()

    fun addHexAlert(newAlert: NewHexAlert) = launch {
        log(TAG) { "addHexAlert($newAlert)" }

        alertsRepo.addHexAlert(newAlert)
    }

    fun addSquawkAlert(squawk: SquawkCode) = launch {
        log(TAG) { "addSquawkAlert($squawk)" }
        try {
            alertsRepo.checkSquawk(squawk)
        } catch (e: HttpException) {
            log(TAG, WARN) { "Squawk check failed: ${e.asLog()}" }
            if (e.code() == 403) {
                throw UnsupportedSquawkError(e)
            }
            throw e
        }

        alertsRepo.addSquawkAlert(squawk)
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