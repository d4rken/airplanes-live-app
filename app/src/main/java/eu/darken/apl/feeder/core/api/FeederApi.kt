package eu.darken.apl.feeder.core.api

import retrofit2.http.GET
import retrofit2.http.Query

interface FeederApi {

    @GET("feeder")
    suspend fun getFeeder(@Query("id", encoded = true) ids: String): FeederInfos

}