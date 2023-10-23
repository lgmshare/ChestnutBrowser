package com.welcome.browser.ad

import android.content.Context
import android.util.Base64
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.welcome.browser.App
import com.welcome.browser.R
import com.welcome.browser.ad.loader.AdmobLoader
import com.welcome.browser.databinding.LayoutAdBinding
import com.welcome.browser.utils.SharePrefUtils
import com.welcome.browser.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 适配：多平台配置｜多ID配置
 */
object AdManager {

    private val loadAdScope = MainScope()

    private var adIds: MutableList<AdId> = arrayListOf()
    private var maxShowCount = 1000
    private var maxClickCount = 1000
    private var showCount = 0 //应用级每日广告展示次数上限
    private var clickCount = 0 //应用级每日点击次数上限
    private var homeNativeAdShowTime: Long = 0
    private var tabNativeAdShowTime: Long = 0

    /**
     * 缓存池
     */
    private val nativeAdCache by lazy { AdCache() }
    private val interstitialAdCache by lazy { AdCache() }

    fun init(context: Context) {
        MobileAds.initialize(context)
        val date = SharePrefUtils.adDataDate
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Calendar.getInstance().time)
        Utils.log("$date  $today")
        if (date == today) {
            val showCount = SharePrefUtils.adShowCount
            val clickCount = SharePrefUtils.adClickCount
            setShowAndClick(showCount, clickCount)
        } else {
            setShowAndClick(0, 0)
            SharePrefUtils.adDataDate = today
            SharePrefUtils.adClickCount = 0
            SharePrefUtils.adShowCount = 0
        }
        try {
            //加载广告配置ID
            val encodeAdIdData = FirebaseRemoteConfig.getInstance().getString("browser_ad").ifEmpty {
                App.AD_CONFIG
            }
            val decodeAdIdData = String(Base64.decode(encodeAdIdData, Base64.DEFAULT), StandardCharsets.UTF_8)
            val adInfo = Gson().fromJson(decodeAdIdData, AdInfo::class.java)
            adInfo?.let {
                setAdIds(it.adIds)
                setMaxShowAndClick(it.maxShowCount, it.maxClickCount)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAdIds(list: List<AdId>?) {
        if (list != null) {
            this.adIds.clear()
            this.adIds.addAll(list)
        }
    }

    private fun setShowAndClick(showCount: Int, clickCount: Int) {
        this.showCount = showCount
        this.clickCount = clickCount
    }

    private fun setMaxShowAndClick(maxShowCount: Int, maxClickCount: Int) {
        this.maxShowCount = maxShowCount
        this.maxClickCount = maxClickCount
    }

    /**
     * 执行预加载
     */
    fun preload() {
        request(AdPosition.LOADING)
        request(AdPosition.HOME)
    }

    fun request(adPosition: AdPosition): Job? {
        Utils.log("------ REQUEST ------")
        val adCache = getPositionAdCache(adPosition)
        var job = adCache.job
        val caches = adCache.caches
        // 判断展示次数or点击次数是否达到设定限制
        if (isLimited(adPosition)) {
            Utils.logE("limited, done.")
            return null
        }

        // 如果在上次请求中，则不重新请求
        if (job?.isActive == true) {
            Utils.logE("has last loading, done.")
            return job
        }

        // 如果有缓存，则不重新请求
        if (caches.isNotEmpty()) {
            Utils.logE("$adPosition caches is not empty, done.")
            return null
        }
        val ids = getPositionIds(adPosition)
        if (ids.isEmpty()) {
            Utils.logE("$adPosition no ids config, done.$adIds")
            return null
        } else {
            Utils.log("$adPosition load ids config: $ids")
        }
        Utils.log("加载${if (ids[0].type == AdType.NATIVE) "原生广告" else "插屏广告"}")
        job = loadAdScope.launch {
            Utils.log("$adPosition start ids waterfall")
            // 缓存失败重试1次
            var retryCount = 1
            while (adCache.caches.isEmpty() && retryCount >= 0) {
                for (id in ids) {
                    Utils.log("$adPosition start load id: ${id.getIdAndSortKey()}")
                    val loader = when (id.platform) {
                        AdPlatform.PLATFORM_ADMOB -> AdmobLoader()
                        else -> AdmobLoader()
                    }
                    val adData = when (id.type) {
                        AdType.INTERSTITIAL -> loader.loadInterstitial(id)
                        AdType.NATIVE -> loader.loadNative(id)
                        AdType.OPEN -> loader.loadAppOpenAd(id)
                        else -> AdData(id, null, false, AdCode.NOT_SUPPORT_AD_TYPE, null)
                    }
                    if (adData.success && adData.ad != null) {
                        Utils.log("$adPosition succeed load id: ${id.getIdAndSortKey()}, result: ${adData.ad.hashCode()}")
                        caches.add(adData)
                        break
                    } else {
                        Utils.logE("$adPosition failed load id: ${id.getIdAndSortKey()}, code: ${adData.code}, msg: ${adData.msg}")
                    }
                }
                retryCount--
            }
            Utils.log("$adPosition end ids waterfall")
        }
        adCache.job = job
        return job
    }

    fun show(adPosition: AdPosition, activity: AppCompatActivity, adContainer: FrameLayout? = null, onClose: (() -> Unit)? = null) {
        // 判断当前页面是否是对应广告位页面
        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Utils.log("页面展示广告")
            when (adPosition) {
                AdPosition.LOADING -> {
                    showOpen(adPosition, activity, onClose)
                }
                AdPosition.CLEAN -> {
                    showInterstitial(adPosition, activity, onClose)
                }

                AdPosition.HOME, AdPosition.TAP -> {
                    if (adPosition == AdPosition.HOME) {
                        if (System.currentTimeMillis() - homeNativeAdShowTime >= 10 * 1000L) {
                            showNative(adPosition, activity, adContainer)
                        } else {
                            Utils.logE("HOME 页面展示广告时间小于10s")
                        }
                    } else {
                        if (System.currentTimeMillis() - tabNativeAdShowTime >= 10 * 1000L) {
                            showNative(adPosition, activity, adContainer)
                        } else {
                            Utils.logE("HOME 页面展示广告时间小于10s")
                        }
                    }
                }
            }
        } else {
            Utils.log("页面不可见无法展示")
            onClose?.invoke()
        }
    }

    private fun showOpen(adPosition: AdPosition, activity: AppCompatActivity, onClose: (() -> Unit)? = null) {
        val adData = get(adPosition)
        when (val ad = adData?.ad) {
            is AppOpenAd -> {
                Utils.log("展示开屏广告")
                onAdShow(adPosition)
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdClicked() {
                        onAdClick(adPosition)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        onClose?.invoke()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        onClose?.invoke()
                    }

                    override fun onAdImpression() {
                        onImpression(adPosition)
                    }

                    override fun onAdShowedFullScreenContent() {
                    }
                }
                ad.show(activity)
            }

            else -> {
                onClose?.invoke()
            }
        }
    }

