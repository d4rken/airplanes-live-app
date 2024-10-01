package eu.darken.apl.search.core

import android.location.Location

sealed interface SearchQuery {

    val isEmpty: Boolean

    data class All(val terms: Set<String> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = terms.isEmpty()
    }

    data class Hex(val hexes: Set<String> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = hexes.isEmpty()
    }

    data class Callsign(val signs: Set<String> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = signs.isEmpty()
    }

    data class Registration(val regs: Set<String> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = regs.isEmpty()
    }

    data class Squawk(val codes: Set<String> = emptySet()) : SearchQuery {
        override val isEmpty: Boolean
            get() = codes.isEmpty()
    }

    data class Airframe(val types: Set<String> = emptySet()) : SearchQuery {
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