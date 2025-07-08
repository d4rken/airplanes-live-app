package eu.darken.apl.watch.ui.create

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.watch.core.WatchRepo
import javax.inject.Inject

@HiltViewModel
class CreateAircraftWatchViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val watchRepo: WatchRepo,
) : ViewModel3(
    dispatcherProvider = dispatcherProvider,
    tag = logTag("Aircraft", "Create", "ViewModel")
) {

    private val args by handle.navArgs<CreateAircraftWatchFragmentArgs>()
    val initHex: AircraftHex?
        get() = args.hex
    val initNote: String?
        get() = args.note

    init {
        log(tag, INFO) { "initHex=$initHex, initNote=$initNote" }
    }

    fun create(hex: AircraftHex, note: String) = launch {
        log(tag) { "create($hex, $note)" }
        watchRepo.createAircraft(hex.uppercase(), note.trim())
        popNavStack()
    }
}