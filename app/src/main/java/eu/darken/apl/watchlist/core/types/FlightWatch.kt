package eu.darken.apl.watchlist.core.types

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.db.types.BaseWatchEntity
import eu.darken.apl.watchlist.core.db.types.FlightWatchEntity
import eu.darken.apl.watchlist.core.history.WatchCheck


data class FlightWatch(
    private val base: BaseWatchEntity,
    private val specific: FlightWatchEntity,
) : Watch {
    override val id: WatchId
        get() = specific.id

    override val note: String
        get() = base.userNote

    val callsign: Callsign
        get() = specific.callsign

    override fun matches(ac: Aircraft): Boolean {
        return ac.callsign?.uppercase() == callsign.uppercase()
    }

    data class Status(
        override val watch: FlightWatch,
        override val lastCheck: WatchCheck?,
        override val lastHit: WatchCheck?,
        override val tracked: Set<Aircraft> = emptySet(),
    ) : Watch.Status {

        val callsign: Callsign
            get() = watch.callsign
    }
}