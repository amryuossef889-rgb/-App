package com.example.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppFontSize
import com.example.data.repository.AppSettings
import com.example.data.repository.BackgroundMode
import com.example.data.repository.BackgroundScale
import com.example.data.repository.SettingsRepository
import com.example.data.repository.ThemeMode
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun setBackgroundMode(mode: BackgroundMode) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundMode(mode)
        }
    }

    fun setCustomBackgroundPath(path: String?) {
        viewModelScope.launch {
            settingsRepository.updateCustomBackgroundPath(path)
            settingsRepository.updateBackgroundMode(BackgroundMode.CUSTOM)
        }
    }

    fun setBackgroundOpacity(opacity: Float) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundOpacity(opacity)
        }
    }

    fun setBackgroundScale(scale: BackgroundScale) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundScale(scale)
        }
    }

    fun setFontSize(size: AppFontSize) {
        viewModelScope.launch {
            settingsRepository.updateFontSize(size)
        }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.updateReminderSettings(enabled, hour, minute)
            if (enabled) {
                NotificationHelper.scheduleDailyAlarm(context, hour, minute)
            } else {
                NotificationHelper.cancelDailyAlarm(context)
            }
        }
    }

    fun resetBackground() {
        viewModelScope.launch {
            settingsRepository.updateCustomBackgroundPath(null)
            settingsRepository.updateBackgroundMode(BackgroundMode.DEFAULT)
            settingsRepository.updateBackgroundOpacity(0.25f)
            settingsRepository.updateBackgroundScale(BackgroundScale.CROP)
        }
    }

    companion object {
        fun provideFactory(context: Context, settingsRepository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(context, settingsRepository) as T
                }
            }
    }
}
