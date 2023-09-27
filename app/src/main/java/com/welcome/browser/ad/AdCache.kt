package com.welcome.browser.ad

import kotlinx.coroutines.Job

/**
 * 广告缓存池
 */
class AdCache {
    val caches = mutableListOf<AdData<*>>()
    var job: Job? = null
}