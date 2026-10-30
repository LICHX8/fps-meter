package com.example.fpsmeter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.LinkedList

/**
 * عرض مخصص (Custom View) خفيف يرسم آخر N قيمة FPS كخط بياني بسيط.
 * لا يعتمد على أي مكتبة خارجية لإبقاء التطبيق صغيراً ومستقلاً.
 */
class FpsGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val maxPoints = 60
    private val values = LinkedList<Int>()

    private val linePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 4f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#1B1B1B")
        style = Paint.Style.FILL
    }

    fun addValue(fps: Int) {
        values.addLast(fps)
        if (values.size > maxPoints) values.removeFirst()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        if (values.size < 2) return

        val maxFps = (values.maxOrNull() ?: 60).coerceAtLeast(60)
        val stepX = width.toFloat() / (maxPoints - 1)

        var prevX = 0f
        var prevY = height - (values[0].toFloat() / maxFps) * height
        val offset = maxPoints - values.size

        for (i in 1 until values.size) {
            val x = (offset + i) * stepX
            val y = height - (values[i].toFloat() / maxFps) * height

            linePaint.color = when {
                values[i] >= 50 -> Color.parseColor("#4CAF50")
                values[i] >= 30 -> Color.parseColor("#FFC107")
                else -> Color.parseColor("#F44336")
            }

            canvas.drawLine(prevX, prevY, x, y, linePaint)
            prevX = x
            prevY = y
        }
    }
}
