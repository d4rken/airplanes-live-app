package eu.darken.apl.alerts.core.api

import dagger.Reusable
import eu.darken.apl.alerts.core.AircraftHex
import eu.darken.apl.alerts.core.SquawkCode
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.stream.Collectors
import javax.inject.Inject


@Reusable
class AlertsEndpoint @Inject constructor(
    private val baseClient: OkHttpClient,
    private val moshiConverterFactory: MoshiConverterFactory,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val api: AlertsApi by lazy {
        val configHttpClient = baseClient.newBuilder().apply {

        }.build()

        Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl("https://api.airplanes.live/v1/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(AlertsApi::class.java)
    }


    suspend fun getHexAlerts(
        hexes: Set<AircraftHex>,
    ): AlertsApi.Alerts = withContext(dispatcherProvider.IO) {
        log(TAG) { "getAlerts(hexes=$hexes)" }

        hexes
            .chunked(30)
            .map { hexesChunk ->
                hexesChunk.stream()
                    .map { it.toString() }
                    .collect(Collectors.joining(","))
            }
            .map { api.getHexAlerts(it) }
            .toList()
            .let { alerts ->
                AlertsApi.Alerts(
                    hexes = alerts.flatMap { it.hexes },
                    squawks = emptyList()
                )
            }
    }

    suspend fun getSquawkAlerts(
        squawks: Set<SquawkCode>
    ): AlertsApi.Alerts = withContext(dispatcherProvider.IO) {
        log(TAG) { "getAlerts(squawks=$squawks)" }

        squawks
            .chunked(30)
            .map { hexesChunk ->
                hexesChunk.stream()
                    .map { it.toString() }
                    .collect(Collectors.joining(","))
            }
            .map { api.getSquawkAlerts(it) }
            .toList()
            .let { alerts ->
                AlertsApi.Alerts(
                    hexes = alerts.flatMap { it.hexes },
                )
            }
    }

    companion object {
        private val TAG = logTag("Feeder", "Endpoint")
    }
}