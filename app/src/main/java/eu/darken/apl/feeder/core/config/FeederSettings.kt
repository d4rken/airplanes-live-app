package eu.darken.apl.feeder.core.config

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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeederSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi,
) : PreferenceScreenData {

    private val Context.dataStore by preferencesDataStore(name = "settings_feeder")

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    val feederGroup = dataStore.createValue("feeder.group", FeederGroup(), moshi)

    override val mapper = PreferenceStoreMapper(
    )

    companion object {
        internal val TAG = logTag("Feeder", "Settings")
    }
}