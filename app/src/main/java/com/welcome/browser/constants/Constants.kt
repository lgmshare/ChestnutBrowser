package com.welcome.browser.constants

import com.welcome.browser.R
import com.welcome.browser.model.WebNav

val NAV_SITES = mutableListOf<WebNav>().apply {
    add(WebNav(0, "facebook", "Facebook", "https://www.facebook.com", R.mipmap.ic_site_fb))
    add(WebNav(1, "google", "Google", "https://www.google.com", R.mipmap.ic_site_gg))
    add(WebNav(2, "youtube", "Youtube", "https://www.youtube.com", R.mipmap.ic_site_yb))
    add(WebNav(3, "twitter", "Twitter", "https://www.twitter.com", R.mipmap.ic_site_tw))
    add(WebNav(4, "instagram", "Instagram", "https://www.instagram.com", R.mipmap.ic_site_ins))
    add(WebNav(5, "amazon", "Amazon", "https://www.amazon.com", R.mipmap.ic_site_am))
    add(WebNav(6, "tiktok", "Tiktok", "https://www.tiktok.com", R.mipmap.ic_site_tiktok))
    add(WebNav(7, "yahoo", "Yahoo", "https://www.yahoo.com", R.mipmap.ic_site_yh))
}