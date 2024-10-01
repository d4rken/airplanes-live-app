package eu.darken.apl.map.core

import android.webkit.JavascriptInterface
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class AndroidWebInterface @AssistedInject constructor(
    @Assisted private val listener: Listener,
) {

    interface Listener {
        fun onHomePressed()
        fun onUrlChanged(newUrl: String)
    }

    @JavascriptInterface
    fun onHomePressed() {
        listener.onHomePressed()
    }

    @JavascriptInterface
    fun onUrlChanged(newUrl: String) {
        listener.onUrlChanged(newUrl)
    }

    @AssistedFactory
    interface Factory {
        fun create(listener: Listener): AndroidWebInterface
    }
}