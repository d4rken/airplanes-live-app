package eu.darken.apl.main.ui.settings.general

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel3(
    dispatcherProvider,
    tag = logTag("Settings", "General", "VM"),
)