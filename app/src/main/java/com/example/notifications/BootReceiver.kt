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

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsRepo = SettingsRepository(context)
                    val settings = settingsRepo.settingsFlow.first()
                    if (settings.reminderEnabled) {
                        NotificationHelper.scheduleDailyAlarm(
                            context,
                            settings.reminderHour,
                            settings.reminderMinute
                        )
                    }
                    if (settings.persistentSunnahEnabled) {
                        val db = AppDatabase.getInstance(context)
                        val userProgress = db.userProgressDao().getUserProgressDirect()
                        val currentSunnahId = userProgress?.currentSunnahId ?: 1
                        val currentSunnah = db.sunnahDao().getSunnahWithHadithDirect(currentSunnahId)
                        NotificationHelper.showPersistentSunnahNotification(
                            context,
                            currentSunnahId,
                            currentSunnah?.sunnah?.title
                        )
                        NotificationHelper.schedulePersistentSunnahRefresh(context)
                    } else {
                        NotificationHelper.cancelPersistentSunnahRefresh(context)
                        NotificationHelper.cancelPersistentSunnahNotification(context)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
