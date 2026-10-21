package com.englishlearningapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    primaryContainer = AppPrimaryContainer,
    onPrimaryContainer = Color(0xFF18305F),
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = AppScreenBackground,
    surface = AppCardBackground,
    surfaceContainer = AppCardBackground,
    surfaceContainerLow = AppCardBackground,
    surfaceContainerLowest = AppCardBackground,
    surfaceVariant = AppCardBackground,
    onBackground = Color(0xFF17191C),
    onSurface = Color(0xFF17191C),
    onSurfaceVariant = AppSecondaryText,
    outline = Color(0xFFD7D9DE),
    outlineVariant = Color(0xFFE4E3E0)
)

@Composable
fun EnglishLearningAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
