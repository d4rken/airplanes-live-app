package eu.darken.apl.watch.ui.create

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.watch.core.WatchlistRepo
import javax.inject.Inject

@HiltViewModel
class CreateSquawkWatchViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val watchlistRepo: WatchlistRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val args by handle.navArgs<CreateSquawkWatchFragmentArgs>()
    val initSquawk: SquawkCode?
        get() = args.squawk
    val initNote: String?
        get() = args.note

    init {
        log(TAG, INFO) { "initSquawk=$initSquawk, initNote=$initNote" }
    }

    fun create(code: SquawkCode, note: String) = launch {
        log(TAG) { "create($code, $note)" }
        watchlistRepo.createSquawk(code, note.trim())
        popNavStack()
    }
}