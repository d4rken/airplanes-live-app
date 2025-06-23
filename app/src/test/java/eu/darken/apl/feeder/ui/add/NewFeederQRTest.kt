package eu.darken.apl.feeder.ui.add

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NewFeederQRTest {

    @Test
    fun `toUri generates correct URI scheme and format`() {
        val feederQR = NewFeederQR(
            receiverId = "test123",
            receiverLabel = "Test Feeder"
        )

        val result = feederQR.toUri()

        result.toString() shouldContain "eu_darken_apl://feeder?data="
        result.toString() shouldContain "receiverId"
        result.toString() shouldContain "test123"
        result.toString() shouldContain "receiverLabel"
        result.toString() shouldContain "Test Feeder"
    }

    @Test
    fun `toUri handles null receiverLabel correctly`() {
        val feederQR = NewFeederQR(
            receiverId = "test456",
            receiverLabel = null
        )

        val result = feederQR.toUri()

        result.toString() shouldContain "eu_darken_apl://feeder?data="
        result.toString() shouldContain "receiverId"
        result.toString() shouldContain "test456"
        result.toString() shouldContain "receiverLabel"
        result.toString() shouldContain "null"
    }

    @Test
    fun `JSON serialization produces expected format`() {
        val feederQR = NewFeederQR(
            receiverId = "receiver789",
            receiverLabel = "My Custom Feeder"
        )

        val jsonString = Json.encodeToString(feederQR)

        jsonString shouldBe "{\"receiverId\":\"receiver789\",\"receiverLabel\":\"My Custom Feeder\"}"
    }

    @Test
    fun `JSON serialization handles null receiverLabel`() {
        val feederQR = NewFeederQR(
            receiverId = "receiver456",
            receiverLabel = null
        )

        val jsonString = Json.encodeToString(feederQR)

        jsonString shouldBe "{\"receiverId\":\"receiver456\",\"receiverLabel\":null}"
    }

    @Test
    fun `URI contains properly encoded JSON data`() {
        val feederQR = NewFeederQR(
            receiverId = "special-chars_123",
            receiverLabel = "Feeder with spaces"
        )

        val result = feederQR.toUri()
        val uriString = result.toString()

        uriString shouldContain "eu_darken_apl://feeder?data="

        // Extract the data parameter
        val dataStart = uriString.indexOf("data=") + 5
        val jsonData = uriString.substring(dataStart)

        // Verify it's valid JSON that can be parsed back
        val parsedQR = Json.decodeFromString<NewFeederQR>(jsonData)
        parsedQR.receiverId shouldBe "special-chars_123"
        parsedQR.receiverLabel shouldBe "Feeder with spaces"
    }

    @Test
    fun `toUri creates exact expected URI format`() {
        val feederQR = NewFeederQR(
            receiverId = "test-receiver",
            receiverLabel = "Test Label"
        )

        val result = feederQR.toUri()

        result.toString() shouldBe "eu_darken_apl://feeder?data={\"receiverId\":\"test-receiver\",\"receiverLabel\":\"Test Label\"}"
    }

    @Test
    fun `URI scheme is correct`() {
        val feederQR = NewFeederQR(
            receiverId = "test",
            receiverLabel = "label"
        )

        val result = feederQR.toUri()

        result.scheme shouldBe "eu_darken_apl"
        result.host shouldBe "feeder"
        result.getQueryParameter("data") shouldContain "receiverId"
    }

    @Test
    fun `URI can be parsed back to original data`() {
        val originalQR = NewFeederQR(
            receiverId = "roundtrip-test",
            receiverLabel = "Round Trip Label"
        )

        val uri = originalQR.toUri()
        val jsonData = uri.getQueryParameter("data")!!
        val parsedQR = Json.decodeFromString<NewFeederQR>(jsonData)

        parsedQR.receiverId shouldBe originalQR.receiverId
        parsedQR.receiverLabel shouldBe originalQR.receiverLabel
    }
}