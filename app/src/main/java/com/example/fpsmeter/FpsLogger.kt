package com.example.fpsmeter

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * يسجّل قيم FPS مع الطابع الزمني إلى ملف CSV داخل تخزين التطبيق الخاص،
 * بحيث يمكن للمستخدم لاحقاً تصدير التقرير أو مشاركته.
 */
class FpsLogger(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val logFile: File by lazy {
        File(context.getExternalFilesDir(null), "fps_report.csv").apply {
            if (!exists()) {
                writeText("timestamp,fps\n")
            }
        }
    }

    fun logFps(fps: Int) {
        FileWriter(logFile, true).use { writer ->
            val timestamp = dateFormat.format(Date())
            writer.append("$timestamp,$fps\n")
        }
    }

    fun getReportFile(): File = logFile

    fun clear() {
        logFile.writeText("timestamp,fps\n")
    }
}
