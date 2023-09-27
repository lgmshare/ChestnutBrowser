package com.welcome.browser.ui

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAd
import com.welcome.browser.App
import com.welcome.browser.BuildConfig
import com.welcome.browser.R
import com.welcome.browser.ad.AdManager
import com.welcome.browser.ad.AdPosition
import com.welcome.browser.constants.NAV_SITES
import com.welcome.browser.databinding.CleanDialogBinding
import com.welcome.browser.databinding.MainActivityBinding
import com.welcome.browser.databinding.SettingDialogBinding
import com.welcome.browser.extensions.copyToClipboard
import com.welcome.browser.extensions.hideSoftInput
import com.welcome.browser.extensions.jumpGooglePlayStore
import com.welcome.browser.extensions.jumpShare
import com.welcome.browser.extensions.setOnClick
import com.welcome.browser.extensions.toast
import com.welcome.browser.firebase.FirebaseEventUtil
import com.welcome.browser.model.WebLink
import com.welcome.browser.ui.adapter.WebNavAdapter
import com.welcome.browser.ui.base.BaseActivity
import com.welcome.browser.ui.tabs.WebLinksActivity
import com.welcome.browser.utils.Utils
import com.welcome.browser.web.WebManagers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : BaseActivity<MainActivityBinding>() {

    private var menuDialog: Dialog? = null

    override fun onbBuildVB(): MainActivityBinding {
        return MainActivityBinding.inflate(layoutInflater)
    }

    private val adapter by lazy { WebNavAdapter() }

    override fun onCreatePage() {
        binding.run {
            root.viewTreeObserver.addOnGlobalLayoutListener {
                updateWebPageDraw()
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener { }
            }

            searchView.addTextChangedListener {
                val inputText = binding.searchView.text.toString().trim()
                if (inputText.isEmpty()) {
                    btnSearch.isVisible = true
                    btnDelete.isVisible = false
                } else {
                    btnSearch.isVisible = false
                    btnDelete.isVisible = true
                }
            }

            searchView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    binding.searchView.text = SpannableStringBuilder("")
                    stopLoading()
                    WebManagers.currentWebLinks.webView.stopLoad()
                    setWebViewVisible(false)
                }
            }

            searchView.setOnEditorActionListener(object : OnEditorActionListener {
                override fun onEditorAction(textView: TextView, actionId: Int, p2: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        textView.hideSoftInput()
                        val inputText = binding.searchView.text.toString().trim()
                        if (inputText.isNullOrEmpty()) {
                            toast(R.string.please_enter_content)
                        } else {
                            FirebaseEventUtil.searchEvent(inputText)
                            startBrowser(inputText)
                        }
                        return true
                    }
                    return false
                }
            })

            setOnClick(btnDelete, btnBack, btnGo, btnClean, btnCount, btnSetting) {
                binding.searchView.hideSoftInput()
                when (this) {
                    btnDelete -> {
                        FirebaseEventUtil.event("chest_clean")
                        binding.searchView.setText("")
                    }

                    btnBack -> {
                        callBack()
                    }

                    btnGo -> {
                        callGo()
                    }

                    btnClean -> {
                        val viewBinding = CleanDialogBinding.inflate(layoutInflater)
                        viewBinding.btnConfirm.setOnClickListener {
                            startActivity(Intent(this@MainActivity, CleanActivity::class.java))
                        }
                        menuDialog = Dialog(this@MainActivity, R.style.ChestnutDialog)
                        menuDialog?.setContentView(viewBinding.root)
                        menuDialog?.window?.also {
                            it.attributes?.also { attr ->
                                attr.width = LayoutParams.MATCH_PARENT
                                attr.height = LayoutParams.WRAP_CONTENT
                                it.attributes = attr
                            }
                        }
                        menuDialog?.show()
                    }

                    btnCount -> {
                        startActivity(Intent(this@MainActivity, WebLinksActivity::class.java))
                    }

                    btnSetting -> {
                        val viewBinding = SettingDialogBinding.inflate(layoutInflater)
                        viewBinding.btnNew.setOnClickListener {
                            FirebaseEventUtil.newTabEvent("setting")
                            createNewWebTab()
                            menuDialog?.dismiss()
                        }
                        viewBinding.btnShare.setOnClickListener {
                            FirebaseEventUtil.event("chest_share")
                            val path = if (WebManagers.currentWebLinks.webView.isIdea || WebManagers.currentWebLinks.webView.isStopped) {
                                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                            } else {
                                WebManagers.currentWebLinks.webView.url ?: ""
                            }
                            jumpShare(path)
                            menuDialog?.dismiss()
                        }
                        viewBinding.btnCopy.setOnClickListener {
                            FirebaseEventUtil.event("chest_copy")
                            val path = if (WebManagers.currentWebLinks.webView.isIdea || WebManagers.currentWebLinks.webView.isStopped) {
                                ""
                            } else {
                                WebManagers.currentWebLinks.webView.url ?: ""
                            }
                            copyToClipboard(path)
                            menuDialog?.dismiss()
                        }
                        viewBinding.btnTermsUsers.setOnClickListener {
                            startActivity(Intent(this@MainActivity, TermsActivity::class.java))
                        }
                        viewBinding.btnRateUs.setOnClickListener {
                            jumpGooglePlayStore()
                            menuDialog?.dismiss()
                        }
                        viewBinding.btnPrivacyPolicy.setOnClickListener {
                            startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                        }
                        menuDialog = Dialog(this@MainActivity, R.style.ChestnutDialog)
                        menuDialog?.setContentView(viewBinding.root)
                        menuDialog?.window?.also {
                            it.attributes?.also { attr ->
                                attr.width = LayoutParams.MATCH_PARENT
                                attr.height = LayoutParams.WRAP_CONTENT
                                attr.gravity = Gravity.BOTTOM
                                it.attributes = attr
                            }
                        }
                        menuDialog?.show()
                    }
                }
            }
        }

        adapter.dataList.clear()
        adapter.dataList.addAll(NAV_SITES)
        adapter.itemClickCallback = { item, _ ->
            binding.searchView.hideSoftInput()
            startBrowser(item.ip)
            FirebaseEventUtil.newLinkEvent(item.name)
        }
        binding.navRecyclerView.layoutManager = GridLayoutManager(this, 4)
        binding.navRecyclerView.adapter = adapter

        WebManagers.chestnutWebListener = object : WebManagers.ChestnutWebListener {
            override fun onProgressChanged(progress: Int, index: Int) {
                if (WebManagers.currentWebLinks.index == index) {
                    binding.progressBar.isVisible = progress < 100
                    binding.progressBar.progress = progress
                    if (progress >= 100) {
                        setWebViewVisible(true)
                        if (binding.webContainer.childCount >= 1) {
                            val view = binding.webContainer.getChildAt(0)
                            if (view != WebManagers.currentWebLinks.webView) {
                                binding.webContainer.removeAllViews()
                                (WebManagers.currentWebLinks.webView?.parent as? ViewGroup)?.removeAllViews()
                                binding.webContainer.addView(WebManagers.currentWebLinks.webView)
                            }
                        } else {
                            binding.webContainer.removeAllViews()
                            (WebManagers.currentWebLinks.webView?.parent as? ViewGroup)?.removeAllViews()
                            binding.webContainer.addView(WebManagers.currentWebLinks.webView)
                        }

                        lifecycleScope.launch {
                            delay(1000)
                            Utils.log("网页快照")
                            val webView = WebManagers.currentWebLinks.webView
                            if (webView.isLaidOut) {
                                val bitmap = webView.drawToBitmap(Bitmap.Config.ARGB_8888)
                                WebManagers.updateCurrentWebBitMap(bitmap)
                            }
                        }

                        updateBottomTools()
                        binding.searchView.text = SpannableStringBuilder("")
                    }
                }
            }

            override fun addWeb(webLinks: WebLink) {
                stopLoading()
                updateBottomTools()
                binding.webContainer.removeAllViews()
                binding.webContainer.isVisible = false
                binding.tvCount.text = "${WebManagers.webPageLists.size}"
            }

            override fun removeWeb(webLinks: WebLink) {
                lifecycleScope.launchWhenResumed {
                    binding.tvCount.text = "${WebManagers.webPageLists.size}"
                    updateBottomTools()
                    if (WebManagers.currentWebLinks.webView.isLoadingFinish) {
                        binding.webContainer.removeAllViews()
                        binding.webContainer.addView(WebManagers.currentWebLinks.webView)
                        setWebViewVisible(true)
                        return@launchWhenResumed
                    }
                    if (WebManagers.currentWebLinks.webView.isLoading) {
                        binding.progressBar.isVisible = true
                        binding.searchView.text =
                            SpannableStringBuilder(WebManagers.currentWebLinks.inputText)
                        setWebViewVisible(false)
                        return@launchWhenResumed
                    }
                    if (WebManagers.currentWebLinks.webView.isStopped || WebManagers.currentWebLinks.webView.isIdea) {
                        setWebViewVisible(false)
                        binding.progressBar.isVisible = false
                        binding.searchView.text = SpannableStringBuilder("")
                    }

                }

            }

            override fun onWebChanged(webLinks: WebLink) {
                lifecycleScope.launchWhenResumed {
                    if (webLinks.webView.isLoadingFinish) {
                        setWebViewVisible(true)
                        binding.progressBar.isVisible = false
                        binding.webContainer.removeAllViews()
                        binding.webContainer.addView(webLinks.webView)
                    } else {
                        binding.searchView.text = SpannableStringBuilder(webLinks.inputText)
                        binding.progressBar.isVisible = webLinks.webView.isLoading
                        setWebViewVisible(false)
                    }
                    updateBottomTools()
                }
            }

            override fun clean() {
                lifecycleScope.launchWhenResumed {
                    WebManagers.currentWebLinks.webView.stopLoad()
                    WebManagers.currentWebLinks.webView.clearHistories()
                    setWebViewVisible(false)
                    updateBottomTools()
                    binding.progressBar.isVisible = false
                    binding.progressBar.progress = 0
                    binding.searchView.text = SpannableStringBuilder("")
                    WebManagers.currentWebLinks.inputText = ""
                    binding.tvCount.text = "${WebManagers.webPageLists.size}"
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if (App.INSTANCE.isFront) {
            AdManager.preload()
            FirebaseEventUtil.event("chest_show")
        }
    }

    override fun onStop() {
        super.onStop()
        menuDialog?.dismiss()
        menuDialog = null
    }

    override fun onResume() {
        super.onResume()
        if (!binding.webContainer.isVisible) {
            lifecycleScope.launchWhenResumed {
                AdManager.request(AdPosition.HOME)
                delay(300)
                AdManager.show(AdPosition.HOME, this@MainActivity, binding.adContainer)
            }
        }
    }

    private fun startBrowser(content: String) {
        FirebaseEventUtil.event("chest_newSearch")
        binding.searchView.hideSoftInput()
        binding.searchView.clearFocus()
        WebManagers.currentWebLinks.inputText = content
        WebManagers.currentWebLinks.webView.startLoad(content)
    }

    private fun setWebViewVisible(isVisible: Boolean) {
        binding.webContainer.isVisible = isVisible
        if (isVisible) {
            //销毁广告
            val ad = binding.adContainer.getTag(R.id.native_ad_id)
            if (ad is NativeAd) {
                Utils.log("销毁广告")
                ad.destroy()
            }
            binding.adContainer.setTag(R.id.native_ad_id, null)
            binding.adContainer.removeAllViews()
            binding.adContainer.isVisible = false
        } else {
            binding.webContainer.removeAllViews()
            lifecycleScope.launchWhenResumed {
                AdManager.request(AdPosition.HOME)
                delay(300)
                AdManager.show(AdPosition.HOME, this@MainActivity, binding.adContainer)
            }
        }
    }

    private fun stopLoading() {
        binding.progressBar.isVisible = false
        binding.searchView.text = SpannableStringBuilder("")
    }

    private fun updateBottomTools() {
        if (binding.webContainer.isVisible) {
            binding.btnBack.isEnabled = true
            binding.btnBack.setImageResource(R.mipmap.nav_back_b)
            if (WebManagers.currentWebLinks.webView.canGoForward()) {
                binding.btnGo.isEnabled = true
                binding.btnGo.setImageResource(R.mipmap.nav_go_b)
            } else {
                binding.btnGo.isEnabled = false
                binding.btnGo.setImageResource(R.mipmap.nav_go)
            }
        } else {
            WebManagers.currentWebLinks.webView.clearHistories()
            binding.btnBack.isEnabled = false
            binding.btnGo.isEnabled = false
            binding.webContainer.removeAllViews()
            binding.btnBack.setImageResource(R.mipmap.nav_back)
            binding.btnGo.setImageResource(R.mipmap.nav_go)
        }
    }

    private fun callBack() {
        if (WebManagers.currentWebLinks.webView.canGoBack()) {
            WebManagers.currentWebLinks.webView.goBack()
            return
        }
        WebManagers.currentWebLinks.webView.stopLoad()
        setWebViewVisible(false)
        stopLoading()
        updateBottomTools()
    }

    private fun callGo() {
        if (WebManagers.currentWebLinks.webView.canGoForward()) {
            WebManagers.currentWebLinks.webView.goForward()
        }
        updateBottomTools()
    }

    /**
     * 更新网页快照
     */
    private fun updateWebPageDraw() {
        WebManagers.updateCurrentWebBitMap(binding.root.drawToBitmap())
    }

    override fun onBackPressed() {
        if (binding.webContainer.isVisible) {
            binding.btnBack.callOnClick()
            return
        }
        moveTaskToBack(true)
    }
}