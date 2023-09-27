package com.welcome.browser.ad

/**
 * @Author leo
 * @Date 2023/3/20
 */
data class AdId(
    val id: String,
    val platform: String,
    val cache: Int,
    val type: Int,
    val level: Int
) {
    fun getIdAndSortKey(): String {
        return "$id : $level"
    }
}