package eu.darken.apl.main.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.uix.ViewModel3
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @Suppress("unused") private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
) : ViewModel3(dispatcherProvider)