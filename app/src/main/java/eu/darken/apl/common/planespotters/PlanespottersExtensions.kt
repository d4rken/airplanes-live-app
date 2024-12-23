package eu.darken.apl.common.planespotters

import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.ViewSizeResolver
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.planespotters.coil.AircraftThumbnailQuery
import eu.darken.apl.common.planespotters.coil.PlanespottersImage
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex

fun PlanespottersThumbnailView.load(
    data: AircraftThumbnailQuery
): Disposable? {
    log(TAG, VERBOSE) { "Loading $data into $this" }
    val current = tag as? AircraftHex
    if (current == data.hex) return null
    tag = data.hex

    val request = ImageRequest.Builder(context).apply {
        size(ViewSizeResolver(this@load))
        data(data)
        listener(
            onStart = { request ->
                log(TAG, VERBOSE) { "onStart: $request for $data" }
                setImage(null)
            },
            onCancel = { request ->
                log(TAG) { "onCancel: $request for $data" }
                setImage(null)
            },
            onSuccess = { request, result ->
                log(TAG, VERBOSE) { "onSuccess: $result for $request for $data" }

                setImage(result.drawable as PlanespottersImage)
            },
            onError = { request, error ->
                log(TAG) { "onError: $error for $request for $data:\n${error.throwable.asLog()}" }
                setImage(null)
            }
        )
    }.build()

    return context.imageLoader.enqueue(request)
}


fun Aircraft.toPlanespottersQuery(large: Boolean = false) = AircraftThumbnailQuery(
    hex = this.hex,
    registration = this.registration,
    large = large,
)

fun PlanespottersThumbnailView.load(
    aircraft: Aircraft?,
    large: Boolean = false,
): Disposable? = if (aircraft != null) {
    load(aircraft.toPlanespottersQuery(large = large))
} else {
    setImage(null)
    null
}

private val TAG = logTag("Planespotters", "Extensions")
