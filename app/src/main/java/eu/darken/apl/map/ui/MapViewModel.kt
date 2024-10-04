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
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.map.core.MapSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val mapSettings: MapSettings,
    private val webpageTool: WebpageTool,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args = MapFragmentArgs.fromSavedStateHandle(handle)
    private val currentOptions = MutableStateFlow(args.mapOptions ?: MapOptions())

    val events = SingleLiveEvent<MapEvents>()
    private val refreshTrigger = MutableStateFlow(UUID.randomUUID())

    val state = combine(
        refreshTrigger,
        currentOptions.onEach { log(TAG, INFO) { "New MapOptions: $it" } },
    ) { _, options ->

        State(
            options = options,
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
            targetAircraft = arrayOf(hex)
        ).navigate()
    }

    fun addAlert(hex: AircraftHex) {
        log(TAG) { "addAlert($hex)" }
        MapFragmentDirections.actionMapToAlerts(
            targetAircraft = arrayOf(hex)
        ).navigate()
    }

    data class State(
        val options: MapOptions,
    )
}