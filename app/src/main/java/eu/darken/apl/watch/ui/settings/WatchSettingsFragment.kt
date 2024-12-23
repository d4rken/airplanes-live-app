package eu.darken.apl.watch.ui.settings

import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.uix.PreferenceFragment2
import eu.darken.apl.databinding.ViewPreferenceSeekbarBinding
import eu.darken.apl.watch.core.WatchSettings
import java.time.Duration
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class WatchSettingsFragment : PreferenceFragment2() {

    private val vdc: WatchSettingsViewModel by viewModels()

    @Inject lateinit var watchSettings: WatchSettings

    override val settings: WatchSettings by lazy { watchSettings }
    override val preferenceFile: Int = R.xml.preferences_watch

    override fun onPreferencesCreated() {
        super.onPreferencesCreated()

        findPreference<Preference>(settings.watchMonitorInterval.keyName)?.apply {
            setOnPreferenceClickListener {
                val dialogLayout = ViewPreferenceSeekbarBinding.inflate(layoutInflater, null, false)
                dialogLayout.apply {
                    slider.valueFrom = 15f
                    slider.valueTo = 1440f
                    slider.value = settings.watchMonitorInterval.valueBlocking.toMinutes().toFloat()

                    val getSliderText = { value: Float ->
                        resources.getQuantityString(
                            R.plurals.watch_setting_check_interval_minutes,
                            value.toInt(),
                            value.toInt(),
                        )
                    }
                    slider.setLabelFormatter { getSliderText(it) }
                    sliderValue.text = getSliderText(slider.value)

                    slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                        override fun onStartTrackingTouch(slider: Slider) {
                            sliderValue.text = getSliderText(slider.value)
                        }

                        override fun onStopTrackingTouch(slider: Slider) {
                            sliderValue.text = getSliderText(slider.value)
                        }
                    })
                }
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.watch_settings_monitor_interval_title)
                    setView(dialogLayout.root)
                    setPositiveButton(R.string.common_save_action) { _, _ ->
                        settings.watchMonitorInterval.valueBlocking =
                            Duration.ofMinutes(dialogLayout.slider.value.toLong())
                    }
                    setNegativeButton(R.string.common_cancel_action) { _, _ -> }
                    setNeutralButton(R.string.common_reset_action) { _, _ ->
                        settings.watchMonitorInterval.valueBlocking = WatchSettings.DEFAULT_CHECK_INTERVAL
                    }
                }.show()
                true
            }
        }
    }
}