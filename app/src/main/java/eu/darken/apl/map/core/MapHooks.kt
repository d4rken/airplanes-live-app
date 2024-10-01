package eu.darken.apl.map.core

import android.webkit.WebView
import eu.darken.apl.common.debug.logging.log

internal fun WebView.setupButtonHook(
    elementId: String,
    hookName: String
) {
    log(MapHandler.TAG) { "Setting up hook for '$hookName(...)' on '$elementId'" }
    val jsCode = """
            (function() {
                var button = document.getElementById('$elementId');
                if (button && !button.dataset.listenerAdded) {
                    button.addEventListener('click', function() {
                        if(event.isTrusted) {
                            Android.$hookName();
                        }
                    });
                    button.dataset.listenerAdded = 'true';
                }
            })();
        """.trimIndent()
    evaluateJavascript(jsCode, null)
}

internal fun WebView.setupUrlChangeHook() {
    log(MapHandler.TAG) { "Setting up hook for URL change events" }
    val jsCode = """
            (function() {
                if (!window.urlChangeListenerAdded) {
                    var pushState = history.pushState;
                    history.pushState = function() {
                        pushState.apply(history, arguments);
                        Android.onUrlChanged(window.location.href);
                    };
                    var replaceState = history.replaceState;
                    history.replaceState = function() {
                        replaceState.apply(history, arguments);
                        Android.onUrlChanged(window.location.href);
                    };
                    window.urlChangeListenerAdded = true;
                }
            })();
        """.trimIndent()
    evaluateJavascript(jsCode, null)
}