package com.zayaai.app.ads

import com.zayaai.app.BuildConfig

/**
 * AdMob Configuration for AI ZAYA.
 * Contains Official Ad Unit IDs from Google AdMob console:
 * - Rewarded: ca-app-pub-1411920894777921/1688794562
 * - App Open: ca-app-pub-1411920894777921/2890195598
 * - Rewarded Interstitial: ca-app-pub-1411920894777921/5062109543
 * - Banner: ca-app-pub-1411920894777921/6370453871
 *
 * Automatically switches to Google Official Test Ad Units in DEBUG builds
 * to protect the AdMob account against policy violations / invalid traffic during development.
 */
object AdMobConfig {

    // --- Official AI ZAYA Production AdMob Identifiers ---
    const val ADMOB_APP_ID = "ca-app-pub-1411920894777921~7775034889"

    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-1411920894777921/6370453871"
    const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-1411920894777921/2890195598"
    const val PROD_REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1411920894777921/5062109543"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-1411920894777921/1688794562"

    // --- Google Official Test Ad Unit IDs for Safe Testing ---
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Set to true if you want to test on device using real IDs in release mode
    var forceTestAds: Boolean = false

    fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG || forceTestAds) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
    }

    fun getAppOpenAdUnitId(): String {
        return if (BuildConfig.DEBUG || forceTestAds) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID
    }

    fun getRewardedInterstitialAdUnitId(): String {
        return if (BuildConfig.DEBUG || forceTestAds) TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID else PROD_REWARDED_INTERSTITIAL_AD_UNIT_ID
    }

    fun getRewardedAdUnitId(): String {
        return if (BuildConfig.DEBUG || forceTestAds) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
    }
}
