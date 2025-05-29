package eu.darken.apl.main.ui.settings

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.BuildConfigWrap
import eu.darken.apl.common.PrivacyPolicy
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.datastore.PreferenceScreenData
import eu.darken.apl.common.uix.PreferenceFragment2
import eu.darken.apl.main.core.GeneralSettings
import eu.darken.apl.main.ui.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class SettingsIndexFragment : PreferenceFragment2() {

    @Inject lateinit var generalSettings: GeneralSettings
    override val settings: PreferenceScreenData
        get() = generalSettings
    override val preferenceFile: Int = R.xml.preferences_index

    @Inject lateinit var webpageTool: WebpageTool

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(R.menu.menu_settings_index) { item ->
            when (item.itemId) {
                else -> {}
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPreferencesCreated() {
        findPreference<Preference>("core.changelog")!!.summary = BuildConfigWrap.VERSION_DESCRIPTION
        findPreference<Preference>("core.privacy")!!.setOnPreferenceClickListener {
            webpageTool.open(PrivacyPolicy.URL)
            true
        }
        findPreference<Preference>("core.sponsor")!!.setOnPreferenceClickListener {
            (requireActivity() as MainActivity).goSponsor()
            true
        }
        super.onPreferencesCreated()
    }
}