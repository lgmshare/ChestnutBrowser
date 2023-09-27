package com.welcome.browser.utils

import com.welcome.browser.App

object SharePrefUtils {

    fun isFirstLaunch(): Boolean {
        val sf = App.INSTANCE.sharePref

        val isFirstLaunch = sf.getBoolean("isFirstLaunch", true)
        if (isFirstLaunch) {
            sf.edit().putBoolean("isFirstLaunch", false).apply()
            return true
        }

        return false
    }

    var country: String?
        get() = App.INSTANCE.sharePref.getString("country", "")
        set(value) = App.INSTANCE.sharePref.edit().putString("country", value).apply()

    var adDataDate: String?
        get() = App.INSTANCE.sharePref.getString("adDataDate", "")
        set(value) = App.INSTANCE.sharePref.edit().putString("adDataDate", value).apply()

    var adShowCount: Int
        get() = App.INSTANCE.sharePref.getInt("adShowCount", 0)
        set(value) = App.INSTANCE.sharePref.edit().putInt("adShowCount", value).apply()

    var adClickCount: Int
        get() = App.INSTANCE.sharePref.getInt("adClickCount", 0)
        set(value) = App.INSTANCE.sharePref.edit().putInt("adClickCount", value).apply()
}