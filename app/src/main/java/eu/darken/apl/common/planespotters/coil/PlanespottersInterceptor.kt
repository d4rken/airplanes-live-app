package eu.darken.apl.common.planespotters.coil

import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.ImageResult
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.aircraft.Aircraft
import kotlinx.coroutines.delay
import retrofit2.HttpException

class PlanespottersInterceptor : Interceptor {

    private val APIS = setOf(
        "plnspttrs.net",
        "planespotters.net",
    )

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request

        val isPlanespotters = request.data is Aircraft || APIS.any { request.data.toString().contains(it) }
        if (!isPlanespotters) return chain.proceed(request)

        val result = chain.proceed(request)

        if (((result as? ErrorResult)?.throwable as? HttpException)?.code() == 429) {
            val response = (result.throwable as HttpException).response()!!
            val retryAfter = response.headers()["Retry-After"]?.toIntOrNull() ?: (5..10).random()
            log(TAG, VERBOSE) { "Rate-limit hit, delaying request!" }
            delay(retryAfter * 1000L)
            return chain.proceed(request)
        }

        return result
    }

    companion object {
        private val TAG = logTag("Planerspotters", "Interceptor")
    }
}