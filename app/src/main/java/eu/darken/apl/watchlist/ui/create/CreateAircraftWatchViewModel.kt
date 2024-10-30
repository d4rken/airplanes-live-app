package eu.darken.apl.watchlist.ui.create

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.watchlist.core.WatchlistRepo
import javax.inject.Inject

@HiltViewModel
class CreateAircraftWatchViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val watchlistRepo: WatchlistRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args by handle.navArgs<CreateAircraftWatchFragmentArgs>()
    val initHex: AircraftHex?
        get() = args.hex
    val initNote: String?
        get() = args.note

    init {
        log(TAG, INFO) { "initHex=$initHex, initNote=$initNote" }
    }

    fun create(hex: AircraftHex, note: String) = launch {
        log(TAG) { "create($hex, $note)" }
        watchlistRepo.createAircraft(hex.uppercase(), note.trim())
        popNavStack()
    }
}