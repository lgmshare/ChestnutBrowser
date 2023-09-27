package com.welcome.browser.ad

/**
 * @Author leo
 * @Date 2023/3/20
 */
data class AdData<T>(
    val adId: AdId,
    val ad: T? = null,
    val success: Boolean = false,
    val code: Int? = null,
    val msg: String? = null,
) {
    val cacheTime = System.currentTimeMillis()
}