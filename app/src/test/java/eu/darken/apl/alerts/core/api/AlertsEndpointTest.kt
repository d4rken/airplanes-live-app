package eu.darken.apl.alerts.core.api

import eu.darken.apl.common.http.HttpModule
import eu.darken.apl.common.serialization.SerializationModule
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.coroutine.TestDispatcherProvider

class AlertsEndpointTest : BaseTest() {
    private lateinit var endpoint: AlertsEndpoint

    @BeforeEach
    fun setup() {
        endpoint = AlertsEndpoint(
            baseClient = HttpModule().baseHttpClient(),
            dispatcherProvider = TestDispatcherProvider(),
            moshiConverterFactory = HttpModule().moshiConverter(SerializationModule().moshi())
        )
    }

    @Test
    fun `de-serialization of squawks`() = runTest {
        val squawks = endpoint.getSquawkAlerts(setOf("7700,7600,7500"))
        squawks shouldNotBe null
    }

    @Test
    fun `de-serialization of hexes`() = runTest {
        val hexes = endpoint.getHexAlerts(setOf(""))
        hexes shouldNotBe null
    }
}