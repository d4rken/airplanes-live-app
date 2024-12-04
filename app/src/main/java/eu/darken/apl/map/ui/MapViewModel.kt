package eu.darken.apl.map.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.permissions.Permission
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.findByHex
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.map.core.MapSettings
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.watch.core.WatchRepo
import eu.darken.apl.watch.core.types.AircraftWatch
import eu.darken.apl.watch.core.types.Watch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    @ApplicationContext private val context: Context,
    private val mapSettings: MapSettings,
    private val webpageTool: WebpageTool,
    private val searchRepo: SearchRepo,
    private val watchRepo: WatchRepo,
    private val aircraftRepo: AircraftRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args = MapFragmentArgs.fromSavedStateHandle(handle)
    private val currentOptions = MutableStateFlow(args.mapOptions ?: MapOptions())

    val events = SingleLiveEvent<MapEvents>()
    private val refreshTrigger = MutableStateFlow(UUID.randomUUID())

    val state = combine(
        refreshTrigger,
        watchRepo.watches,
        currentOptions.onEach { log(TAG, INFO) { "New MapOptions: $it" } },
    ) { _, alerts, options ->

        State(
            options = options,
            alerts = alerts,
        )
    }.asLiveData2()

    fun checkLocationPermission() {
        if (Permission.ACCESS_COARSE_LOCATION.isGranted(context)) {
            log(TAG) { "checkLocationPermission(): Already granted" }
        } else {
            log(TAG, INFO) { "checkLocationPermission(): Requesting location permission" }
            events.postValue(MapEvents.RequestLocationPermission)
        }
    }

    fun homeMap() {
        log(TAG) { "homeMap()" }
        events.postValue(MapEvents.HomeMap)
    }

    fun onOpenUrl(url: String) {
        log(TAG) { "onOpenUrl($url)" }
        webpageTool.open(url)
    }

    fun onOptionsUpdated(options: MapOptions) {
        log(TAG) { "onUrlUpdated($options)" }
        currentOptions.value = options
    }

    fun showInSearch(hex: AircraftHex) {
        log(TAG) { "showInSearch($hex)" }
        MapFragmentDirections.actionMapToSearch(
            targetHexes = arrayOf(hex)
        ).navigate()
    }

    fun addWatch(hex: AircraftHex) = launch {
        log(TAG) { "addWatch($hex)" }
        aircraftRepo.findByHex(hex) ?: searchRepo.search(SearchQuery.Hex(hex)).aircraft.single()
        MapFragmentDirections.actionMapToCreateAircraftWatchFragment(
            hex = hex,
        ).navigate()
        launch {
            val added = withTimeoutOrNull(20 * 1000) {
                watchRepo.status
                    .mapNotNull { watches ->
                        watches
                            .filterIsInstance<AircraftWatch.Status>()
                            .filter { it.hex == hex }
                            .filter { it.tracked.isNotEmpty() }
                            .firstOrNull()
                    }
                    .firstOrNull()
            }
            log(TAG) { "addWatch(...): $added" }
            if (added != null) events.postValue(MapEvents.WatchAdded(added))
        }
    }

    fun reset() = launch {
        log(TAG) { "reset()" }
        currentOptions.value = MapOptions()
    }

    data class State(
        val options: MapOptions,
        val alerts: Collection<Watch>,
    )
}