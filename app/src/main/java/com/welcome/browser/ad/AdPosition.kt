package com.welcome.browser.ad

/**
 * @Author leo
 * @Date 2023/3/20
 */
enum class AdPosition(val cache: Int, val preload: Boolean) {

    HOME(1, true),
    TAP(1, true),
    LOADING(2, true),
    CLEAN(2, true);
}