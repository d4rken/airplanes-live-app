package eu.darken.apl.map.core

import android.webkit.JavascriptInterface
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.apl.main.core.aircraft.AircraftHex

class MapWebInterface @AssistedInject constructor(
    @Assisted private val listener: Listener,
) {

    interface Listener {
        fun onHomePressed()
        fun onUrlChanged(newUrl: String)
        fun onShowInSearch(hex: AircraftHex)
    }

    @JavascriptInterface
    fun onHomePressed() {
        listener.onHomePressed()
    }

    @JavascriptInterface
    fun onUrlChanged(newUrl: String) {
        listener.onUrlChanged(newUrl)
    }

    @JavascriptInterface
    fun onShowInSearch(hex: String) {
        listener.onShowInSearch(hex)
    }

    @AssistedFactory
    interface Factory {
        fun create(listener: Listener): MapWebInterface
    }
}