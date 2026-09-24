package com.example.notifications

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SunnahOverlayService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(OVERLAY_NOTIFICATION_ID, buildForegroundNotification())
        showOrUpdateOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_UPDATE) showOrUpdateOverlay()
        return START_STICKY
    }

    private fun showOrUpdateOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@SunnahOverlayService)
            val progress = db.userProgressDao().getUserProgressDirect()
            val id = progress?.currentSunnahId ?: 1
            val sunnah = db.sunnahDao().getSunnahWithHadithDirect(id)
            val title = sunnah?.sunnah?.title?.takeIf { it.isNotBlank() } ?: "افتح التطبيق لمعرفة سُنّة اليوم"
            launch(Dispatchers.Main) { renderOverlay(title) }
        }
    }

    private fun renderOverlay(title: String) {
        val wm = windowManager ?: getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        if (overlayView == null) {
            val view = TextView(this).apply {
                setTextColor(Color.WHITE)
                setTextSize(15f)
                setPadding(28, 18, 28, 18)
                setBackgroundColor(Color.rgb(30, 30, 30))
                gravity = Gravity.CENTER_VERTICAL
                elevation = 12f
                setOnClickListener {
                    val open = Intent(this@SunnahOverlayService, com.example.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(open)
                }
            }

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 18
            }

            try {
                wm.addView(view, params)
                overlayView = view
            } catch (e: Exception) {
                stopSelf()
                return
            }
        }

        overlayView?.text = "🌿 سُنّة اليوم  •  $title"
    }

    private fun buildForegroundNotification(): Notification {
        NotificationHelper.createNotificationChannel(this)
        val intent = Intent(this, com.example.MainActivity::class.java)
        val pending = android.app.PendingIntent.getActivity(
            this, 3001, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "sunnah_persistent_heads_up_v2")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("سُنّة اليوم")
            .setContentText("العرض العائم مفعّل")
            .setOngoing(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onDestroy() {
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val ACTION_UPDATE = "com.example.action.UPDATE_SUNNAH_OVERLAY"
        private const val OVERLAY_NOTIFICATION_ID = 1003

        fun start(context: Context) {
            val intent = Intent(context, SunnahOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context) {
            context.startService(Intent(context, SunnahOverlayService::class.java).apply {
                action = ACTION_UPDATE
            })
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SunnahOverlayService::class.java))
        }
    }
}
