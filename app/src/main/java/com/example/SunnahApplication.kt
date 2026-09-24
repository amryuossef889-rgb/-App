package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SunnahRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SunnahApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val sunnahRepository: SunnahRepository by lazy { SunnahRepository(database) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.settingsFlow.first()
                if (settings.reminderEnabled) {
                    NotificationHelper.scheduleDailyAlarm(
                        this@SunnahApplication,
                        settings.reminderHour,
                        settings.reminderMinute
                    )
                }
                if (settings.persistentSunnahEnabled) {
                    val userProgress = database.userProgressDao().getUserProgressDirect()
                    val currentSunnahId = userProgress?.currentSunnahId ?: 1
                    val currentSunnah = database.sunnahDao().getSunnahWithHadithDirect(currentSunnahId)
                    NotificationHelper.showPersistentSunnahNotification(
                        this@SunnahApplication,
                        currentSunnahId,
                        currentSunnah?.sunnah?.title
                    )
                    NotificationHelper.schedulePersistentSunnahRefresh(this@SunnahApplication)
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
                        android.provider.Settings.canDrawOverlays(this@SunnahApplication)) {
                        com.example.notifications.SunnahOverlayService.start(this@SunnahApplication)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
