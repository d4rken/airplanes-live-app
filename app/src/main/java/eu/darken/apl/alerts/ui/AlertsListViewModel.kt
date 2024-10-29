package eu.darken.apl.alerts.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.monitor.AlertMonitor
import eu.darken.apl.alerts.core.types.CallsignAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.alerts.ui.types.MultiAircraftAlertVH
import eu.darken.apl.alerts.ui.types.SingleAircraftAlertVH
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.findByHex
import eu.darken.apl.map.core.MapOptions
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
    private val alertMonitor: AlertMonitor,
    private val webpageTool: WebpageTool,
    private val locationManager2: LocationManager2,
    private val aircraftRepo: AircraftRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args by handle.navArgs<AlertsListFragmentArgs>()
    private val targetAircraft: Set<AircraftHex>?
        get() = args.targetAircraft?.toSet()

    init {
        log(TAG, INFO) { "targetAircraft=$targetAircraft" }
    }

    private val refreshTimer = callbackFlow {
        while (isActive) {
            refresh()
            send(Unit)
            delay(60 * 1000)
        }
        awaitClose()
    }

    val state = combine(
        refreshTimer,
        alertsRepo.status,
        locationManager2.state,
        alertsRepo.isRefreshing
    ) { _, alerts, locationState, isRefreshing ->
        val items = alerts
            .sortedBy { alert -> alert.note.takeIf { it.isNotBlank() } ?: "ZZZZ" }
            .map { alert ->
                when (alert) {
                    is HexAlert.Status -> SingleAircraftAlertVH.Item(
                        status = alert,
                        aircraft = aircraftRepo.findByHex(alert.hex),
                        distanceInMeter = run {
                            if (locationState !is LocationManager2.State.Available) return@run null
                            val location = alert.tracked.firstOrNull()?.location ?: return@run null
                            locationState.location.distanceTo(location)
                        },
                        onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                        onThumbnail = { launch { webpageTool.open(it.link) } },
                    )

                    is CallsignAlert.Status -> SingleAircraftAlertVH.Item(
                        status = alert,
                        aircraft = aircraftRepo.findByHex(alert.callsign),
                        distanceInMeter = run {
                            if (locationState !is LocationManager2.State.Available) return@run null
                            val location = alert.tracked.firstOrNull()?.location ?: return@run null
                            locationState.location.distanceTo(location)
                        },
                        onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                        onThumbnail = { launch { webpageTool.open(it.link) } },
                    )

                    is SquawkAlert.Status -> MultiAircraftAlertVH.Item(
                        status = alert,
                        onTap = { AlertsListFragmentDirections.actionAlertsToAlertActionDialog(alert.id).navigate() },
                        onAircraftTap = {
                            AlertsListFragmentDirections.actionAlertsToMap(
                                mapOptions = MapOptions(
                                    filter = MapOptions.Filter(icaos = setOf(it.hex))
                                )
                            ).navigate()
                        },
                        onThumbnail = { launch { webpageTool.open(it.link) } },
                    )
                }
            }
        State(
            items = items,
            isRefreshing = isRefreshing,
        )
    }.asLiveData2()

    fun refresh() = launch {
        log(TAG) { "refresh()" }
        alertMonitor.checkAlerts()
    }

    data class State(
        val items: List<AlertsListAdapter.Item>,
        val isRefreshing: Boolean = false,
    )
}