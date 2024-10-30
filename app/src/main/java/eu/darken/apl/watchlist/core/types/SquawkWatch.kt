package eu.darken.apl.watchlist.core.types

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.db.types.SquawkWatchEntity
import eu.darken.apl.watchlist.core.history.WatchCheck

data class SquawkWatch(
    private val entity: SquawkWatchEntity,
) : Watch {
    override val id: WatchId
        get() = entity.id
    override val note: String
        get() = entity.userNote

    val code: SquawkCode
        get() = entity.code

    override fun matches(ac: Aircraft): Boolean {
        return ac.squawk?.uppercase() == code.uppercase()
    }

    data class Status(
        override val watch: SquawkWatch,
        override val lastCheck: WatchCheck?,
        override val lastHit: WatchCheck?,
        override val tracked: Set<Aircraft> = emptySet(),
    ) : Watch.Status {
        val squawk: SquawkCode
            get() = watch.code
    }
}