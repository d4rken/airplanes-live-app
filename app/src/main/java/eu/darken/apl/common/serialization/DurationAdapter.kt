package eu.darken.apl.common.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Duration

class DurationAdapter {
    @ToJson
    fun toJson(value: Duration) = value.toString()

    @FromJson
    fun fromJson(raw: String) = Duration.parse(raw)
}
