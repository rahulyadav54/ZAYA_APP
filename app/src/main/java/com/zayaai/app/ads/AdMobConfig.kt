package com.zayaai.app.ads

/**
 * AdMob Configuration for AI ZAYA.
 * Configured strictly with official PRODUCTION AdMob IDs to generate live revenue.
 */
object AdMobConfig {

    // --- Official AI ZAYA Production AdMob Identifiers ---
    const val ADMOB_APP_ID = "ca-app-pub-1411920894777921~7775034889"

    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-1411920894777921/6370453871"
    const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-1411920894777921/2890195598"
    const val PROD_REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1411920894777921/5062109543"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-1411920894777921/1688794562"

    // Set to true only if test ads are explicitly requested
    var forceTestAds: Boolean = false

    // Official Google Test Ad Units (fallback)
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    fun getBannerAdUnitId(): String {
        return if (forceTestAds) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
    }

    fun getAppOpenAdUnitId(): String {
        return if (forceTestAds) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID
    }

    fun getRewardedInterstitialAdUnitId(): String {
        return if (forceTestAds) TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID else PROD_REWARDED_INTERSTITIAL_AD_UNIT_ID
    }

    fun getRewardedAdUnitId(): String {
        return if (forceTestAds) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
    }
}
