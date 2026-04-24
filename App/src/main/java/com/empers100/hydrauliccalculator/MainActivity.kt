package com.empers100.hydrauliccalculator

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var pendingReport: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        // Регистрируем оба интерфейса
        webView.addJavascriptInterface(ShareInterface(), "AndroidShare")
        webView.addJavascriptInterface(SaveInterface(), "AndroidSave")

        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/HydraulicCalculator.html")
    }

    // Интерфейс для кнопки «Поделиться»
    inner class ShareInterface {
        @JavascriptInterface
        fun shareText(title: String, text: String, url: String) {
            runOnUiThread {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, "$text\n$url")
                }
                startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
            }
        }
    }

    // Интерфейс для кнопки «Сохранить в файл»
    inner class SaveInterface {
        @JavascriptInterface
        fun saveReport(reportText: String) {
            runOnUiThread {
                pendingReport = reportText
                val fileName = "hydraulic_report_${System.currentTimeMillis()}.txt"
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, fileName)
                }
                startActivityForResult(intent, REQUEST_CODE_SAVE_REPORT)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SAVE_REPORT = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SAVE_REPORT && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    pendingReport?.let { report ->
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(report.toByteArray())
                        }
                        Toast.makeText(this, "Отчёт сохранён", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(this, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    pendingReport = null
                }
            }
        }
    }
}
