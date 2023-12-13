package eu.darken.apl.common.github

import com.squareup.moshi.Moshi
import dagger.Reusable
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

@Reusable
class GithubReleaseCheck @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val baseHttpClient: OkHttpClient,
    private val baseMoshi: Moshi,
) {

    private val api: GithubApi by lazy {
        Retrofit.Builder().apply {
            baseUrl("https://api.github.com")
            client(baseHttpClient)
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(MoshiConverterFactory.create(baseMoshi).asLenient())
        }.build().create(GithubApi::class.java)
    }

    suspend fun latestRelease(owner: String, repo: String): GithubApi.ReleaseInfo = withContext(dispatcherProvider.IO) {
        log(TAG, VERBOSE) { "latestRelease(owner=$owner, repo=$repo)" }
        return@withContext api.latestRelease(owner, repo).also {
            log(TAG, INFO) { "latestRelease(owner=$owner, repo=$repo) is $it" }
        }
    }

    companion object {
        private val TAG = logTag("airplanes.live", "Endpoint")
    }
}