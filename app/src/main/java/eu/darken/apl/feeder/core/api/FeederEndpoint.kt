package eu.darken.apl.feeder.core.api

import dagger.Reusable
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.feeder.core.ReceiverId
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.stream.Collectors
import javax.inject.Inject


@Reusable
class FeederEndpoint @Inject constructor(
    private val baseClient: OkHttpClient,
    private val moshiConverterFactory: MoshiConverterFactory,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val api: FeederApi by lazy {
        val configHttpClient = baseClient.newBuilder().apply {

        }.build()

        Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl("https://api.airplanes.live/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(FeederApi::class.java)
    }


    suspend fun getFeeder(ids: Set<ReceiverId>): FeedInfos = withContext(dispatcherProvider.IO) {
        log(TAG) { "getFeeder(ids=$ids)" }

        ids
            .chunked(25)
            .map { it.stream().map { it.toString() }.collect(Collectors.joining(",")) }
            .map { api.getFeeder(it) }
            .toList()
            .let { infos ->
                val beasts = infos.flatMap { it.beast }
                val mlats = infos.flatMap { it.mlat }
                FeedInfos(
                    beast = beasts,
                    mlat = mlats,
//                    anywhereLink = anywheres
//                        .flatMap { it.removePrefix("https://globe.airplanes.live/?feed=").split(",") }
//                        .toList()
//                        .let { "https://globe.airplanes.live/?feed=${it.joinToString(",")}" }
                )
            }
    }

    companion object {
        private val TAG = logTag("Feeder", "Endpoint")
    }
}