package eu.darken.apl.common.github

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Reusable
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

@Reusable
class GithubReleaseCheck @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val baseHttpClient: OkHttpClient,
    private val baseJson: Json,
) {

    private val api: GithubApi by lazy {
        Retrofit.Builder().apply {
            baseUrl("https://api.github.com")
            client(baseHttpClient)
            addConverterFactory(ScalarsConverterFactory.create())
            val contentType = "application/json".toMediaType()
            addConverterFactory(baseJson.asConverterFactory(contentType))
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
