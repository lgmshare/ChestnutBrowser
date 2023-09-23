package com.welcome.browser.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.welcome.browser.model.WebLink
import com.welcome.browser.views.ChestnutWeb
import com.welcome.browser.web.WebManagers

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB

    abstract fun onbBuildVB(): VB

    abstract fun onCreatePage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = onbBuildVB()
        setContentView(binding.root)
        onCreatePage()
    }

    fun createNewWebTab() {
        WebManagers.addWeb(WebLink("", null, WebManagers.getMaxWeb() + 1, ChestnutWeb()))
    }
}