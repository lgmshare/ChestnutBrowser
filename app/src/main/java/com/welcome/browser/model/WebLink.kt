package com.welcome.browser.model

import android.graphics.Bitmap
import com.welcome.browser.views.ChestnutWeb

data class WebLink(var path: String, var bitmap: Bitmap?, val index: Int, val webView: ChestnutWeb) {
    var createTime = System.currentTimeMillis()
    var inputText = ""
}