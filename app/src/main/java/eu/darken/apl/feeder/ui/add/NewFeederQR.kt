package eu.darken.apl.feeder.ui.add

import android.net.Uri
import eu.darken.apl.feeder.core.ReceiverId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NewFeederQR(
    @SerialName("receiverId") val receiverId: ReceiverId,
    @SerialName("receiverLabel") val receiverLabel: String?,
    @SerialName("receiverIpv4Address") val receiverIpv4Address: String?,
) {
    fun toUri(): Uri {
        val jsonData = Json.encodeToString(this)
        return Uri.parse("$PREFIX?data=$jsonData")
    }

    companion object {
        const val PREFIX = "eu_darken_apl://feeder?"
    }
}
