package com.example.fpsmeter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

/**
 * خدمة أمامية (Foreground Service) تعرض نافذة عائمة صغيرة فوق التطبيقات الأخرى
 * تُظهر معدل الإطارات الحالي، وتسجّل القيم في ملف تقرير محلي.
 */
class FpsOverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "fps_meter_channel"
        const val NOTIFICATION_ID = 1001
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: LinearLayout? = null
    private var fpsTextView: TextView? = null
    private lateinit var fpsCounter: FpsCounter
    private lateinit var fpsLogger: FpsLogger

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        fpsLogger = FpsLogger(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        addOverlayView()

        fpsCounter = FpsCounter { fps ->
            fpsTextView?.text = "$fps FPS"
            fpsTextView?.setTextColor(colorForFps(fps))
            fpsLogger.logFps(fps)
        }
        fpsCounter.start()
    }

    private fun colorForFps(fps: Int): Int = when {
        fps >= 50 -> Color.parseColor("#4CAF50")
        fps >= 30 -> Color.parseColor("#FFC107")
        else -> Color.parseColor("#F44336")
    }

    private fun addOverlayView() {
        val layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 100

        val container = LinearLayout(this).apply {
            setBackgroundColor(Color.parseColor("#CC1B1B1B"))
            setPadding(24, 12, 24, 12)
        }

        val textView = TextView(this).apply {
            text = "-- FPS"
            textSize = 16f
            setTextColor(Color.WHITE)
        }

        container.addView(textView)
        fpsTextView = textView
        overlayView = container

        // السماح بسحب النافذة العائمة لتغيير موقعها
        container.setOnTouchListener(object : android.view.View.OnTouchListener {
            var initialX = 0
            var initialY = 0
            var touchX = 0f
            var touchY = 0f

            override fun onTouch(v: android.view.View?, event: android.view.MotionEvent): Boolean {
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                    }
                    android.view.MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - touchX).toInt()
                        params.y = initialY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(overlayView, params)
                    }
                }
                return true
            }
        })

        windowManager.addView(container, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        super.onDestroy()
        fpsCounter.stop()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
