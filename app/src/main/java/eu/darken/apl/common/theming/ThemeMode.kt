package eu.darken.apl.common.theming

import eu.darken.apl.R
import eu.darken.apl.common.preferences.EnumPreference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode(override val labelRes: Int) : EnumPreference<ThemeMode> {
    @SerialName("SYSTEM") SYSTEM(R.string.ui_theme_mode_system_label),
    @SerialName("DARK") DARK(R.string.ui_theme_mode_dark_label),
    @SerialName("LIGHT") LIGHT(R.string.ui_theme_mode_light_label),
    ;
}
