package com.welcome.browser.views

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.core.view.drawToBitmap
import com.welcome.browser.App
import com.welcome.browser.firebase.FirebaseEventUtil
import com.welcome.browser.web.WebManagers

class ChestnutWeb @JvmOverloads constructor(context: Context = App.INSTANCE, attributeSet: AttributeSet? = null) : WebView(context, attributeSet) {

    enum class WebState {
        IDEA, LOADING, FINISH, STOPPED
    }

    init {
        settings.apply {
            javaScriptEnabled = true
            setSupportZoom(false)
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            useWideViewPort = false
            loadWithOverviewMode = true
            defaultTextEncodingName = "utf-8"
            loadsImagesAutomatically = true
            allowContentAccess = true
            allowFileAccess = true
            builtInZoomControls = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
        webChromeClient = ChestnutWebChromeClient()
        webViewClient = ChestnutWebClient()
    }

    private var isClearHistory = false

    private var loadState: WebState = WebState.IDEA

    val isIdea: Boolean
        get() {
            return loadState == WebState.IDEA
        }

    val isStopped: Boolean
        get() {
            return loadState == WebState.STOPPED
        }

    val isLoadingFinish: Boolean
        get() {
            return loadState == WebState.FINISH
        }

    val isLoading: Boolean
        get() {
            return loadState == WebState.LOADING
        }

    var startTimes = 0L

    fun startLoad(path: String) {
        loadState = WebState.LOADING
        if (Patterns.WEB_URL.matcher(path).matches()) {
            loadUrl(path)
        } else {
            WebManagers.currentWebLinks.webView.startLoad("https://www.google.com/search?q=${path}")
        }
    }

    fun stopLoad() {
        loadState = WebState.STOPPED
        stopLoading()
    }

    fun clearHistories() {
        isClearHistory = true
    }

    inner class ChestnutWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress >= 100) {
                if (isClearHistory) {
                    isClearHistory = false
                    view.clearHistory()
                }
            }
            if (loadState != WebState.STOPPED) {
                loadState = if (newProgress >= 100) {
                    if (startTimes != 0L) {
                        FirebaseEventUtil.webLoadEvent(((System.currentTimeMillis() - startTimes) / 1000))
                        startTimes = 0L
                    }

                    WebState.FINISH
                } else {
                    WebState.LOADING
                }
                WebManagers.chestnutWebListener?.onProgressChanged(newProgress, WebManagers.getWebIndex(this@ChestnutWeb))
            }
        }
    }

    inner class ChestnutWebClient : WebViewClient() {
        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loadState = WebState.LOADING
            startTimes = System.currentTimeMillis()
        }

        override fun onPageFinished(view: WebView?, url: String) {
            super.onPageFinished(view, url)
            if (loadState != WebState.STOPPED) {
                loadState = WebState.FINISH
            }

            val webView = WebManagers.currentWebLinks.webView
            if (webView.isLaidOut) {
                val bitmap = webView.drawToBitmap(Bitmap.Config.ARGB_8888)
                WebManagers.updateCurrentWebBitMap(bitmap)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onSafeBrowsingHit(view: WebView?, request: WebResourceRequest?, threatType: Int, callback: SafeBrowsingResponse?) {
            callback?.backToSafety(true)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return super.shouldOverrideUrlLoading(view, url)
        }
    }
}