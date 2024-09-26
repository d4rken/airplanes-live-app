package eu.darken.apl.main.core.api

import eu.darken.apl.common.http.HttpModule
import eu.darken.apl.common.serialization.SerializationModule
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.coroutine.TestDispatcherProvider

class AplEndpointTest : BaseTest() {
    private lateinit var endpoint: AplEndpoint

    @BeforeEach
    fun setup() {
        endpoint = AplEndpoint(
            baseClient = HttpModule().baseHttpClient(),
            dispatcherProvider = TestDispatcherProvider(),
            moshiConverterFactory = HttpModule().moshiConverter(SerializationModule().moshi())
        )
    }

    @Test
    fun `aircraft by squawks`() = runTest {
        endpoint.getBySquawks(setOf("3532,1200,0420")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by hexes `() = runTest {
        endpoint.getByHexes(setOf("A213BD,A4FBAC")).apply {
            this shouldNotBe null
        }
    }

    @Test
    fun `aircraft by callsigns`() = runTest {
        endpoint.getByCallsigns(setOf("AAL1002,AAL1328")).apply {
            this shouldNotBe null
        }
    }
}