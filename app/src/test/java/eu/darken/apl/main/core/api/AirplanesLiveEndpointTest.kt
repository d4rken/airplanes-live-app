package eu.darken.apl.main.core.api

import eu.darken.apl.common.http.HttpModule
import eu.darken.apl.common.serialization.SerializationModule
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.coroutine.TestDispatcherProvider

class AirplanesLiveEndpointTest : BaseTest() {
    private lateinit var endpoint: AirplanesLiveEndpoint

    @BeforeEach
    fun setup() {
        endpoint = AirplanesLiveEndpoint(
            baseClient = HttpModule().baseHttpClient(),
            dispatcherProvider = TestDispatcherProvider(),
            moshiConverterFactory = HttpModule().moshiConverter(SerializationModule().moshi())
        )
    }

    @Test
    fun `aircraft by squawks`() = runTest {
        endpoint.getBySquawk(setOf("3532,1200,0420")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by hexes `() = runTest {
        endpoint.getByHex(setOf("A213BD,A4FBAC")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by callsigns`() = runTest {
        endpoint.getByCallsign(setOf("AAL1002,AAL1328")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by registration`() = runTest {
        endpoint.getByRegistration(setOf("N656NK")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by airframe`() = runTest {
        endpoint.getByAirframe(setOf("F16")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by location`() = runTest {
        endpoint.getByLocation(51.473419, -0.491683, 100).apply {
            this shouldNotBe null
        }
    }
}