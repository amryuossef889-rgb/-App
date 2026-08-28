package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.repository.AppFontSize
import com.example.data.repository.ThemeMode

val LocalAppFontSize = compositionLocalOf { AppFontSize.MEDIUM }

private val LightColorScheme = lightColorScheme(
    primary = MinimalEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = MinimalEmeraldLight,
    onPrimaryContainer = MinimalEmeraldPrimary,
    secondary = MinimalEmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = MinimalEmeraldLight,
    onSecondaryContainer = MinimalEmeraldPrimary,
    tertiary = StreakFireOrange,
    onTertiary = Color.White,
    background = MinimalBackgroundLight,
    onBackground = MinimalTextPrimaryLight,
    surface = MinimalSurfaceLight,
    onSurface = MinimalTextPrimaryLight,
    surfaceVariant = MinimalSurfaceContainerLight,
    onSurfaceVariant = MinimalTextSecondaryLight,
    outline = MinimalBorderLightMedium
)

private val DarkColorScheme = darkColorScheme(
    primary = MinimalDarkPrimary,
    onPrimary = MinimalDarkOnPrimary,
    primaryContainer = MinimalDarkSurfaceContainer,
    onPrimaryContainer = MinimalDarkPrimary,
    secondary = MinimalDarkSecondary,
    onSecondary = MinimalDarkOnPrimary,
    secondaryContainer = MinimalDarkSurfaceContainer,
    onSecondaryContainer = MinimalDarkSecondary,
    tertiary = Color(0xFFFDBA74),
    onTertiary = Color(0xFF431407),
    background = MinimalDarkBackground,
    onBackground = MinimalDarkTextPrimary,
    surface = MinimalDarkSurface,
    onSurface = MinimalDarkTextPrimary,
    surfaceVariant = MinimalDarkSurfaceContainer,
    onSurfaceVariant = MinimalDarkTextSecondary,
    outline = MinimalDarkBorder
)

@Composable
fun SunnahTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontSize: AppFontSize = AppFontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppFontSize provides fontSize) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
