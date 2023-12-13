package eu.darken.apl.common.uix

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag

abstract class ViewModel1 : ViewModel() {
    val TAG: String = logTag("VM", javaClass.simpleName)

    init {
        log(TAG) { "Initialized" }
    }

    @CallSuper
    override fun onCleared() {
        log(TAG) { "onCleared()" }
        super.onCleared()
    }
}