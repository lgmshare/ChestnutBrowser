package com.welcome.browser.firebase

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class FirebaseRemoteConfigUtil {

    companion object {

        fun init(context: Context) {
            //初始化
            Firebase.initialize(context)
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            })
            remoteConfig.fetchAndActivate()
        }

        fun getConfig() {

        }
    }
}