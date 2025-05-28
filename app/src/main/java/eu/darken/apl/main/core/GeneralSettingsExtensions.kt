package eu.darken.apl.main.core

import eu.darken.apl.common.theming.ThemeMode
import eu.darken.apl.common.theming.ThemeStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ThemeState(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val style: ThemeStyle = ThemeStyle.DEFAULT,
)

val GeneralSettings.themeState: Flow<ThemeState>
    get() = combine(
        this.themeMode.flow,
        themeStyle.flow
    ) { mode, style ->
        ThemeState(mode, style)
    }