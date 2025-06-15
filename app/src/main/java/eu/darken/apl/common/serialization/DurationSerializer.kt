package eu.darken.apl.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Duration

object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JavaDuration", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Duration {
        return Duration.parse(decoder.decodeString())
    }
}