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
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.welcome.browser.ad.AdManager
import com.welcome.browser.firebase.FirebaseRemoteConfigUtil
import com.welcome.browser.ui.WelcomeActivity

class App : Application(), LifecycleEventObserver {

    lateinit var sharePref: SharedPreferences

    var activityCount: Int = 0
    var startCount: Int = 0
    var isFront: Boolean = false

    companion object {
        const val AD_CONFIG =
            "ewogICAgIm1heFNob3dDb3VudCI6IDMwLAogICAgIm1heENsaWNrQ291bnQiOiAxMCwKICAgICJhZElkcyI6IFsKICAgICAgICB7CiAgICAgICAgICAgICJpZCI6ICJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvMTAzMzE3MzcxMiIsCiAgICAgICAgICAgICJjYWNoZSI6IDIsCiAgICAgICAgICAgICJwbGF0Zm9ybSI6ICJhZG1vYiIsCiAgICAgICAgICAgICJ0eXBlIjogMiwKICAgICAgICAgICAgImxldmVsIjogNAogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiaWQiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzEwMzMxNzM3MTJ4MyIsCiAgICAgICAgICAgICJjYWNoZSI6IDIsCiAgICAgICAgICAgICJwbGF0Zm9ybSI6ICJhZG1vYiIsCiAgICAgICAgICAgICJ0eXBlIjogMiwKICAgICAgICAgICAgImxldmVsIjogMwogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiaWQiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzEwMzMxNzM3MTJ4MiIsCiAgICAgICAgICAgICJjYWNoZSI6IDIsCiAgICAgICAgICAgICJwbGF0Zm9ybSI6ICJhZG1vYiIsCiAgICAgICAgICAgICJ0eXBlIjogMiwKICAgICAgICAgICAgImxldmVsIjogMgogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiaWQiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzEwMzMxNzM3MTIiLAogICAgICAgICAgICAiY2FjaGUiOiAyLAogICAgICAgICAgICAicGxhdGZvcm0iOiAiYWRtb2IiLAogICAgICAgICAgICAidHlwZSI6IDIsCiAgICAgICAgICAgICJsZXZlbCI6IDMKICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImlkIjogImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8yMjQ3Njk2MTEweDIiLAogICAgICAgICAgICAiY2FjaGUiOiAxLAogICAgICAgICAgICAicGxhdGZvcm0iOiAiYWRtb2IiLAogICAgICAgICAgICAidHlwZSI6IDEsCiAgICAgICAgICAgICJsZXZlbCI6IDEKICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImlkIjogImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8yMjQ3Njk2MTEwIiwKICAgICAgICAgICAgImNhY2hlIjogMSwKICAgICAgICAgICAgInBsYXRmb3JtIjogImFkbW9iIiwKICAgICAgICAgICAgInR5cGUiOiAxLAogICAgICAgICAgICAibGV2ZWwiOiA0CiAgICAgICAgfQogICAgXQp9"

        lateinit var INSTANCE: App

    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        sharePref = getSharedPreferences("browser", MODE_PRIVATE)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
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
                if (p0 is AdActivity) {
                    p0.finish()
                }
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }
        })

        FirebaseRemoteConfigUtil.init(this)

        AdManager.init(this)
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