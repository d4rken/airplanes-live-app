package eu.darken.apl.feeder.core.config

import eu.darken.apl.feeder.core.ReceiverId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import java.time.Duration

class FeederConfigTest : BaseTest() {

    @Test
    fun `FeederConfig has offline notifications enabled by default with 48 hour timeout`() {
        val receiverId: ReceiverId = "test-receiver-id"
        val config = FeederConfig(receiverId = receiverId)

        config.offlineCheckTimeout shouldBe Duration.ofHours(48)
    }

    @Test
    fun `FeederConfig can override default offline timeout`() {
        val receiverId: ReceiverId = "test-receiver-id"
        val customTimeout = Duration.ofHours(6)
        val config = FeederConfig(
            receiverId = receiverId,
            offlineCheckTimeout = customTimeout
        )

        config.offlineCheckTimeout shouldBe customTimeout
    }

    @Test
    fun `FeederConfig can disable offline notifications`() {
        val receiverId: ReceiverId = "test-receiver-id"
        val config = FeederConfig(
            receiverId = receiverId,
            offlineCheckTimeout = null
        )

        config.offlineCheckTimeout shouldBe null
    }
}