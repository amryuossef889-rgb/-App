package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val userProgress = db.userProgressDao().getUserProgressDirect()
                val currentSunnahId = userProgress?.currentSunnahId ?: 1
                val currentSunnah = db.sunnahDao().getSunnahWithHadithDirect(currentSunnahId)

                NotificationHelper.showDailySunnahNotification(
                    context = context,
                    sunnahId = currentSunnahId,
                    sunnahTitle = currentSunnah?.sunnah?.title
                )

                // Reschedule for next day
                val settingsRepo = SettingsRepository(context)
                val settings = settingsRepo.settingsFlow.first()
                if (settings.reminderEnabled) {
                    NotificationHelper.scheduleDailyAlarm(
                        context,
                        settings.reminderHour,
                        settings.reminderMinute
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
