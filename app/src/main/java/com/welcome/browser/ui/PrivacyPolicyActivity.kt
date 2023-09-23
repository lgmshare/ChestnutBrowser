package com.welcome.browser.ui

import com.welcome.browser.databinding.PolicyActivityBinding
import com.welcome.browser.ui.base.BaseActivity

class PrivacyPolicyActivity : BaseActivity<PolicyActivityBinding>() {

    override fun onbBuildVB(): PolicyActivityBinding {
        return PolicyActivityBinding.inflate(layoutInflater)
    }

    override fun onCreatePage() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
}