package eu.darken.apl.common.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import eu.darken.apl.R
import eu.darken.apl.common.hasApiLevel
import eu.darken.apl.common.theming.ThemeMode
import eu.darken.apl.common.theming.ThemeStyle
import eu.darken.apl.main.core.ThemeState

@Composable
fun myLightColorScheme(): ColorScheme = lightColorScheme(
    primary = colorResource(id = R.color.md_theme_primary),
    onPrimary = colorResource(id = R.color.md_theme_onPrimary),
    primaryContainer = colorResource(id = R.color.md_theme_primaryContainer),
    onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer),

    secondary = colorResource(id = R.color.md_theme_secondary),
    onSecondary = colorResource(id = R.color.md_theme_onSecondary),
    secondaryContainer = colorResource(id = R.color.md_theme_secondaryContainer),
    onSecondaryContainer = colorResource(id = R.color.md_theme_onSecondaryContainer),

    tertiary = colorResource(id = R.color.md_theme_tertiary),
    onTertiary = colorResource(id = R.color.md_theme_onTertiary),
    tertiaryContainer = colorResource(id = R.color.md_theme_tertiaryContainer),
    onTertiaryContainer = colorResource(id = R.color.md_theme_onTertiaryContainer),

    error = colorResource(id = R.color.md_theme_error),
    onError = colorResource(id = R.color.md_theme_onError),
    errorContainer = colorResource(id = R.color.md_theme_errorContainer),
    onErrorContainer = colorResource(id = R.color.md_theme_onErrorContainer),

    background = colorResource(id = R.color.md_theme_background),
    onBackground = colorResource(id = R.color.md_theme_onBackground),

    surface = colorResource(id = R.color.md_theme_surface),
    onSurface = colorResource(id = R.color.md_theme_onSurface),
    surfaceVariant = colorResource(id = R.color.md_theme_surfaceVariant),
    onSurfaceVariant = colorResource(id = R.color.md_theme_onSurfaceVariant),

    outline = colorResource(id = R.color.md_theme_outline),
    outlineVariant = colorResource(id = R.color.md_theme_outlineVariant),

    inverseSurface = colorResource(id = R.color.md_theme_inverseSurface),
    inverseOnSurface = colorResource(id = R.color.md_theme_inverseOnSurface),

    surfaceDim = colorResource(id = R.color.md_theme_surfaceDim),
    surfaceBright = colorResource(id = R.color.md_theme_surfaceBright),
    surfaceContainerLowest = colorResource(id = R.color.md_theme_surfaceContainerLowest),
    surfaceContainerLow = colorResource(id = R.color.md_theme_surfaceContainerLow),
    surfaceContainer = colorResource(id = R.color.md_theme_surfaceContainer),
    surfaceContainerHigh = colorResource(id = R.color.md_theme_surfaceContainerHigh),
    surfaceContainerHighest = colorResource(id = R.color.md_theme_surfaceContainerHighest)
)

@Composable
fun myDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = colorResource(id = R.color.md_theme_primary),
    onPrimary = colorResource(id = R.color.md_theme_onPrimary),
    primaryContainer = colorResource(id = R.color.md_theme_primaryContainer),
    onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer),

    secondary = colorResource(id = R.color.md_theme_secondary),
    onSecondary = colorResource(id = R.color.md_theme_onSecondary),
    secondaryContainer = colorResource(id = R.color.md_theme_secondaryContainer),
    onSecondaryContainer = colorResource(id = R.color.md_theme_onSecondaryContainer),

    tertiary = colorResource(id = R.color.md_theme_tertiary),
    onTertiary = colorResource(id = R.color.md_theme_onTertiary),
    tertiaryContainer = colorResource(id = R.color.md_theme_tertiaryContainer),
    onTertiaryContainer = colorResource(id = R.color.md_theme_onTertiaryContainer),

    error = colorResource(id = R.color.md_theme_error),
    onError = colorResource(id = R.color.md_theme_onError),
    errorContainer = colorResource(id = R.color.md_theme_errorContainer),
    onErrorContainer = colorResource(id = R.color.md_theme_onErrorContainer),

    background = colorResource(id = R.color.md_theme_background),
    onBackground = colorResource(id = R.color.md_theme_onBackground),

    surface = colorResource(id = R.color.md_theme_surface),
    onSurface = colorResource(id = R.color.md_theme_onSurface),
    surfaceVariant = colorResource(id = R.color.md_theme_surfaceVariant),
    onSurfaceVariant = colorResource(id = R.color.md_theme_onSurfaceVariant),

    outline = colorResource(id = R.color.md_theme_outline),
    outlineVariant = colorResource(id = R.color.md_theme_outlineVariant),

    inverseSurface = colorResource(id = R.color.md_theme_inverseSurface),
    inverseOnSurface = colorResource(id = R.color.md_theme_inverseOnSurface),

    surfaceDim = colorResource(id = R.color.md_theme_surfaceDim),
    surfaceBright = colorResource(id = R.color.md_theme_surfaceBright),
    surfaceContainerLowest = colorResource(id = R.color.md_theme_surfaceContainerLowest),
    surfaceContainerLow = colorResource(id = R.color.md_theme_surfaceContainerLow),
    surfaceContainer = colorResource(id = R.color.md_theme_surfaceContainer),
    surfaceContainerHigh = colorResource(id = R.color.md_theme_surfaceContainerHigh),
    surfaceContainerHighest = colorResource(id = R.color.md_theme_surfaceContainerHighest)
)

@Composable
fun MyAppTheme(
    state: ThemeState = ThemeState(),
    content: @Composable () -> Unit
) {
    val dynamicColors = when (state.style) {
        ThemeStyle.DEFAULT -> false
        ThemeStyle.MATERIAL_YOU -> hasApiLevel(31)
    }
    val darkTheme = when (state.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> true
    }

    @SuppressLint("NewApi")
    val colors = when {
        dynamicColors && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColors && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> myDarkColorScheme()
        else -> myLightColorScheme()
    }
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LightThemePreview() = MyAppTheme(
    state = ThemeState(
        mode = ThemeMode.LIGHT,
        style = ThemeStyle.DEFAULT
    )
) { SampleContent() }

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun DarkThemePreview(): Unit = MyAppTheme(
    state = ThemeState(
        mode = ThemeMode.DARK,
        style = ThemeStyle.DEFAULT
    )
) { SampleContent() }

@Preview(showBackground = true, name = "Material You Light Mode")
@Composable
fun MaterialYouLightThemePreview() = MyAppTheme(
    state = ThemeState(
        mode = ThemeMode.LIGHT,
        style = ThemeStyle.MATERIAL_YOU
    )
) { SampleContent() }

@Preview(showBackground = true, name = "Material You Dark Mode")
@Composable
fun MaterialYouDarkThemePreview() = MyAppTheme(
    state = ThemeState(
        mode = ThemeMode.DARK,
        style = ThemeStyle.MATERIAL_YOU
    )
) { SampleContent() }