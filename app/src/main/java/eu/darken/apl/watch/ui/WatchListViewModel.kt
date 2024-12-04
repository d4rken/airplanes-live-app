package eu.darken.apl.watch.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
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
import eu.darken.apl.watch.core.WatchRepo
import eu.darken.apl.watch.core.alerts.WatchMonitor
import eu.darken.apl.watch.core.types.AircraftWatch
import eu.darken.apl.watch.core.types.FlightWatch
import eu.darken.apl.watch.core.types.SquawkWatch
import eu.darken.apl.watch.ui.types.MultiAircraftWatchVH
import eu.darken.apl.watch.ui.types.SingleAircraftWatchVH
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val watchRepo: WatchRepo,
    private val watchMonitor: WatchMonitor,
    private val webpageTool: WebpageTool,
    private val locationManager2: LocationManager2,
    private val aircraftRepo: AircraftRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args by handle.navArgs<WatchListFragmentArgs>()
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
        watchRepo.status,
        locationManager2.state,
        watchRepo.isRefreshing
    ) { _, alerts, locationState, isRefreshing ->
        val items = alerts
            .sortedByDescending { it.watch.addedAt }
            .sortedBy { alert ->
                alert.note.takeIf { it.isNotBlank() } ?: "ZZZZ"
            }
            .map { alert ->
                when (alert) {
                    is AircraftWatch.Status -> SingleAircraftWatchVH.Item(
                        status = alert,
                        aircraft = aircraftRepo.findByHex(alert.hex),
                        ourLocation = (locationState as? LocationManager2.State.Available)?.location,
                        onTap = {
                            WatchListFragmentDirections.actionWatchlistToWatchlistDetailsFragment(alert.id).navigate()
                        },
                        onThumbnail = { launch { webpageTool.open(it.link) } },
                    )

                    is FlightWatch.Status -> SingleAircraftWatchVH.Item(
                        status = alert,
                        aircraft = aircraftRepo.findByHex(alert.callsign),
                        ourLocation = (locationState as? LocationManager2.State.Available)?.location,
                        onTap = {
                            WatchListFragmentDirections.actionWatchlistToWatchlistDetailsFragment(alert.id).navigate()
                        },
                        onThumbnail = { launch { webpageTool.open(it.link) } },
                    )

                    is SquawkWatch.Status -> MultiAircraftWatchVH.Item(
                        status = alert,
                        ourLocation = (locationState as? LocationManager2.State.Available)?.location,
                        onShowMore = {
                            WatchListFragmentDirections.actionWatchlistToSearch(
                                targetSquawks = arrayOf(alert.squawk)
                            ).navigate()
                        },
                        onTap = {
                            WatchListFragmentDirections.actionWatchlistToWatchlistDetailsFragment(alert.id).navigate()
                        },
                        onAircraftTap = {
                            WatchListFragmentDirections.actionWatchlistToMap(
                                mapOptions = MapOptions.focus(it)
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
        watchMonitor.check()
    }

    data class State(
        val items: List<WatchListAdapter.Item>,
        val isRefreshing: Boolean = false,
    )
}