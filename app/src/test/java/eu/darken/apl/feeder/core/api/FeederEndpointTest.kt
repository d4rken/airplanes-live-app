package eu.darken.apl.feeder.core.api

import eu.darken.apl.common.http.HttpModule
import eu.darken.apl.common.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.coroutine.TestDispatcherProvider

class FeederEndpointTest : BaseTest() {
    private lateinit var endpoint: FeederEndpoint

    @BeforeEach
    fun setup() {
        endpoint = FeederEndpoint(
            baseClient = HttpModule().baseHttpClient(),
            dispatcherProvider = TestDispatcherProvider(),
            moshiConverterFactory = HttpModule().moshiConverter(SerializationModule().moshi())
        )
    }

    @Test
    fun `de-serialization`() = runTest {
        val infos = endpoint.getFeedInfos(setOf("04e5c4d5-2068-4759-b1a5-8398e8f60f66"))
        infos.apply {
            beast shouldNotBe emptyList<FeedInfos.Beast>()
            mlat shouldNotBe emptyList<FeedInfos.Mlat>()
            mlat.first().apply {
                user shouldBe "fremont"
            }
        }
    }
}