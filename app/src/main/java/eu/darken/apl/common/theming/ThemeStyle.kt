package eu.darken.apl.common.theming

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.darken.apl.R
import eu.darken.apl.common.preferences.EnumPreference

@JsonClass(generateAdapter = false)
enum class ThemeStyle(override val labelRes: Int) : EnumPreference<ThemeStyle> {
    @Json(name = "DEFAULT") DEFAULT(R.string.ui_theme_style_default_label),
    @Json(name = "MATERIAL_YOU") MATERIAL_YOU(R.string.ui_theme_style_materialyou_label),
    ;
}