package eu.darken.apl.feeder.ui.add

import android.net.Uri
import eu.darken.apl.feeder.core.ReceiverId
import eu.darken.apl.feeder.core.config.FeederPosition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NewFeederQR(
    @SerialName("receiverId") val receiverId: ReceiverId,
    @SerialName("receiverLabel") val receiverLabel: String? = null,
    @SerialName("receiverIpv4Address") val receiverIpv4Address: String? = null,
    @SerialName("position") val position: FeederPosition? = null,
) {
    fun toUri(json: Json): Uri {
        val jsonData = json.encodeToString(this)
        return Uri.parse("$PREFIX?data=$jsonData")
    }

    companion object {
        const val PREFIX = "eu_darken_apl://feeder"

        fun isValid(url: String): Boolean = url.startsWith(PREFIX)

        fun fromUri(uri: Uri, json: Json): NewFeederQR? {
            val raw = uri.toString()
            if (!raw.startsWith(PREFIX)) return null
            val jsonData = raw.substringAfter("data=")
            return json.decodeFromString<NewFeederQR>(jsonData)
        }
    }
}
