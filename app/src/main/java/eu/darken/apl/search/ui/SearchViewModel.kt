package eu.darken.apl.search.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.BuildConfigWrap
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.GeneralSettings
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.search.ui.items.AircraftResultVH
import eu.darken.apl.search.ui.items.LocationPromptVH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val searchRepo: SearchRepo,
    private val webpageTool: WebpageTool,
    private val locationManager2: LocationManager2,
    private val generalSettings: GeneralSettings,
) : ViewModel3(dispatcherProvider) {

    private val currentQuery = MutableStateFlow(
        if (BuildConfigWrap.DEBUG) SearchRepo.Query("C17") else null
    )
    private val currentSearch: Flow<SearchRepo.Result?> = currentQuery
        .map { query -> query?.let { searchRepo.search(it) } }
        .flatMapLatest { it ?: flowOf(null) }

    val events = SingleLiveEvent<SearchEvents>()

    val state = combine(
        currentQuery,
        currentSearch,
        generalSettings.searchLocationDismissed.flow,
        locationManager2.state,
    ) { query, result, locationDismissed, locationState ->
        val items = mutableListOf<SearchAdapter.Item>()

        if (!locationDismissed && (locationState as? LocationManager2.State.Unavailable)?.isPermissionIssue == true) {
            LocationPromptVH.Item(
                locationState,
                onGrant = {
                    events.postValue(SearchEvents.RequestLocationPermission)
                },
                onDismiss = {
                    generalSettings.searchLocationDismissed.valueBlocking = true
                }
            ).run { items.add(this) }
        }

        result?.aircraft
            ?.map { ac ->
                AircraftResultVH.Item(
                    aircraft = ac,
                    distanceInMeter = if (locationState is LocationManager2.State.Available && ac.location != null) {
                        locationState.location.distanceTo(ac.location!!)
                    } else {
                        null
                    },
                    onTap = {

                    },
                    onLongPress = {

                    },
                    onThumbnail = {
                        launch { webpageTool.open(it.link) }
                    }
                )
            }
            ?.sortedBy { it.distanceInMeter ?: Float.MAX_VALUE }
            ?.run { items.addAll(this) }

        State(
            query = query,
            isSearching = result?.searching ?: false,
            items = items,
        )
    }.asLiveData2()

    fun search(term: String?) {
        log(TAG) { "search($term)" }
        currentQuery.value = term
            ?.takeIf { it.isNotBlank() }
            ?.let { SearchRepo.Query(it) }
    }

    data class State(
        val items: List<SearchAdapter.Item>,
        val query: SearchRepo.Query? = null,
        val isSearching: Boolean = false,
    )

    companion object {
        private val TAG = logTag("Search", "Fragment", "ViewModel")
    }
}