package eu.darken.apl.common.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID

class UUIDAdapter {
    @ToJson
    fun toJson(value: UUID) = value.toString()

    @FromJson
    fun fromJson(raw: String) = UUID.fromString(raw)
}
