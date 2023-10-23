package com.welcome.browser.ad.loader

import com.welcome.browser.ad.AdData
import com.welcome.browser.ad.AdId

interface AdLoaderInterface {

    suspend fun loadNative(adId: AdId): AdData<*>

    suspend fun loadAppOpenAd(adId: AdId): AdData<*>

    suspend fun loadInterstitial(adId: AdId): AdData<*>

}