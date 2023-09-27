package com.welcome.browser.ui

import android.webkit.CookieManager
import androidx.lifecycle.lifecycleScope
import com.welcome.browser.R
import com.welcome.browser.ad.AdManager
import com.welcome.browser.ad.AdPosition
import com.welcome.browser.databinding.CleanActivityBinding
import com.welcome.browser.extensions.toast
import com.welcome.browser.firebase.FirebaseEventUtil
import com.welcome.browser.ui.base.BaseActivity
import com.welcome.browser.web.WebManagers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class CleanActivity : BaseActivity<CleanActivityBinding>() {

    private var job: Job? = null

    private var startTime = 0L

    override fun onbBuildVB(): CleanActivityBinding {
        return CleanActivityBinding.inflate(layoutInflater)
    }

    override fun onCreatePage() {
        startTime = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()

        job = lifecycleScope.launch {
            kotlin.runCatching {
                withTimeoutOrNull(14000) {
                    launch {
                        CookieManager.getInstance().removeAllCookies {
                        }
                        WebManagers.cleanAllWeb()
                    }

                    launch {
                        AdManager.request(AdPosition.CLEAN)?.join()
                    }

                    launch {
                        delay(2800)
                        FirebaseEventUtil.cleanEvent((System.currentTimeMillis() - startTime) / 1000)
                    }
                }
            }.onSuccess {
                toast(R.string.clean_successfully)
                FirebaseEventUtil.event("chest_clean_end")
                AdManager.show(AdPosition.CLEAN, this@CleanActivity) {
                    finish()
                }
            }.onFailure {
                finish()
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