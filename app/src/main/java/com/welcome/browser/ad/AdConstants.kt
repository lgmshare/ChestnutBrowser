package com.welcome.browser.ad

/**
 * 错误码
 */
interface AdCode {
    companion object {
        const val NOT_SUPPORT_PLATFORM = 1
        const val NOT_SUPPORT_AD_TYPE = 2
    }
}

/**
 * 广告平台
 */
interface AdPlatform {
    companion object {
        const val PLATFORM_ADMOB = "admob"
    }
}

/**
 *类型
 */
interface AdType {
    companion object {
        const val NATIVE = 1 //原生广告
        const val INTERSTITIAL = 2 // 插屏广告
        const val OPEN = 3 //
    }
}