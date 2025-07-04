package eu.darken.apl.feeder.core.api

import dagger.Reusable
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.feeder.core.ReceiverId
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Inject


@Reusable
class FeederEndpoint @Inject constructor(
    private val baseClient: OkHttpClient,
    private val jsonConverterFactory: Converter.Factory,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val api: FeederApi by lazy {
        val configHttpClient = baseClient.newBuilder().apply {

        }.build()

        Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl("https://api.airplanes.live/")
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create(FeederApi::class.java)
    }


    suspend fun getFeedInfos(ids: Set<ReceiverId>): Map<ReceiverId, FeedInfos> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getFeedInfos(ids=$ids)" }

        ids.associate {
            log(TAG, VERBOSE) { "Getting feeder data for $it" }
            it to api.getFeeder(it)
        }
    }

    suspend fun getFeedStatus(): FeedStatus = withContext(dispatcherProvider.IO) {
        log(TAG) { "getFeedStatus()" }
        api.getFeedStatus()
    }

    companion object {
        private val TAG = logTag("Feeder", "Endpoint")
    }
}
