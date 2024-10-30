package eu.darken.apl.watchlist.ui.create

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.watchlist.core.WatchlistRepo
import javax.inject.Inject

@HiltViewModel
class CreateFlightWatchViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val watchlistRepo: WatchlistRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args by handle.navArgs<CreateFlightWatchFragmentArgs>()
    val initCallsign: Callsign?
        get() = args.callsign
    val initNote: String?
        get() = args.note

    init {
        log(TAG, INFO) { "initCallsign=$initCallsign, initNote=$initNote" }
    }

    fun create(callsign: Callsign, note: String) = launch {
        log(TAG) { "create($callsign, $note)" }
        watchlistRepo.createFlight(callsign, note.trim())
        popNavStack()
    }
}