package eu.darken.apl.main.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.SponsorHelper
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.uix.ViewModel2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    private val sponsorHelper: SponsorHelper,
) : ViewModel2(dispatcherProvider = dispatcherProvider) {

    private val stateFlow = MutableStateFlow(State())
    val state = stateFlow
        .onEach { log(VERBOSE) { "New state: $it" } }
        .asLiveData2()

    private val readyStateInternal = MutableStateFlow(true)
    val readyState = readyStateInternal.asLiveData2()

    fun onGo() {
        stateFlow.value = stateFlow.value.copy(ready = true)
    }

    fun goSponsor() = launch {
        sponsorHelper.openSponsorPage()
    }

    data class State(
        val ready: Boolean = false
    )

}