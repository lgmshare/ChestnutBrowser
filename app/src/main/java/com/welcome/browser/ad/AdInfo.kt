package com.welcome.browser.ad

data class AdInfo(
    val maxShowCount: Int,
    val maxClickCount: Int,
    val adIds: List<AdId>,
)
