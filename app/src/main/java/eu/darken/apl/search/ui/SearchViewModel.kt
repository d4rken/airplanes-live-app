package eu.darken.apl.search.ui

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.SingleEventFlow
import eu.darken.apl.common.flow.combine
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.common.flow.throttleLatest
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.GeneralSettings
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.map.core.AirplanesLive
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import eu.darken.apl.search.ui.items.AircraftResultVH
import eu.darken.apl.search.ui.items.LocationPromptVH
import eu.darken.apl.search.ui.items.NoAircraftVH
import eu.darken.apl.search.ui.items.SearchingAircraftVH
import eu.darken.apl.search.ui.items.SummaryVH
import eu.darken.apl.watch.core.WatchRepo
import eu.darken.apl.watch.core.types.AircraftWatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
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
    private val watchRepo: WatchRepo,
) : ViewModel3(
    dispatcherProvider,
    tag = logTag("Search", "ViewModel"),
) {

    private val args by handle.navArgs<SearchFragmentArgs>()
    private val targetHexes: Set<AircraftHex>?
        get() = args.targetHexes?.toSet()
    private val targetSquawks: Set<SquawkCode>?
        get() = args.targetSquawks?.toSet()


    init {
        log(tag, INFO) { "targetHexes=$targetHexes, targetSquawks=$targetSquawks" }
    }

    val events = SingleEventFlow<SearchEvents>()

    private val currentInput = MutableStateFlow<Input?>(null)

    private val searchTrigger = MutableStateFlow(UUID.randomUUID())
    private val currentSearch: Flow<SearchRepo.Result?> = combine(
        searchTrigger,
        currentInput.filterNotNull(),
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

            State.Mode.POSITION -> {
                var location = input.rawMeta as? Location
                if (location == null) {
                    location = locationManager2.fromName(input.raw.trim())
                }
                if (location != null) {
                    SearchQuery.Position(location)
                } else {
                    SearchQuery.Position()
                }
            }
        }.also { log(tag) { "Mapped raw query: '$input' to $it" } }
    }
        .map { searchRepo.liveSearch(it) }
        .flatMapLatest { it }
        .replayingShare(viewModelScope)

    private suspend fun getCurrentLocationSearch(): Input? {
        val locationState = locationManager2.state
            .filter { it !is LocationManager2.State.Waiting }
            .first()
        if (locationState !is LocationManager2.State.Available) return null

        val location = locationState.location
        val symbols = DecimalFormatSymbols(Locale.US) // Ensure the use of period as decimal separator
        val formatter = DecimalFormat("#.##", symbols)
        val roundedLat = formatter.format(location.latitude).toDouble()
        val roundedLon = formatter.format(location.longitude).toDouble()
        val altText = "${roundedLat},${roundedLon}"
        val address = locationManager2.toName(location)
        return Input(
            raw = address?.locality ?: altText,
            rawMeta = location,
            State.Mode.POSITION
        )
    }

    init {
        log(tag) { "init with handle: $handle" }
        launch {
            if (currentInput.value != null) return@launch

            currentInput.value = when {
                targetHexes != null -> Input(
                    mode = State.Mode.HEX,
                    raw = targetHexes!!.joinToString(","),
                )

                targetSquawks != null -> Input(
                    mode = State.Mode.SQUAWK,
                    raw = targetSquawks!!.joinToString(","),
                )

                else -> {
                    getCurrentLocationSearch() ?: Input()
                }
            }.also { log(tag) { "Setting initial search to $it" } }
        }
    }

    val state = combine(
        currentInput.filterNotNull(),
        currentSearch.throttleLatest(500),
        watchRepo.watches,
        generalSettings.searchLocationDismissed.flow,
        locationManager2.state,
    ) { input, result, alerts, locationDismissed, locationState ->
        val items = mutableListOf<SearchAdapter.Item>()

        if (!locationDismissed && (locationState as? LocationManager2.State.Unavailable)?.isPermissionIssue == true) {
            LocationPromptVH.Item(
                locationState,
                onGrant = {
                    events.emitBlocking(SearchEvents.RequestLocationPermission)
                },
                onDismiss = {
                    generalSettings.searchLocationDismissed.valueBlocking = true
                }
            ).run { items.add(this) }
        }

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
                items.add(SummaryVH.Item(result.aircraft.size))
            }
        }

        result?.aircraft
            ?.map { ac ->
                AircraftResultVH.Item(
                    aircraft = ac,
                    watch = alerts.filterIsInstance<AircraftWatch>().firstOrNull { it.matches(ac) },
                    distanceInMeter = if (locationState is LocationManager2.State.Available && ac.location != null) {
                        locationState.location.distanceTo(ac.location!!)
                    } else {
                        null
                    },
                    onTap = {
                        SearchFragmentDirections.actionSearchToSearchAction(
                            hex = ac.hex,
                        ).navigate()
                    },
                    onThumbnail = { launch { webpageTool.open(it.link) } },
                    onWatch = {
                        SearchFragmentDirections.actionSearchToWatchlistDetailsFragment(it.id).navigate()
                    },
                )
            }
            ?.sortedBy { it.distanceInMeter ?: Float.MAX_VALUE }
            ?.run { items.addAll(this) }

        State(
            input = input,
            isSearching = result?.searching ?: false,
            items = items,
        )
    }.catch { }.asStateFlow()

    fun search(input: Input) {
        log(tag) { "search($input)" }
        if (currentInput.value == input) {
            searchTrigger.value = UUID.randomUUID()
        } else {
            currentInput.value = input
        }
    }

    fun updateSearchText(raw: String) = launch {
        log(tag) { "updateSearchText($raw)" }
        val oldInput = currentInput.value ?: Input()
        val newInput = when (oldInput.mode) {
            State.Mode.POSITION -> if (raw.trim().isNotEmpty()) {
                oldInput.copy(
                    raw = raw,
                    rawMeta = locationManager2.fromName(raw.trim())
                )
            } else {
                getCurrentLocationSearch() ?: Input()
            }

            else -> oldInput.copy(raw = raw, rawMeta = null)
        }

        log(tag) { "updateSearchText(): $newInput <- $oldInput" }
        search(newInput)
    }

    fun updateMode(mode: State.Mode) = launch {
        log(tag) { "updateMode($mode)" }
        val oldInput = currentInput.value ?: Input()
        val newInput = when (mode) {
            State.Mode.REGISTRATION -> oldInput.copy(mode = mode, raw = "HO-HOHO", rawMeta = null)
            State.Mode.AIRFRAME -> oldInput.copy(mode = mode, raw = "A320", rawMeta = null)
            State.Mode.SQUAWK -> oldInput.copy(mode = mode, raw = "7700,7600,7500", rawMeta = null)
            State.Mode.INTERESTING -> oldInput.copy(mode = mode, raw = "military,ladd,pia", rawMeta = null)
            State.Mode.POSITION -> getCurrentLocationSearch() ?: Input("", mode = State.Mode.POSITION, rawMeta = null)
            else -> oldInput.copy(mode = mode, raw = "", rawMeta = null)
        }
        log(tag) { "updateMode(): $newInput <- $oldInput" }
        search(newInput)
    }

    fun showOnMap(items: Collection<SearchAdapter.Item>) {
        log(tag) { "showOnMap(${items.size} items)" }
        val acs = items.filterIsInstance<AircraftResultVH.Item>().map { it.aircraft }
        if (acs.isEmpty()) return
        SearchFragmentDirections.actionSearchToMap(
            mapOptions = MapOptions.focusAircraft(acs.toSet())
        ).navigate()
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
        val raw: String = "military, pia, ladd",
        val rawMeta: Any? = null,
        val mode: State.Mode = State.Mode.INTERESTING,
    )
}