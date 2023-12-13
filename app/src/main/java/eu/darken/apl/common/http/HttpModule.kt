package eu.darken.apl.common.http

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.debug.autoreport.DebugSettings
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HttpModule {

    @Reusable
    @Provides
    fun loggingInterceptor(
        debugSettings: DebugSettings? = null,
    ): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor.Logger {
            log(TAG, VERBOSE) { it }
        }
        return HttpLoggingInterceptor(logger).apply {
            level = if (debugSettings?.isDebugMode?.valueBlocking != false) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    @Singleton
    @Provides
    fun baseHttpClient(
        @BaseCache cache: Cache? = null,
        loggingInterceptor: HttpLoggingInterceptor = loggingInterceptor(),
    ): OkHttpClient = OkHttpClient().newBuilder().apply {
        cache(cache)
        connectTimeout(20L, TimeUnit.SECONDS)
        readTimeout(20L, TimeUnit.SECONDS)
        writeTimeout(20L, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)
        addInterceptor(loggingInterceptor)
    }.build()

    @BaseCache
    @Provides
    @Singleton
    fun baseHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_base_cache")
        return Cache(cacheDir, 1024L * 1024L * 20) // 20 MB
    }

    @Reusable
    @Provides
    fun moshiConverter(moshi: Moshi): MoshiConverterFactory = MoshiConverterFactory.create(moshi)

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class BaseCache

    companion object {
        private val TAG = logTag("Http")
    }
}