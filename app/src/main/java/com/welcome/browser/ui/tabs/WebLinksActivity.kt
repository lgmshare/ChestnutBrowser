package com.welcome.browser.ui.tabs

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.welcome.browser.ad.AdManager
import com.welcome.browser.ad.AdPosition
import com.welcome.browser.databinding.WebLinksActivityBinding
import com.welcome.browser.extensions.setOnClick
import com.welcome.browser.firebase.FirebaseEventUtil
import com.welcome.browser.model.WebLink
import com.welcome.browser.ui.adapter.WebLinkAdapter
import com.welcome.browser.ui.base.BaseActivity
import com.welcome.browser.web.WebManagers
import kotlinx.coroutines.delay

class WebLinksActivity : BaseActivity<WebLinksActivityBinding>() {

    override fun onbBuildVB(): WebLinksActivityBinding {
        return WebLinksActivityBinding.inflate(layoutInflater)
    }

    private val adapter by lazy { WebLinkAdapter() }

    override fun onCreatePage() {
        FirebaseEventUtil.event("chest_showTab")
        binding.run {
            setOnClick(btnBack, btnAdd) {
                when (this) {
                    btnAdd -> {
                        FirebaseEventUtil.newTabEvent("tab")
                        createNewWebTab()
                        finish()
                    }

                    btnBack -> {
                        finish()
                    }
                }
            }
        }

        adapter.itemClickCallback = { item, position ->
            WebManagers.currentWebLinks = item
            WebManagers.chestnutWebListener?.onWebChanged(item)
            finish()
        }

        adapter.itemDeleteClickCallback = { item, position ->
            remoteWeb(item, position)
        }
        binding.tabsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.tabsRecyclerView.adapter = adapter

        updateData()
    }

    private fun remoteWeb(link: WebLink, position: Int) {
        WebManagers.removeWebLinks(link)
        if (WebManagers.currentWebLinks == link) {
            WebManagers.currentWebLinks = WebManagers.getMinWebLinks()
        }
        WebManagers.chestnutWebListener?.removeWeb(link)

        updateData()
        adapter.notifyDataSetChanged()
    }

    private fun updateData() {
        val list = WebManagers.webPageLists.sortedByDescending { it.index }
        adapter.dataList.clear()
        adapter.dataList.addAll(list)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            AdManager.request(AdPosition.TAP)
            delay(300)
            AdManager.show(AdPosition.TAP, this@WebLinksActivity, binding.adContainer)
        }
    }
}