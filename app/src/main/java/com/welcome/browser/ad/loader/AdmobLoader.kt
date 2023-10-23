package com.welcome.browser.ad.loader

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.welcome.browser.App
import com.welcome.browser.ad.AdData
import com.welcome.browser.ad.AdId
import com.welcome.browser.ad.AdManager
import com.welcome.browser.ad.AdPosition
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @Author leo
 * @Date 2023/3/20
 */
class AdmobLoader : AdLoaderInterface {

    override suspend fun loadNative(adId: AdId): AdData<*> {
        return suspendCancellableCoroutine { callback ->
            AdLoader.Builder(App.INSTANCE, adId.id).forNativeAd { nativeAd ->
                callback.resume(AdData(adId, nativeAd, true, null, null)) {}
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    callback.resume(AdData(adId, null, false, adError.code, adError.message)) {}
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdManager.onAdClick(AdPosition.HOME)
                }
            }).build().loadAd(AdRequest.Builder().build())
        }
    }

    override suspend fun loadAppOpenAd(adId: AdId): AdData<*> {
        return suspendCancellableCoroutine { callback ->
            val request = AdRequest.Builder().build()
            AppOpenAd.load(App.INSTANCE, adId.id, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        callback.resume(AdData(adId, ad, true, null, null)) {}
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        callback.resume(AdData(adId, null, false, adError.code, adError.message)) {}
                    }
                })
        }
    }

    override suspend fun loadInterstitial(adId: AdId): AdData<*> {
        return suspendCancellableCoroutine { callback ->
            InterstitialAd.load(App.INSTANCE, adId.id, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    callback.resume(AdData(adId, interstitialAd, true, null, null)) {}
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    callback.resume(AdData(adId, null, false, adError.code, adError.message)) {}
                }
            })
        }
    }
}