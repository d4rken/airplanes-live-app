package eu.darken.apl.search.core

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
import eu.darken.apl.search.ui.SearchViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi,
) : PreferenceScreenData {

    private val Context.dataStore by preferencesDataStore(name = "settings_search")

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    val searchLocationDismissed = dataStore.createValue("search.location.dismissed", false)
    val inputLastRegistration = dataStore.createValue("search.lastinput.registration", "HO-HOHO")
    val inputLastHex = dataStore.createValue("search.lastinput.hex", "3C65A3")
    val inputLastCallsign = dataStore.createValue("search.lastinput.callsign", "DLH453")
    val inputLastAirframe = dataStore.createValue("search.lastinput.airframe", "A320")
    val inputLastSquawk = dataStore.createValue("search.lastinput.squawk", "7700,7600,7500")
    val inputLastInteresting = dataStore.createValue("search.lastinput.interesting", "military,ladd,pia")
    val inputLastPosition = dataStore.createValue("search.lastinput.position", "Frankfurt am Main, Germany")
    val inputLastAll = dataStore.createValue("search.lastinput.all", "")
    val inputLastMode = dataStore.createValue("search.lastmode", SearchViewModel.State.Mode.POSITION, moshi)


    override val mapper = PreferenceStoreMapper()

    companion object {
        internal val TAG = logTag("Search", "Settings")
    }
}