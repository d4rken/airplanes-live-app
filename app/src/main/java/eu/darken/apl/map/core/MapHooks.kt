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

internal fun WebView.setupShowInSearch() {
    log(MapHandler.TAG) { "Setting up 'Show in search' button and creating hook" }
    val jsCode = """
        (function() {
            new MutationObserver(function(mutations) {
                mutations.forEach(function() {
                    var targetDiv = document.getElementById('selected_icao');
                    if (targetDiv && !document.querySelector('#android_show_search')) {
                        var button = document.createElement('button');
                        button.id = 'android_show_search';
                        button.textContent = 'Show in search';
                        button.style = 'margin-top: 10px; width: 100%';
                        targetDiv.parentNode.insertBefore(button, targetDiv.nextSibling);
        
                        button.addEventListener('click', function() {
                            if (window.getComputedStyle(targetDiv).display !== "none") {
                                var hexText = targetDiv.textContent.match(/Hex:\s*([0-9A-F]+)/i);
                                var hex = hexText ? hexText[1] : "N/A";
                                if (hex !== "N/A") {
                                    Android.onShowInSearch(hex); // Calls the Android interface method with the hex value
                                }
                            }
                        });
                    }
                });
            }).observe(document.body, { childList: true, subtree: true });
        })();
    """.trimIndent()
    evaluateJavascript(jsCode, null)
}