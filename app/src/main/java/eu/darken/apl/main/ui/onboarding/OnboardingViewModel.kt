package eu.darken.apl.main.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.main.core.GeneralSettings
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    fun finishOnboarding() = launch {
        generalSettings.isOnboardingFinished.value(true)
        OnboardingFragmentDirections.actionOnboardingFragmentToMainFragment().navigate()
    }

}