    private fun showInterstitial(adPosition: AdPosition, activity: AppCompatActivity, onClose: (() -> Unit)? = null) {
        val adData = get(adPosition)
        when (val ad = adData?.ad) {
            is InterstitialAd -> {
                Utils.log("展示插屏广告")
                onAdShow(adPosition)
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdClicked() {
                        onAdClick(adPosition)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        onClose?.invoke()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        onClose?.invoke()
                    }

                    override fun onAdImpression() {
                        onImpression(adPosition)
                    }

                    override fun onAdShowedFullScreenContent() {
                    }
                }
                ad.show(activity)
            }

            else -> {
                onClose?.invoke()
            }
        }
    }

    private fun showNative(adPosition: AdPosition, activity: AppCompatActivity, adContainer: FrameLayout?) {
        val adData = get(adPosition)
        when (val ad = adData?.ad) {
            is NativeAd -> {
                Utils.log("展示原生广告")
                when (adPosition) {
                    AdPosition.TAP -> {
                        tabNativeAdShowTime = System.currentTimeMillis()
                        val adBinding = LayoutAdBinding.inflate(activity.layoutInflater)
                        showNativeAdView(ad, adBinding)
                        adBinding
                    }

                    AdPosition.HOME -> {
                        homeNativeAdShowTime = System.currentTimeMillis()
                        val adBinding = LayoutAdBinding.inflate(activity.layoutInflater)
                        showNativeAdView(ad, adBinding)
                        adBinding
                    }

                    else -> null
                }?.let { adBinding ->
                    onAdShow(adPosition)
                    onImpression(adPosition)
                    if (adContainer?.visibility == View.GONE) {
                        adContainer.visibility = View.VISIBLE
                    }
                    adContainer?.removeAllViews()
                    adContainer?.addView(adBinding.root)
                    adContainer?.setTag(R.id.native_ad_id, ad)
                }
            }

            else -> {

            }
        }
    }

    private fun showNativeAdView(nativeAd: NativeAd, adBinding: LayoutAdBinding) {
        val nativeAdView = adBinding.root

        nativeAdView.iconView = adBinding.adAppIcon
        nativeAdView.headlineView = adBinding.adHeadline
        nativeAdView.bodyView = adBinding.adBody
        nativeAdView.callToActionView = adBinding.adCallToAction

        if (nativeAd.icon == null) {
            adBinding.adAppIcon.visibility = View.GONE
        } else {
            adBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            adBinding.adAppIcon.visibility = View.VISIBLE
        }

        adBinding.adHeadline.text = nativeAd.headline

        if (nativeAd.body == null) {
            adBinding.adBody.visibility = View.INVISIBLE
        } else {
            adBinding.adBody.text = nativeAd.body
            adBinding.adBody.visibility = View.VISIBLE
        }

        if (nativeAd.callToAction == null) {
            adBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            adBinding.adCallToAction.visibility = View.VISIBLE
            adBinding.adCallToAction.text = nativeAd.callToAction
        }
        nativeAdView.setNativeAd(nativeAd)
    }

    private fun get(adPosition: AdPosition): AdData<*>? {
        Utils.log("------ GET ------")

        if (isLimited(adPosition)) {
            Utils.log("limited, done.")
            return null
        }

        val adCache = getPositionAdCache(adPosition)
        val caches = adCache.caches
        if (caches.isEmpty()) {
            Utils.log("caches is empty, done.")
            return null
        }
        return caches.minByOrNull { it.adId.level }.also {
            caches.remove(it)
            Utils.log("get ad from caches, ${it?.adId?.getIdAndSortKey()}, ${it?.ad?.hashCode()}")
            if (adPosition.preload && caches.isEmpty()) {
                request(adPosition)
            }
        }
    }

    fun onEnter(adPosition: AdPosition) {
    }

    fun onAdClick(adPosition: AdPosition) {
        clickCount++
        SharePrefUtils.adClickCount = clickCount
        Utils.log("广告页点击次数：$adPosition $clickCount")
    }

    fun onAdShow(adPosition: AdPosition) {
        showCount++
        SharePrefUtils.adShowCount = showCount
        Utils.log("广告页展示次数：$adPosition $showCount")
    }

    fun onImpression(adPosition: AdPosition) {
    }

    private fun getPositionIds(adPosition: AdPosition): List<AdId> {
        return adIds.filter { it.cache == adPosition.cache }.sortedBy { it.level }
    }

    private fun getPositionAdCache(adPosition: AdPosition): AdCache {
        return when (adPosition) {
            AdPosition.HOME, AdPosition.TAP -> {
                // 移除过期广告
                nativeAdCache.caches.removeIf { System.currentTimeMillis() - it.cacheTime >= 3000000 }
                nativeAdCache
            }

            else -> {
                // 移除过期广告
                interstitialAdCache.caches.removeIf { System.currentTimeMillis() - it.cacheTime >= 3000000 }
                interstitialAdCache
            }
        }
    }

    private fun isLimited(adPosition: AdPosition): Boolean {
        if (showCount >= maxShowCount || clickCount >= maxClickCount) {
            Utils.log("广告显示及点击次数限制 $maxShowCount-$showCount | $maxClickCount-$clickCount")
            return true
        }
        return false
    }
}