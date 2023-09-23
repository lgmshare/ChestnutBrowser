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


}