package eu.darken.apl.common.error

import eu.darken.apl.common.livedata.SingleLiveEvent

interface ErrorEventSource {
    val errorEvents: SingleLiveEvent<Throwable>
}