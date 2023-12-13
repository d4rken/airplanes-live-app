package eu.darken.apl.common.theming

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.R
import eu.darken.apl.common.preferences.EnumPreference

@JsonClass(generateAdapter = false)
enum class ThemeMode(override val labelRes: Int) : EnumPreference<ThemeMode> {
    @Json(name = "SYSTEM") SYSTEM(R.string.ui_theme_mode_system_label),
    @Json(name = "DARK") DARK(R.string.ui_theme_mode_dark_label),
    @Json(name = "LIGHT") LIGHT(R.string.ui_theme_mode_light_label),
    ;
}

