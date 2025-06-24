package eu.darken.apl.watch.core

// Import the specific createValue function from DataStoreValueJson
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
import javax.inject.Inject
import javax.inject.Singleton
import eu.darken.apl.common.datastore.createValue as createJsonValue

@Singleton
class WatchSettings @Inject constructor(
    @param:ApplicationContext private val context: Context,
    json: Json,
) : PreferenceScreenData {

    private val Context.dataStore by preferencesDataStore(name = "settings_alerts")

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    val watchMonitorInterval = dataStore.createJsonValue("watch.monitor.interval", DEFAULT_CHECK_INTERVAL, json)

    override val mapper = PreferenceStoreMapper()

    companion object {
        val DEFAULT_CHECK_INTERVAL = Duration.ofMinutes(60)
        internal val TAG = logTag("Watch", "Settings")
    }
}
