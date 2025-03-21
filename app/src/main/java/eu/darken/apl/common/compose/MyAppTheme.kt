package eu.darken.apl.common.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import eu.darken.apl.common.hasApiLevel


@Composable
fun MyAppTheme(
    dynamicColors: Boolean = hasApiLevel(31),
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()

    @SuppressLint("NewApi")
    val colors = when {
        dynamicColors && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColors && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LightThemePreview() {
    MyAppTheme(dynamicColors = false) {
        SampleContent()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun DarkThemePreview() {
    MyAppTheme(dynamicColors = false) {
        SampleContent()
    }
}