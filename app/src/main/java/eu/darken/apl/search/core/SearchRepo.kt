package eu.darken.apl.search.core

import androidx.core.text.isDigitsOnly
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Airframe
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.Registration
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.main.core.api.AplEndpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepo @Inject constructor(
    private val endpoint: AplEndpoint,
) {

    data class Query(
        val term: String,
        val type: Type = Type.ALL,
    ) {
        enum class Type {
            HEX, SQUAWK, ALL
        }
    }

    data class Result(
        val aircraft: Collection<Aircraft>,
        val searching: Boolean,
    )

    suspend fun search(query: Query): Flow<Result> {
        log(TAG) { "search($query)" }
        val squawks = mutableSetOf<SquawkCode>()
        val hexes = mutableSetOf<AircraftHex>()
        val airframes = mutableSetOf<Airframe>()
        val callsigns = mutableSetOf<Callsign>()
        val registrations = mutableSetOf<Registration>()
        query.term
            .split(",")
            .let { items ->
                squawks.addAll(items.filter { it.length == 4 && it.isDigitsOnly() })
                hexes.addAll(items.filter { it.length == 6 })
                airframes.addAll(items.filter { it.length <= 5 })
                callsigns.addAll(items.filter { it.length in 5..8 })
                registrations.addAll(items.filter { it.length in 5..8 })
            }
        return combine(
            flow { emit(endpoint.getBySquawk(squawks) as Collection<Aircraft>?) }.map { it }.onStart { emit(null) },
            flow { emit(endpoint.getByHex(hexes) as Collection<Aircraft>?) }.map { it }.onStart { emit(null) },
            flow { emit(endpoint.getByAirframe(airframes) as Collection<Aircraft>?) }.map { it }.onStart { emit(null) },
            flow { emit(endpoint.getByCallsign(callsigns) as Collection<Aircraft>?) }.map { it }.onStart { emit(null) },
            flow { emit(endpoint.getByRegistration(registrations) as Collection<Aircraft>?) }.onStart { emit(null) },
        ) { squawkAc, hexAc, airframeAc, callsignAc, registrationAc ->
            val ac = mutableSetOf<Aircraft>()

            squawkAc?.let { ac.addAll(it) }
            hexAc?.let { ac.addAll(it) }
            airframeAc?.let { ac.addAll(it) }
            callsignAc?.let { ac.addAll(it) }
            registrationAc?.let { ac.addAll(it) }

            Result(
                aircraft = ac,
                searching = listOf(squawkAc, hexAc, airframeAc, callsignAc, registrationAc).any { it == null }
            )
        }
    }

    companion object {
        private val TAG = logTag("Search", "Repo")
    }
}