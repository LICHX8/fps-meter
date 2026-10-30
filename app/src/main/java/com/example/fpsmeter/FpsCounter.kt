package com.example.fpsmeter

import android.view.Choreographer

/**
 * يقيس معدل الإطارات (FPS) الفعلي للنظام باستخدام Choreographer،
 * الذي يستقبل إشعاراً من نظام أندرويد عند كل نبضة Vsync (رسم إطار جديد).
 *
 * الفكرة: نحسب عدد الإطارات التي مرت خلال نافذة زمنية (ثانية واحدة تقريباً)
 * ثم نحوّلها إلى معدل FPS.
 */
class FpsCounter(private val onFpsUpdate: (Int) -> Unit) {

    private val choreographer = Choreographer.getInstance()
    private var frameCount = 0
    private var windowStartNanos = 0L
    private var isRunning = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (windowStartNanos == 0L) {
                windowStartNanos = frameTimeNanos
            }

            frameCount++
            val elapsedNanos = frameTimeNanos - windowStartNanos

            // كل ثانية (1_000_000_000 نانوثانية) نحسب الـ FPS ونصفّر العداد
            if (elapsedNanos >= 1_000_000_000L) {
                val elapsedSeconds = elapsedNanos / 1_000_000_000.0
                val fps = (frameCount / elapsedSeconds).toInt()
                onFpsUpdate(fps)

                frameCount = 0
                windowStartNanos = frameTimeNanos
            }

            choreographer.postFrameCallback(this)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        frameCount = 0
        windowStartNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        choreographer.removeFrameCallback(frameCallback)
    }
}
