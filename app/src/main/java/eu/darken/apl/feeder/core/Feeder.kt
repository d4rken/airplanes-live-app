package eu.darken.apl.feeder.core

import eu.darken.apl.feeder.core.config.FeederConfig
import eu.darken.apl.feeder.core.stats.BeastStatsEntity
import eu.darken.apl.feeder.core.stats.MlatStatsEntity
import java.time.Duration
import java.time.Instant

data class Feeder(
    val config: FeederConfig,
    val beastStats: BeastStatsEntity? = null,
    val mlatStats: MlatStatsEntity? = null,
) {

    val label: String
        get() = config.label ?: config.receiverId.takeLast(5)

    val isOffline: Boolean
        get() = if (config.offlineCheckTimeout != null) {
            Duration.between(lastSeen, Instant.now()) > config.offlineCheckTimeout
        } else {
            false
        }

    val lastSeen: Instant?
        get() = listOfNotNull(beastStats?.receivedAt, mlatStats?.receivedAt).maxOrNull()

    val id: ReceiverId
        get() = config.receiverId

    val anywhereId: String
        get() = id.split("-").take(3).joinToString("")

}
