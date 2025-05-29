package eu.darken.apl.common.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

class InstantAdapter {
    @ToJson
    fun toJson(value: Instant) = value.toString()

    @FromJson
    fun fromJson(raw: String) = Instant.parse(raw)
}
