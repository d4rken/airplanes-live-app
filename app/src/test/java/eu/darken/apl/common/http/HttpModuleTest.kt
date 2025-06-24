package eu.darken.apl.common.http

import eu.darken.apl.common.BuildConfigWrap
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import java.util.concurrent.TimeUnit

class HttpModuleTest : BaseTest() {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpModule: HttpModule

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        httpModule = HttpModule()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `baseHttpClient sets correct user-agent header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = okhttp3.Request.Builder()
            .url(mockWebServer.url("/").toString())
            .build()
        httpModule.baseHttpClient().newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        val expectedUserAgent =
            "${BuildConfigWrap.APPLICATION_ID}/${BuildConfigWrap.VERSION_NAME} (Android null; null)"
        recordedRequest?.getHeader("User-Agent") shouldBe expectedUserAgent
    }
}