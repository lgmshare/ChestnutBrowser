package com.welcome.browser.web

import android.graphics.Bitmap
import com.welcome.browser.model.WebLink
import com.welcome.browser.views.ChestnutWeb

object WebManagers {

    private val indexWebLinks = WebLink("", null, 1, ChestnutWeb())

    val webPageLists = mutableListOf<WebLink>().apply {
        sortBy { it.index }
    }

    var currentWebLinks = indexWebLinks

    fun addWeb(webLinks: WebLink) {
        currentWebLinks = webLinks
        webPageLists.add(webLinks)
        chestnutWebListener?.addWeb(webLinks)
    }

    fun getMaxWeb(): Int {
        if (webPageLists.isEmpty()) {
            return 0
        }

        return webPageLists.sortedBy {
            it.index
        }.last().index
    }

    fun getWebLinks(index: Int): WebLink {
        return webPageLists.find {
            it.index == index
        } ?: indexWebLinks
    }

    fun getMinWebLinks(): WebLink {
        return webPageLists.sortedBy {
            -it.index
        }.getOrNull(0) ?: indexWebLinks
    }

    fun removeWebLinks(webLinks: WebLink) {
        webPageLists.remove(webLinks)
    }

    fun getWebIndex(webView: ChestnutWeb): Int {
        return webPageLists.find {
            it.webView == webView
        }?.index ?: 0
    }

    fun cleanAllWeb() {
        webPageLists.clear()
        webPageLists.add(indexWebLinks)
        currentWebLinks = indexWebLinks
        chestnutWebListener?.clean()
    }

    fun updateCurrentWebBitMap(bitMap: Bitmap?) {
        if (bitMap != null) {
            currentWebLinks.bitmap = bitMap
        }
    }

    var chestnutWebListener: ChestnutWebListener? = null

    interface ChestnutWebListener {
        fun onProgressChanged(progress: Int, index: Int)
        fun addWeb(webLinks: WebLink)
        fun removeWeb(webLinks: WebLink)
        fun onWebChanged(webLinks: WebLink)
        fun clean() {}
    }
}