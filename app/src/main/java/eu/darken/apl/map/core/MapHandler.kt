package eu.darken.apl.map.core

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.AircraftHex
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class MapHandler @AssistedInject constructor(
    @Assisted private val webView: WebView,
    private val mapWebInterfaceFactory: MapWebInterface.Factory,
) : WebViewClient() {

    private lateinit var currentOptions: MapOptions
    private val interfaceListener = object : MapWebInterface.Listener {
        override fun onHomePressed() {
            sendEvent(Event.HomePressed)
        }

        override fun onUrlChanged(newUrl: String) {
            val old = currentOptions
            currentOptions = old.copy(
                filter = old.filter.copy(
                    icaos = newUrl
                        .takeIf { it.contains("icao=") }
                        ?.substringAfter("icao=")
                        ?.substringBefore("&")
                        ?.split(",")
                        ?.filter { it.isNotEmpty() }
                        ?.toSet()
                        ?: emptySet()
                )
            )
            sendEvent(Event.OptionsChanged(currentOptions))
        }

        override fun onShowInSearch(hex: AircraftHex) {
            sendEvent(Event.ShowInSearch(hex))
        }

        override fun onAddAlert(hex: AircraftHex) {
            sendEvent(Event.AddAlert(hex))
        }
    }

    init {
        log(TAG) { "init($webView)" }
        webView.apply {
            webViewClient = this@MapHandler
            addJavascriptInterface(mapWebInterfaceFactory.create(interfaceListener), "Android")
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

                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    log(TAG, VERBOSE) { "Console: ${message.message()}" }
                    return super.onConsoleMessage(message)
                }
            }
        }
    }

    val events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    internal fun sendEvent(event: Event) {
        val success = events.tryEmit(event)
        log(TAG) { "Sending $event = $success" }
    }

    sealed interface Event {
        data object HomePressed : Event
        data class OpenUrl(val url: String) : Event
        data class OptionsChanged(val options: MapOptions) : Event
        data class ShowInSearch(val hex: AircraftHex) : Event
        data class AddAlert(val hex: AircraftHex) : Event
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        log(TAG) { "onPageStarted(): $url $view" }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        log(TAG) { "onPageFinished(): $url $view" }

        if (url.contains("globe.airplanes.live")) {
            view.setupUrlChangeHook()
            view.setupButtonHook("H", "onHomePressed")
            view.setupAddAlert()
            view.setupShowInSearch()
        } else {
            log(TAG, WARN) { "Skipping inject, not globe.airplanes.live" }
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url?.toString() ?: return true

        val isInternal = url.startsWith("https://globe.airplanes.live/")

        if (isInternal) {
            log(TAG, VERBOSE) { "Allowing internal URL: $url" }
        } else {
            log(TAG, INFO) { "Not an allowed internal URL, opening external: $url" }
            sendEvent(Event.OpenUrl(url))
        }

        return !isInternal
    }

    fun loadMap(options: MapOptions) {
        log(TAG, INFO) { "loadMap($options)" }

        val url = options.createUrl()

        if (webView.url != null && currentOptions.filter == options.filter) {
            log(TAG) { "Url already loaded, skipped." }
            return
        }
        currentOptions = options

        webView.loadUrl(url)
    }

    fun clickHome() {
        log(TAG) { "clickHome()" }
        val jsCode = "document.getElementById('H').click();"
        webView.evaluateJavascript(jsCode, null)
    }


    @AssistedFactory
    interface Factory {
        fun create(webView: WebView): MapHandler
    }

    companion object {
        internal val TAG = logTag("Map", "Handler")
    }
}