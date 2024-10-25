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
                    var infoBlockDiv = document.getElementById('selected_infoblock');
                    if (!targetDiv || !infoBlockDiv || window.getComputedStyle(infoBlockDiv).display === "none" || document.querySelector('#android_show_search')) return;
                    
                    var button = document.createElement('button');
                    button.id = 'android_show_search';
                    button.textContent = 'Show in search';
                    button.style = 'margin-top: 10px; width: 100%';
                    targetDiv.parentNode.insertBefore(button, targetDiv.nextSibling);
                    
                    button.addEventListener('click', function() {
                        if (window.getComputedStyle(targetDiv).display === "none") return;
                        var hexText = targetDiv.textContent.match(/Hex:\s*([0-9A-F]+)/i);
                        var hex = hexText ? hexText[1] : "N/A";
                        if (hex !== "N/A") {
                            Android.onShowInSearch(hex);
                        }
                    });
                });
            }).observe(document.body, { childList: true, subtree: true });
        })();
    """.trimIndent()
    evaluateJavascript(jsCode, null)
}

internal fun WebView.setupAddAlert() {
    log(MapHandler.TAG) { "Setting up 'Add alert' button and creating hook" }
    val jsCode = """
        var alertCountInterval = null;
        
        (function() {
            const observer = new MutationObserver(function(mutations) {
                var targetDiv = document.getElementById('selected_icao');
                var infoBlockDiv = document.getElementById('selected_infoblock');
                
                mutations.forEach(function(mutation) {
                    if (!mutation.target.closest('#selected_icao') || !targetDiv) return;
                    if (!infoBlockDiv || window.getComputedStyle(infoBlockDiv).display === "none") return;
                    
                    observer.disconnect();
                    
                    var existingButton = document.querySelector('#android_add_alert');
                    if (existingButton) existingButton.remove();
                    
                    var hexText = targetDiv.textContent ? targetDiv.textContent.match(/Hex:\s*([0-9A-F]+)/i) : null;
                    var hex = hexText ? hexText[1] : "N/A";
                    console.log('TargetDiv content:', targetDiv.textContent);
                    console.log('Hex value obtained:', hex);
                    
                    var button = document.createElement('button');
                    button.id = 'android_add_alert';
                    button.textContent = 'Add alert';
                    button.style = 'margin-top: 10px; width: 100%';
                    targetDiv.parentNode.insertBefore(button, targetDiv.nextSibling);
                    
                    if (hex !== "N/A") updateAlertCount(button, hex);
                    
                    button.addEventListener('click', function() {
                        if (hex === "N/A" || window.getComputedStyle(infoBlockDiv).display === "none") return;
                        Android.onAddAlert(hex);
                        button.disabled = true;
                    });
                    
                    observer.observe(targetDiv, { childList: true, subtree: true, characterData: true });
                    
                    if (alertCountInterval) clearInterval(alertCountInterval);
                    
                    if (hex && hex !== "N/A") {
                        alertCountInterval = setTimeout(function() {
                            updateAlertCount(button, hex);
                            alertCountInterval = setInterval(function() {
                                updateAlertCount(button, hex);
                            }, 5000);
                        }, 5000);
                    }
                });
            });
            
            const targetDiv = document.getElementById('selected_icao');
            if (targetDiv) observer.observe(targetDiv, { childList: true, subtree: true, characterData: true });
        })();
        
        function updateAlertCount(button, hex) {
            var alertCount = Android.getAlertCount(hex);
            if (alertCount > 0) {
                button.textContent = 'Alert exists';
                button.disabled = true;
            } else {
                button.textContent = 'Add alert';
                button.disabled = false;
            }
        }
    """.trimIndent()
    evaluateJavascript(jsCode, null)
}
