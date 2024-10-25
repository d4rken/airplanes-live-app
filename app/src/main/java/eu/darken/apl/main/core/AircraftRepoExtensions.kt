package eu.darken.apl.main.core

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun AircraftRepo.getByHex(hex: AircraftHex): Flow<Aircraft?> = aircraft.map { acs ->
    acs[hex]
}