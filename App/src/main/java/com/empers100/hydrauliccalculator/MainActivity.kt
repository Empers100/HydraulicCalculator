package com.empers100.hydrauliccalculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

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

        webView.addJavascriptInterface(ShareInterface(), "AndroidShare")
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/HydraulicCalculator.html")
    }

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

        @JavascriptInterface
        fun copyToClipboard(text: String) {
            runOnUiThread {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("HydraulicCalculator Link", text)
                clipboard.setPrimaryClip(clip)
                webView.evaluateJavascript("alert('✅ Ссылка скопирована!')", null)
            }
        }
    }
}