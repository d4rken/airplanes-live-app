package eu.darken.apl.main.core

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.datastore.PreferenceScreenData
import eu.darken.apl.common.datastore.PreferenceStoreMapper
import eu.darken.apl.common.datastore.createValue
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.theming.ThemeMode
import eu.darken.apl.common.theming.ThemeStyle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi,
) : PreferenceScreenData {

    private val Context.dataStore by preferencesDataStore(name = "settings_core")

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    val deviceLabel = dataStore.createValue("core.device.label", Build.DEVICE)

    val isAutoReportingEnabled = dataStore.createValue("debug.bugreport.automatic.enabled", true)
    val isUpdateCheckEnabled = dataStore.createValue("updater.check.enabled", false)

    val isOnboardingFinished = dataStore.createValue("core.onboarding.finished", false)

    val themeMode = dataStore.createValue("core.ui.theme.mode", ThemeMode.SYSTEM, moshi)
    val themeStyle = dataStore.createValue("core.ui.theme.style", ThemeStyle.DEFAULT, moshi)

    val searchLocationDismissed = dataStore.createValue("search.location.dismissed", false)

    override val mapper = PreferenceStoreMapper(
        isAutoReportingEnabled,
        deviceLabel,
        themeMode,
        themeStyle,
        isUpdateCheckEnabled,
    )

    companion object {
        internal val TAG = logTag("Core", "Settings")
    }
}