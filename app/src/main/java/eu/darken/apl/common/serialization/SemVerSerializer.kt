package eu.darken.apl.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.swiftzer.semver.SemVer

object SemVerSerializer : KSerializer<SemVer> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SemVer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SemVer) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): SemVer {
        return SemVer.parse(decoder.decodeString())
    }
}