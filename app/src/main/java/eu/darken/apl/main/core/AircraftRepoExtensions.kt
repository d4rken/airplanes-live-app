package eu.darken.apl.main.core

import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Callsign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

fun AircraftRepo.getByHex(hex: AircraftHex): Flow<Aircraft?> = aircraft.map { acs ->
    acs[hex]
}

suspend fun AircraftRepo.findByHex(hex: AircraftHex): Aircraft? = aircraft.first()[hex]

suspend fun AircraftRepo.findByCallsign(callsign: Callsign): Aircraft? =
    aircraft.first().values.firstOrNull { it.callsign == callsign }