package eu.darken.apl.watchlist.core.types

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.history.WatchCheck

sealed interface Watch {

    val id: WatchId
    val note: String

    fun matches(ac: Aircraft): Boolean

    sealed interface Status {
        val watch: Watch
        val id: WatchId
            get() = watch.id
        val note: String
            get() = watch.note

        val lastCheck: WatchCheck?
        val lastHit: WatchCheck?

        val tracked: Set<Aircraft>
    }
}