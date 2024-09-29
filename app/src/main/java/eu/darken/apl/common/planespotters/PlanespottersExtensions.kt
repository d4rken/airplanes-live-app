package eu.darken.apl.common.planespotters

import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.planespotters.coil.PlanespottersImage
import eu.darken.apl.main.core.aircraft.Aircraft

fun PlanespottersThumbnailView.load(
    aircraft: Aircraft,
): Disposable? {
    log(TAG, VERBOSE) { "Loading $aircraft into $this" }
    val current = tag as? Aircraft
    if (current?.hex == aircraft.hex) return null
    tag = current

    val request = ImageRequest.Builder(context).apply {
        data(aircraft)
        listener(
            onStart = { request ->
                log(TAG) { "onStart: $request for $aircraft" }
                setImage(null)
            },
            onCancel = { request ->
                log(TAG) { "onCancel: $request for $aircraft" }
                setImage(null)
            },
            onSuccess = { request, result ->
                log(TAG) { "onStart: $result for $request for $aircraft" }

                setImage(result.drawable as PlanespottersImage)
            },
            onError = { request, error ->
                log(TAG) { "onError: $error for $request for $aircraft:\n${error.throwable?.asLog()}" }
                setImage(null)
            }
        )
    }.build()

    return context.imageLoader.enqueue(request)
}

private val TAG = logTag("Planespotters", "Extensions")
