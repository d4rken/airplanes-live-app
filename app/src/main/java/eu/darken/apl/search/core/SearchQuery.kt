package eu.darken.apl.search.core

import android.location.Location
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.SquawkCode

sealed interface SearchQuery {

    val isEmpty: Boolean

    data class All(val terms: Set<String> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = terms.isEmpty()
    }

    data class Hex(val hexes: Set<AircraftHex> = emptySet()) : SearchQuery {
        constructor(hex: AircraftHex) : this(setOf(hex))

        override val isEmpty: Boolean
            get() = hexes.isEmpty()
    }

    data class Callsign(val signs: Set<eu.darken.apl.main.core.aircraft.Callsign> = emptySet()) : SearchQuery {

        constructor(callsign: eu.darken.apl.main.core.aircraft.Callsign) : this(setOf(callsign))

        override val isEmpty: Boolean
            get() = signs.isEmpty()
    }

    data class Registration(val regs: Set<eu.darken.apl.main.core.aircraft.Registration> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = regs.isEmpty()
    }

    data class Squawk(val codes: Set<SquawkCode> = emptySet()) : SearchQuery {

        constructor(code: SquawkCode) : this(setOf(code))

        override val isEmpty: Boolean
            get() = codes.isEmpty()
    }

    data class Airframe(val types: Set<eu.darken.apl.main.core.aircraft.Airframe> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = types.isEmpty()
    }

    data class Interesting(
        val military: Boolean = false,
        val ladd: Boolean = false,
        val pia: Boolean = false,
    ) : SearchQuery {
        override val isEmpty: Boolean
            get() = !military && !ladd && !pia
    }

    data class Position(val location: Location = Location("empty")) : SearchQuery {
        override val isEmpty: Boolean
            get() = location.provider == "empty"
    }
}