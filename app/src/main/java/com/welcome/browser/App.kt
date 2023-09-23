package com.welcome.browser

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.welcome.browser.ui.WelcomeActivity

class App : Application(), LifecycleEventObserver {

    lateinit var sharePref: SharedPreferences

    var activityCount: Int = 0
    var startCount: Int = 0
    var isFront: Boolean = false

    companion object {
        lateinit var INSTANCE: App
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        sharePref = getSharedPreferences("browser", MODE_PRIVATE)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                activityCount++
            }

            override fun onActivityStarted(activity: Activity) {
                startCount++
                if (startCount == 1) {
                    if (activity !is WelcomeActivity) {
                        activity.startActivity(Intent(activity, WelcomeActivity::class.java))
                    }
                }
            }

            override fun onActivityResumed(p0: Activity) {
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
                startCount--
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
                activityCount--
            }
        })

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                isFront = true
            }

            Lifecycle.Event.ON_STOP -> {
                isFront = false
            }

            else -> {
            }
        }
    }

}