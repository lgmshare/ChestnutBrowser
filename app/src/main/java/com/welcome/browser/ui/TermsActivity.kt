package com.welcome.browser.ui

import com.welcome.browser.databinding.TermsActivityBinding
import com.welcome.browser.ui.base.BaseActivity

class TermsActivity : BaseActivity<TermsActivityBinding>() {

    override fun onbBuildVB(): TermsActivityBinding {
        return TermsActivityBinding.inflate(layoutInflater)
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