package eu.darken.apl.search.core

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun SearchRepo.getByHex(hex: AircraftHex): Flow<Aircraft?> = cache.map { acs ->
    acs[hex]
}