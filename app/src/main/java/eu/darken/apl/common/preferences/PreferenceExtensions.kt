package eu.darken.apl.common.preferences

import androidx.preference.ListPreference
import eu.darken.apl.common.datastore.DataStoreValue
import eu.darken.apl.common.datastore.valueBlocking


inline fun <reified T> ListPreference.setupWithEnum(preference: DataStoreValue<T>) where  T : Enum<T>, T : EnumPreference<T> {
    isPersistent = false

    val startValue = preference.valueBlocking

    entries = enumValues<T>().map { context.getString(it.labelRes) }.toTypedArray()
    entryValues = enumValues<T>().map { it.name }.toTypedArray()
    value = (preference.writer(startValue) as String).removeSurrounding("\"")

    setOnPreferenceChangeListener { _, newValueRaw ->
        val newValue = preference.reader("\"${(newValueRaw)}\"")
        preference.valueBlocking = newValue
        true
    }
}