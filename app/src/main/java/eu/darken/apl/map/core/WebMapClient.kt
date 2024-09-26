package eu.darken.apl.map.core

import android.annotation.SuppressLint
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.Reusable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WebMapClient @AssistedInject constructor(
    @Assisted private val webView: WebView,
    private val appInterface: AppInterface,
) : WebViewClient() {

    init {
        log(TAG) { "init($webView)" }
        webView.apply {
            webViewClient = this@WebMapClient
            addJavascriptInterface(appInterface, "AndroidInterface")
            settings.apply {
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = false
                setGeolocationEnabled(true)
            }
            webChromeClient = object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback,
                ) {
                    log(TAG) { "onGeolocationPermissionsShowPrompt($origin,$callback)" }
                    callback.invoke(origin, true, false)
                }
            }
        }
    }

    val events = appInterface.events
        .onEach { log(TAG) { "event: $it" } }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        log(TAG) { "onPageFinished(): $url $view" }

        if (url.contains("globe.airplanes.live")) {
            view.setupButtonHook("H", "onHomePressed")
        } else {
            log(TAG, WARN) { "Skipping inject, not globe.airplanes.live" }
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url?.toString()
        val allowed = url?.startsWith("https://globe.airplanes.live") == true
        log(TAG, VERBOSE) { "Is URL allowed? $allowed -> $url" }
        return !allowed
    }

    fun clickHome() {
        log(TAG) { "clickHome()" }
        val jsCode = "document.getElementById('H').click();"
        webView.evaluateJavascript(jsCode, null)
    }

    private fun WebView.setupButtonHook(
        elementId: String,
        hookName: String
    ) {
        log(TAG) { "Setting up hook for '$hookName(...)' on '$elementId'" }
        val jsCode = """
            (function() {
                var button = document.getElementById('$elementId');
                if (button && !button.dataset.listenerAdded) {
                    button.addEventListener('click', function() {
                        if(event.isTrusted) {
                            window.AndroidInterface.$hookName();
                        }
                    });
                    button.dataset.listenerAdded = 'true';
                }
            })();
        """.trimIndent()
        evaluateJavascript(jsCode, null)
    }

    @Reusable
    class AppInterface @Inject constructor() {

        val events = MutableSharedFlow<Event>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

        private fun sendEvent(event: Event) {
            val success = events.tryEmit(event)
            log(TAG) { "Sending $event = $success" }
        }

        sealed interface Event {
            data object HomePressed : Event
        }

        @JavascriptInterface
        fun onHomePressed() {
            sendEvent(Event.HomePressed)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(webView: WebView): WebMapClient
    }

    companion object {
        private val TAG = logTag("Map", "Web", "Client")
    }
}