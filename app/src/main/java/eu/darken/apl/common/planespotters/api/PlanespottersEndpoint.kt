package eu.darken.apl.common.planespotters.api

import dagger.Reusable
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.WARN
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Registration
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject


@Reusable
class PlanespottersEndpoint @Inject constructor(
    private val baseClient: OkHttpClient,
    private val moshiConverterFactory: MoshiConverterFactory,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val api: PlanespottersApi by lazy {
        val configHttpClient = baseClient.newBuilder().apply {

        }.build()

        Retrofit.Builder()
            .client(configHttpClient)
            .baseUrl("https://api.planespotters.net/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(PlanespottersApi::class.java)
    }

    suspend fun getPhotosByHex(
        hex: AircraftHex,
    ): Collection<PlanespottersApi.Photo> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getPhotosByHex(hex=$hex)" }
        try {
            api.getPhotosByHex(hex).photos
        } catch (e: HttpException) {
            log(TAG, WARN) { "getPhotosByHex($hex): ${e.asLog()}" }
            throw PlanespottersException(hex, e)
        }
    }

    suspend fun getPhotosByRegistration(
        registration: Registration,
    ): Collection<PlanespottersApi.Photo> = withContext(dispatcherProvider.IO) {
        log(TAG) { "getPhotosByRegistration(registration=$registration)" }
        try {
            api.getPhotosByRegistration(registration).photos
        } catch (e: HttpException) {
            log(TAG, WARN) { "getPhotosByRegistration($registration): ${e.asLog()}" }
            throw PlanespottersException(registration, e)
        }
    }

    companion object {
        private val TAG = logTag("Planespotters", "Endpoint")
    }
}