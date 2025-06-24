package eu.darken.apl.feeder.core.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.datastore.PreferenceScreenData
import eu.darken.apl.common.datastore.PreferenceStoreMapper
import eu.darken.apl.common.debug.logging.logTag
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import eu.darken.apl.common.datastore.createValue as createJsonValue

@Singleton
class FeederSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    json: Json,
) : PreferenceScreenData {

    private val Context.dataStore by preferencesDataStore(name = "settings_feeder")

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    val feederGroup = dataStore.createJsonValue("feeder.group", FeederGroup(), json)

    val feederMonitorInterval = dataStore.createJsonValue("feeder.monitor.interval", DEFAULT_CHECK_INTERVAL, json)
    val lastUpdate = dataStore.createJsonValue("feeder.update.last", Instant.EPOCH, json)

    override val mapper = PreferenceStoreMapper()

    companion object {
        val DEFAULT_CHECK_INTERVAL = Duration.ofMinutes(60)
        internal val TAG = logTag("Feeder", "Settings")
    }
}
