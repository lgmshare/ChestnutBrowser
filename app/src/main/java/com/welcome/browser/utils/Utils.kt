package com.welcome.browser.utils

import android.util.Log
import com.welcome.browser.BuildConfig

class Utils {

    companion object {

        fun log(msg: String?) {
            if (BuildConfig.DEBUG) {
                if (!msg.isNullOrEmpty()) {
                    Log.d("LogHelper", msg)
                }
            }
        }
    }
}