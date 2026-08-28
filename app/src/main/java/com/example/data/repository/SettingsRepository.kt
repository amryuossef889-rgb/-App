package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sunnah_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class BackgroundMode {
    DEFAULT, CUSTOM, DISABLED
}

enum class BackgroundScale {
    CROP, FIT
}

enum class AppFontSize {
    SMALL, MEDIUM, LARGE
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val backgroundMode: BackgroundMode = BackgroundMode.DEFAULT,
    val customBackgroundPath: String? = null,
    val backgroundOpacity: Float = 0.25f,
    val backgroundScale: BackgroundScale = BackgroundScale.CROP,
    val fontSize: AppFontSize = AppFontSize.MEDIUM,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20, // 8:00 PM
    val reminderMinute: Int = 0
)

class SettingsRepository(private val context: Context) {
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_BG_MODE = stringPreferencesKey("bg_mode")
        private val KEY_CUSTOM_BG_PATH = stringPreferencesKey("custom_bg_path")
        private val KEY_BG_OPACITY = floatPreferencesKey("bg_opacity")
        private val KEY_BG_SCALE = stringPreferencesKey("bg_scale")
        private val KEY_FONT_SIZE = stringPreferencesKey("font_size")
        private val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val bgModeStr = preferences[KEY_BG_MODE] ?: BackgroundMode.DEFAULT.name
        val bgScaleStr = preferences[KEY_BG_SCALE] ?: BackgroundScale.CROP.name
        val fontSizeStr = preferences[KEY_FONT_SIZE] ?: AppFontSize.MEDIUM.name

        AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM),
            backgroundMode = runCatching { BackgroundMode.valueOf(bgModeStr) }.getOrDefault(BackgroundMode.DEFAULT),
            customBackgroundPath = preferences[KEY_CUSTOM_BG_PATH],
            backgroundOpacity = preferences[KEY_BG_OPACITY] ?: 0.25f,
            backgroundScale = runCatching { BackgroundScale.valueOf(bgScaleStr) }.getOrDefault(BackgroundScale.CROP),
            fontSize = runCatching { AppFontSize.valueOf(fontSizeStr) }.getOrDefault(AppFontSize.MEDIUM),
            reminderEnabled = preferences[KEY_REMINDER_ENABLED] ?: true,
            reminderHour = preferences[KEY_REMINDER_HOUR] ?: 20,
            reminderMinute = preferences[KEY_REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun updateBackgroundMode(mode: BackgroundMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BG_MODE] = mode.name
        }
    }

    suspend fun updateCustomBackgroundPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path != null) {
                preferences[KEY_CUSTOM_BG_PATH] = path
            } else {
                preferences.remove(KEY_CUSTOM_BG_PATH)
            }
        }
    }

    suspend fun updateBackgroundOpacity(opacity: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BG_OPACITY] = opacity.coerceIn(0.05f, 1.0f)
        }
    }

    suspend fun updateBackgroundScale(scale: BackgroundScale) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BG_SCALE] = scale.name
        }
    }

    suspend fun updateFontSize(size: AppFontSize) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_SIZE] = size.name
        }
    }

    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMINDER_ENABLED] = enabled
            preferences[KEY_REMINDER_HOUR] = hour
            preferences[KEY_REMINDER_MINUTE] = minute
        }
    }
}
