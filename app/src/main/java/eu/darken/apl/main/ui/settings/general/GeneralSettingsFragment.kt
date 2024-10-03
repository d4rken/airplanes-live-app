package eu.darken.apl.main.ui.settings.general

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.BuildConfigWrap
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.preferences.ListPreference2
import eu.darken.apl.common.preferences.setupWithEnum
import eu.darken.apl.common.uix.PreferenceFragment2
import eu.darken.apl.main.core.GeneralSettings
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class GeneralSettingsFragment : PreferenceFragment2() {

    private val vdc: GeneralSettingsViewModel by viewModels()

    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }
    override val preferenceFile: Int = R.xml.preferences_general

    private val themeModePref: ListPreference2
        get() = findPreference(settings.themeMode.keyName)!!
    private val themeStylePref: ListPreference2
        get() = findPreference(settings.themeStyle.keyName)!!
    private val updateCheck: CheckBoxPreference
        get() = findPreference(settings.isUpdateCheckEnabled.keyName)!!

    override fun onPreferencesCreated() {
        themeModePref.setupWithEnum(settings.themeMode)
        themeStylePref.setupWithEnum(settings.themeStyle)

        super.onPreferencesCreated()
    }

    override fun onPreferencesChanged() {
        findPreference<Preference>(settings.deviceLabel.keyName)?.summary = settings.deviceLabel.valueBlocking
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateCheck.isVisible = BuildConfigWrap.FLAVOR == BuildConfigWrap.Flavor.FOSS
    }

}