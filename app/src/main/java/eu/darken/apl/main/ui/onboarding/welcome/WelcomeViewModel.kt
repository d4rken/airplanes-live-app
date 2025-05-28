package eu.darken.apl.main.ui.onboarding.welcome

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
) : ViewModel3(
    dispatcherProvider = dispatcherProvider,
    tag = logTag("Welcome", "ViewModel"),
) {

    fun finishWelcome() = launch {
        WelcomeFragmentDirections.actionWelcomeFragmentToPrivacyFragment().navigate()
    }

}