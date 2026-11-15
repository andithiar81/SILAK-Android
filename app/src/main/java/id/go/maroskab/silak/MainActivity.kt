package id.go.maroskab.silak

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.view.View
import android.widget.FrameLayout
import android.print.PrintManager
import android.content.Context

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val fileRequest = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(23,107,69)
        window.navigationBarColor = android.graphics.Color.BLACK
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.settings.setSupportZoom(false)
        web.addJavascriptInterface(object {
            @JavascriptInterface
            fun printPage() {
                runOnUiThread {
                    val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val adapter = web.createPrintDocumentAdapter("Laporan SILAK")
                    printManager.print("Laporan SILAK", adapter, android.print.PrintAttributes.Builder().build())
                }
            }
        }, "AndroidPrint")
        web.webViewClient = WebViewClient()
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?, callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    startActivityForResult(params?.createIntent(), fileRequest)
                    true
                } catch (e: Exception) {
                    fileCallback = null
                    false
                }
            }
        }
        setContentView(web)
        web.loadUrl("file:///android_asset/www/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileRequest) {
            val result = if (resultCode == Activity.RESULT_OK && data != null)
                WebChromeClient.FileChooserParams.parseResult(resultCode, data) else null
            fileCallback?.onReceiveValue(result)
            fileCallback = null
        }
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
