package eu.darken.apl.watch.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.datastore.PreferenceScreenData
import eu.darken.apl.common.datastore.PreferenceStoreMapper
import eu.darken.apl.common.datastore.createValue
import eu.darken.apl.common.debug.logging.logTag
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi,
) : PreferenceScreenData {

    private val Context.dataStore by preferencesDataStore(name = "settings_alerts")

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    val watchMonitorInterval = dataStore.createValue("watch.monitor.interval", Duration.ofMinutes(15), moshi)

    override val mapper = PreferenceStoreMapper()

    companion object {
        internal val TAG = logTag("Watch", "Settings")
    }
}