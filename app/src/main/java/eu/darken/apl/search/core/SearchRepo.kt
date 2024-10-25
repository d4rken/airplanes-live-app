package eu.darken.apl.search.core

import android.location.Location
import androidx.core.text.isDigitsOnly
import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.Logging.Priority.ERROR
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.combine
import eu.darken.apl.main.core.AircraftRepo
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Airframe
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.Registration
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.main.core.api.AirplanesLiveEndpoint
import eu.darken.apl.main.core.api.getByLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepo @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val endpoint: AirplanesLiveEndpoint,
    private val aircraftRepo: AircraftRepo,
) {

    data class Result(
        val aircraft: Collection<Aircraft>,
        val searching: Boolean,
        val error: Throwable? = null,
    )

    suspend fun liveSearch(query: SearchQuery): Flow<Result> {
        log(TAG) { "liveSearch($query)" }

        val squawks = mutableSetOf<SquawkCode>()
        val hexes = mutableSetOf<AircraftHex>()
        val airframes = mutableSetOf<Airframe>()
        val callsigns = mutableSetOf<Callsign>()
        val registrations = mutableSetOf<Registration>()
        var searchMilitary = false
        var searchPia = false
        var searchLadd = false
        var location: Location? = null

        when (query) {
            is SearchQuery.All -> {
                squawks.addAll(query.terms.filter { it.length == 4 && it.isDigitsOnly() })
                hexes.addAll(query.terms.filter { it.length == 6 })
                airframes.addAll(query.terms.filter { it.length <= 5 })
                callsigns.addAll(query.terms.filter { it.length in 5..8 })
                registrations.addAll(query.terms.filter { it.length in 5..8 })
            }

            is SearchQuery.Hex -> {
                hexes.addAll(query.hexes)
            }

            is SearchQuery.Callsign -> {
                callsigns.addAll(query.signs)
            }

            is SearchQuery.Registration -> {
                registrations.addAll(query.regs)
            }

            is SearchQuery.Squawk -> {
                squawks.addAll(query.codes)
            }

            is SearchQuery.Airframe -> {
                airframes.addAll(query.types)
            }

            is SearchQuery.Interesting -> {
                searchMilitary = query.military
                searchLadd = query.ladd
                searchPia = query.pia
            }

            is SearchQuery.Position -> {
                location = query.location
//                TODO()
            }
        }

        return combine(
            flow {
                emit(endpoint.getBySquawk(squawks) as Collection<Aircraft>?)
            }.onStart { emit(null) },
            flow {
                emit(endpoint.getByHex(hexes) as Collection<Aircraft>?)
            }.onStart { emit(null) },
            flow {
                emit(endpoint.getByAirframe(airframes) as Collection<Aircraft>?)
            }.onStart { emit(null) },
            flow {
                emit(endpoint.getByCallsign(callsigns) as Collection<Aircraft>?)
            }.onStart { emit(null) },
            flow {
                emit(endpoint.getByRegistration(registrations) as Collection<Aircraft>?)
            }.onStart { emit(null) },
            flow {
                if (searchMilitary) emit(endpoint.getMilitary() as Collection<Aircraft>?) else emit(emptySet())
            }.onStart { emit(null) },
            flow {
                if (searchLadd) emit(endpoint.getLADD() as Collection<Aircraft>?) else emit(emptySet())
            }.onStart { emit(null) },
            flow {
                if (searchPia) emit(endpoint.getPIA() as Collection<Aircraft>?) else emit(emptySet())
            }.onStart { emit(null) },
            flow {
                if (location == null) emit(emptySet())
                else emit(endpoint.getByLocation(location, 500f) as Collection<Aircraft>?)
            }.onStart { emit(null) },
        ) { squawkAc, hexAc, airframeAc, callsignAc, registrationAc, militaryAc, laddAc, piaAc, locationAc ->
            val ac = mutableSetOf<Aircraft>()

            squawkAc?.let { ac.addAll(it) }
            hexAc?.let { ac.addAll(it) }
            airframeAc?.let { ac.addAll(it) }
            callsignAc?.let { ac.addAll(it) }
            registrationAc?.let { ac.addAll(it) }
            militaryAc?.let { ac.addAll(it) }
            laddAc?.let { ac.addAll(it) }
            piaAc?.let { ac.addAll(it) }
            locationAc?.let { ac.addAll(it) }

            Result(
                aircraft = ac,
                searching = listOf(
                    squawkAc,
                    hexAc,
                    airframeAc,
                    callsignAc,
                    registrationAc,
                    militaryAc,
                    laddAc,
                    piaAc,
                    locationAc,
                ).any { it == null }
            )
        }
            .onEach { result ->
                aircraftRepo.update(result.aircraft)
            }
            .catch {
                log(TAG, ERROR) { "liveSearch($query) failed:\n${it.asLog()}" }
                emit(Result(aircraft = emptySet(), searching = false, error = it))
            }
    }

    suspend fun search(query: SearchQuery): Result = liveSearch(query).last()

    companion object {
        private val TAG = logTag("Search", "Repo")
    }
}