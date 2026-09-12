package com.sahelibuzz.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var root: FrameLayout

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST = 1001
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.BLACK

        root = FrameLayout(this)

        // ---------------- SPLASH SCREEN ----------------

        val splash = FrameLayout(this).apply {
    setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
    text = "SaheliBuzz"
    textSize = 34f
    typeface = android.graphics.Typeface.create(
        "sans-serif",
        android.graphics.Typeface.BOLD
    )
    setTextColor(Color.rgb(210, 20, 110))
    gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
    text = "BY VAIBHAV"
    textSize = 13f
    letterSpacing = 0.18f
    typeface = android.graphics.Typeface.create(
        "sans-serif-medium",
        android.graphics.Typeface.NORMAL
    )
    setTextColor(Color.rgb(110, 110, 110))
    gravity = Gravity.CENTER
        }

        splash.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        splash.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        val tagline = TextView(this).apply {
    text = "Connect • Chat • Share"
    textSize = 12f
    letterSpacing = 0.08f
    setTextColor(Color.rgb(150, 150, 150))
    gravity = Gravity.CENTER
        }

    splash.addView(
    tagline,
    FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        Gravity.CENTER
    ).apply {
        topMargin = 180.dp
    }
)

        root.addView(
            splash,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // ---------------- WEBVIEW ----------------

        webView = WebView(this).apply {

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true

                allowFileAccess = true
                allowContentAccess = true

                useWideViewPort = true
                loadWithOverviewMode = true

                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(false)
            }

            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url = request?.url?.toString() ?: return false

                    return if (
                        url.startsWith("http://") ||
                        url.startsWith("https://")
                    ) {
                        false
                    } else {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        } catch (_: Exception) {
                        }

                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {

                    this@MainActivity.filePathCallback?.onReceiveValue(null)

                    this@MainActivity.filePathCallback = filePathCallback

                    val intent = fileChooserParams?.createIntent()

                    try {
                        startActivityForResult(
                            intent ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "image/*"
                                addCategory(Intent.CATEGORY_OPENABLE)
                            },
                            FILE_CHOOSER_REQUEST
                        )
                    } catch (_: Exception) {

                        this@MainActivity.filePathCallback = null
                        return false
                    }

                    return true
                }
            }

            visibility = View.INVISIBLE
        }

        val webParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        root.addView(webView, webParams)

        setContentView(root)

        // ---------------- SYSTEM BAR FIX ----------------

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val params = webView.layoutParams as FrameLayout.LayoutParams

            params.topMargin = bars.top
            params.bottomMargin = bars.bottom

            params.leftMargin = 0
            params.rightMargin = 0

            webView.layoutParams = params

            insets
        }

        ViewCompat.requestApplyInsets(root)

        // ---------------- OPEN WEBSITE ----------------

        android.os.Handler(mainLooper).postDelayed({

            splash.visibility = View.GONE
            webView.visibility = View.VISIBLE

            webView.loadUrl("https://saheli-buzz.vercel.app/")

            ViewCompat.requestApplyInsets(root)

        }, 1500)
    }

    // ---------------- FILE PICKER RESULT ----------------

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST) {

            val results: Array<Uri>? =
                if (resultCode == Activity.RESULT_OK) {

                    data?.data?.let {
                        arrayOf(it)
                    }

                } else {
                    null
                }

            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    // ---------------- BACK BUTTON ----------------

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // ---------------- CLEANUP ----------------

    override fun onDestroy() {

        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }

        filePathCallback?.onReceiveValue(null)
        filePathCallback = null

        super.onDestroy()
    }
}
private val Int.dp: Int
    get() = (this * resources.displayMetrics.density).toInt()
