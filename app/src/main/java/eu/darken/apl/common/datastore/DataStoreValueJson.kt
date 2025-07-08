package eu.darken.apl.common.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

inline fun <reified T> jsonReader(
    json: Json,
    defaultValue: T,
): (Any?) -> T {
    val serializer = json.serializersModule.serializer<T>()
    return { rawValue ->
        rawValue as String?
        rawValue?.let { json.decodeFromString(serializer, it) } ?: defaultValue
    }
}

inline fun <reified T> jsonWriter(
    json: Json,
): (T) -> Any? {
    val serializer = json.serializersModule.serializer<T>()
    return { newValue: T ->
        newValue?.let { json.encodeToString(serializer, it) }
    }
}

inline fun <reified T : Any?> DataStore<Preferences>.createValue(
    key: String,
    defaultValue: T = null as T,
    json: Json,
) = DataStoreValue(
    dataStore = this,
    key = stringPreferencesKey(key),
    reader = jsonReader(json, defaultValue),
    writer = jsonWriter(json),
)