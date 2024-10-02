package eu.darken.apl.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.flow.combine
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.common.flow.throttleLatest
import eu.darken.apl.common.livedata.SingleLiveEvent
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.GeneralSettings
import eu.darken.apl.map.core.AirplanesLive
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.search.ui.items.AircraftResultVH
import eu.darken.apl.search.ui.items.LocationPromptVH
import eu.darken.apl.search.ui.items.NoAircraftVH
import eu.darken.apl.search.ui.items.SearchingAircraftVH
import eu.darken.apl.search.ui.items.SummaryVH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val searchRepo: SearchRepo,
    private val webpageTool: WebpageTool,
    private val locationManager2: LocationManager2,
    private val generalSettings: GeneralSettings,
) : ViewModel3(dispatcherProvider) {

    val events = SingleLiveEvent<SearchEvents>()

    private val currentInput = MutableStateFlow(
        Input(
            mode = State.Mode.SQUAWK,
            raw = "7700,7600,7500",
        )
    )

    private val searchTrigger = MutableStateFlow(UUID.randomUUID())
    private val currentSearch: Flow<SearchRepo.Result?> = combine(
        searchTrigger,
        currentInput,
    ) { _, input ->
        val terms = input.raw.split(",").map { it.trim() }.toSet()
        when (input.mode) {
            State.Mode.ALL -> SearchQuery.All(terms)
            State.Mode.HEX -> SearchQuery.Hex(terms)
            State.Mode.CALLSIGN -> SearchQuery.Callsign(terms)
            State.Mode.REGISTRATION -> SearchQuery.Registration(terms)
            State.Mode.SQUAWK -> SearchQuery.Squawk(terms)
            State.Mode.AIRFRAME -> SearchQuery.Airframe(terms)
            State.Mode.INTERESTING -> SearchQuery.Interesting(
                military = terms.contains("military"),
                ladd = terms.contains("ladd"),
                pia = terms.contains("pia"),
            )

            State.Mode.POSITION -> SearchQuery.Position()
        }.also { log(TAG) { "Mapped raw query: '$input' to $it" } }
    }
        .map { searchRepo.search(it) }
        .flatMapLatest { it }
        .replayingShare(viewModelScope)

    init {
        log(TAG) { "init with handle: $handle" }
//        launch {
//            if (currentRawQuery.value != null) return@launch
//            val locationState = locationManager2.state.first()
//            if (locationState is LocationManager2.State.Available) {
//                currentRawQuery.value = SearchQuery.Position(locationState.location)
//            }
//        }
    }

    val state: LiveData<State> = combine(
        currentInput,
        currentSearch.throttleLatest(500),
        generalSettings.searchLocationDismissed.flow,
        locationManager2.state,
    ) { input, result, locationDismissed, locationState ->
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
                        SearchFragmentDirections.actionSearchToMap(
                            mapOptions = MapOptions(
                                filter = MapOptions.Filter(
                                    icaos = setOf(ac.hex),
                                )
                            )
                        ).navigate()
                    },
                    onThumbnail = {
                        launch { webpageTool.open(it.link) }
                    }
                )
            }
            ?.sortedBy { it.distanceInMeter ?: Float.MAX_VALUE }
            ?.run { items.addAll(this) }

        if (result?.aircraft != null) {
            if (result.searching) {
                items.add(SearchingAircraftVH.Item(input, result.aircraft.size))
            } else if (result.aircraft.isEmpty()) {
                NoAircraftVH.Item(
                    input,
                    onStartFeeding = {
                        webpageTool.open(AirplanesLive.URL_START_FEEDING)
                    }
                ).run { items.add(this) }
            } else {
                items.add(0, SummaryVH.Item(result.aircraft.size))
            }
        }

        State(
            input = input,
            isSearching = result?.searching ?: false,
            items = items,
        )
    }.catch { }.asLiveData2()

    fun search(input: Input) {
        log(TAG) { "search($input)" }
        if (currentInput.value == input) {
            searchTrigger.value = UUID.randomUUID()
        } else {
            currentInput.value = input
        }
    }

    fun updateSearchText(raw: String) {
        log(TAG) { "updateSearchText($raw)" }
        search(
            currentInput.value.copy(
                raw = raw,
            )
        )
    }

    fun updateMode(mode: State.Mode) {
        log(TAG) { "updateMode($mode)" }

        val oldInput = currentInput.value
        search(
            oldInput.copy(
                raw = when {
                    mode == State.Mode.INTERESTING -> "military,ladd,pia"
                    mode == State.Mode.SQUAWK -> "7700,7600,7500"
                    mode == State.Mode.POSITION -> "" // TODO
                    else -> ""
                },
                mode = mode,
            )
        )
    }

    data class State(
        val input: Input,
        val items: List<SearchAdapter.Item>,
        val isSearching: Boolean = false,
    ) {
        enum class Mode {
            ALL,
            HEX,
            CALLSIGN,
            REGISTRATION,
            SQUAWK,
            AIRFRAME,
            INTERESTING,
            POSITION,
            ;
        }
    }

    data class Input(
        val raw: String,
        val mode: State.Mode,
    )
}