package eu.darken.apl.main.core.api

import dagger.Reusable
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Airframe
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.Registration
import eu.darken.apl.main.core.aircraft.SquawkCode
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.stream.Collectors
import javax.inject.Inject


@Reusable
class AplEndpoint @Inject constructor(
    private val baseClient: OkHttpClient,
    private val moshiConverterFactory: MoshiConverterFactory,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val api: AplApi by lazy {
        val configHttpClient = baseClient.newBuilder().apply {

        }.build()

        Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl("https://api.airplanes.live/v2/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(AplApi::class.java)
    }

    suspend fun getByHex(
        hexes: Set<AircraftHex>,
    ): Collection<AplApi.Aircraft> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getByHexes(hexes=$hexes)" }
        if (hexes.isEmpty()) return@withContext emptySet()
        hexes
            .chunkToCommaArgs()
            .map { api.getAircraftByHex(it).throwForErrors() }
            .toList()
            .let { responses ->
                responses.flatMap { it.ac }
            }
    }

    suspend fun getBySquawk(
        squawks: Set<SquawkCode>
    ): Collection<AplApi.Aircraft> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getBySquawks(squawks=$squawks)" }
        if (squawks.isEmpty()) return@withContext emptySet()
        squawks
            .chunkToCommaArgs()
            .map { api.getAircraftBySquawk(it).throwForErrors() }
            .toList()
            .let { responses ->
                responses.flatMap { it.ac }
            }
    }

    suspend fun getByCallsign(
        callsigns: Set<Callsign>
    ): Collection<AplApi.Aircraft> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getByCallsigns(callsigns=$callsigns)" }
        if (callsigns.isEmpty()) return@withContext emptySet()
        callsigns
            .chunkToCommaArgs()
            .map { api.getAircraftByCallsign(it).throwForErrors() }
            .toList()
            .let { responses ->
                responses.flatMap { it.ac }
            }
    }

    suspend fun getByRegistration(
        registrations: Set<Registration>
    ): Collection<AplApi.Aircraft> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getByRegistration(registrations=$registrations)" }
        if (registrations.isEmpty()) return@withContext emptySet()
        registrations
            .chunkToCommaArgs()
            .map { api.getAircraftByRegistration(it).throwForErrors() }
            .toList()
            .let { responses ->
                responses.flatMap { it.ac }
            }
    }

    suspend fun getByAirframe(
        airframes: Set<Airframe>
    ): Collection<AplApi.Aircraft> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getByAirframe(squawks=$airframes)" }
        if (airframes.isEmpty()) return@withContext emptySet()
        airframes
            .chunkToCommaArgs()
            .map { api.getAircraftByAirframe(it).throwForErrors() }
            .toList()
            .let { responses ->
                responses.flatMap { it.ac }
            }
    }

    private fun <T : AplApi.BaseResponse> T.throwForErrors(): T = this.also {
        if (it.message != "No error") throw AplApiError(it.message)
    }

    private fun Collection<String>.chunkToCommaArgs(size: Int = 30) = this
        .chunked(size)
        .map { chunk ->
            chunk.stream()
                .map { it.toString() }
                .collect(Collectors.joining(","))
        }

    companion object {
        private val TAG = logTag("Core", "Endpoint")
    }
}