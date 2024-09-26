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
import eu.darken.apl.map.core.MapSettings
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val mapSettings: MapSettings,
    private val webpageTool: WebpageTool,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<MapEvents>()

    val state = mapSettings.globeUrl.flow.map {
        State(
            url = it,

            )
    }.asLiveData2()

    fun checkLocationPermission() {
        if (Permission.ACCESS_FINE_LOCATION.isGranted(context)) {
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

    data class State(
        val url: String,
    )
}