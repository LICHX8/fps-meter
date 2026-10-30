package com.example.fpsmeter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider

class MainActivity : AppCompatActivity() {

    private var isServiceRunning = false
    private lateinit var fpsLogger: FpsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fpsLogger = FpsLogger(this)

        val statusText = findViewById<TextView>(R.id.statusText)
        val btnToggle = findViewById<Button>(R.id.btnToggle)
        val btnExport = findViewById<Button>(R.id.btnExport)

        btnToggle.setOnClickListener {
            if (!isServiceRunning) {
                if (!hasOverlayPermission()) {
                    requestOverlayPermission()
                    return@setOnClickListener
                }
                startOverlayService()
                isServiceRunning = true
                statusText.text = "الحالة: يعمل"
                btnToggle.text = getString(R.string.btn_stop)
            } else {
                stopOverlayService()
                isServiceRunning = false
                statusText.text = "الحالة: متوقف"
                btnToggle.text = getString(R.string.btn_start)
            }
        }

        btnExport.setOnClickListener {
            exportReport()
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        Toast.makeText(this, getString(R.string.permission_needed), Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun startOverlayService() {
        val intent = Intent(this, FpsOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopOverlayService() {
        stopService(Intent(this, FpsOverlayService::class.java))
    }

    private fun exportReport() {
        val file = fpsLogger.getReportFile()
        if (!file.exists() || file.length() == 0L) {
            Toast.makeText(this, "لا توجد بيانات مسجّلة بعد", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this, "$packageName.fileprovider", file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير FPS"))
    }

    override fun onDestroy() {
        super.onDestroy()
        // الخدمة تستمر بالعمل في الخلفية حتى لو أُغلقت الواجهة، ما لم يضغط المستخدم إيقاف
    }
}
