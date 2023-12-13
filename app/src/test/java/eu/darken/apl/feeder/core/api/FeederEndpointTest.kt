package eu.darken.apl.feeder.core.api

import eu.darken.apl.common.http.HttpModule
import eu.darken.apl.common.serialization.SerializationModule
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
        val alerts = endpoint.getFeeder(setOf("7ff8b692-176c-4676-99d4-a3c22563a72e"))
        alerts shouldNotBe null
    }
}