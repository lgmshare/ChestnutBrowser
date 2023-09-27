package com.welcome.browser.ui

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.welcome.browser.App
import com.welcome.browser.ad.AdManager
import com.welcome.browser.ad.AdPosition
import com.welcome.browser.databinding.WelcomeActivityBinding
import com.welcome.browser.firebase.FirebaseEventUtil
import com.welcome.browser.ui.base.BaseActivity
import com.welcome.browser.utils.SharePrefUtils
import com.welcome.browser.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class WelcomeActivity : BaseActivity<WelcomeActivityBinding>() {

    private var job: Job? = null

    override fun onbBuildVB(): WelcomeActivityBinding {
        return WelcomeActivityBinding.inflate(layoutInflater)
    }

    override fun onCreatePage() {

    }

    override fun onStart() {
        super.onStart()
        if (App.INSTANCE.activityCount > 1) {
            FirebaseEventUtil.event("chest_hot")
        } else {
            FirebaseEventUtil.event("chest_cold")
        }

        if (SharePrefUtils.isFirstLaunch()) {
            FirebaseEventUtil.event("chest_first")
            SharePrefUtils.country = Locale.getDefault().country
        }

        Utils.log("firebase属性:country=${SharePrefUtils.country}")
        FirebaseEventUtil.setProperty(SharePrefUtils.country)

        AdManager.preload()

        binding.progressCircular.progress = 0
        job = lifecycleScope.launch {
            kotlin.runCatching {
                withTimeoutOrNull(14000) {
                    launch {
                        delay(2800)
                    }
                    launch {
                        var progress = 0
                        while (isActive && progress <= 60) {
                            binding.progressCircular.progress = progress
                            progress++
                            delay(47)
                        }
                    }
                    launch {
                        //加载广告
                        AdManager.request(AdPosition.LOADING)?.join()
                    }
                }
            }.onSuccess {
                var progress = 60
                while (progress <= 100) {
                    binding.progressCircular.progress = progress
                    progress++
                    delay(25)
                }

                //展示广告
                AdManager.show(AdPosition.LOADING, this@WelcomeActivity) {
                    if (App.INSTANCE.isFront) {
                        if (App.INSTANCE.activityCount == 1) {
                            createNewWebTab()
                            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                        }
                    }
                    finish()
                }
            }.onFailure {

            }
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }

    override fun onBackPressed() {
    }
}