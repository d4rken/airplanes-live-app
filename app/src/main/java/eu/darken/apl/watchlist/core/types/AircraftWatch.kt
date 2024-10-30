package eu.darken.apl.watchlist.core.types

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.db.types.AircraftWatchEntity
import eu.darken.apl.watchlist.core.history.WatchCheck


data class AircraftWatch(
    private val entity: AircraftWatchEntity,
) : Watch {
    override val id: WatchId
        get() = entity.id

    override val note: String
        get() = entity.userNote

    val hex: AircraftHex
        get() = entity.hexCode

    override fun matches(ac: Aircraft): Boolean {
        return ac.hex.uppercase() == hex.uppercase()
    }

    data class Status(
        override val watch: AircraftWatch,
        override val lastCheck: WatchCheck?,
        override val lastHit: WatchCheck?,
        override val tracked: Set<Aircraft> = emptySet(),
    ) : Watch.Status {

        val hex: AircraftHex
            get() = watch.hex
    }
}