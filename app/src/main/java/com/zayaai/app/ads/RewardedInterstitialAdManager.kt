package com.zayaai.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * Manages Rewarded Interstitial Advertisements for AI ZAYA.
 * Used at natural transition points (e.g. after completing a quiz or study session)
 * with clear user notice before display.
 */
object RewardedInterstitialAdManager {

    private const val TAG = "RewardedInterstitial"
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isLoading = false

    fun load(context: Context, onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedInterstitialAd != null) {
            return
        }

        isLoading = true
        val request = AdRequest.Builder().build()
        val adUnitId = AdMobConfig.getRewardedInterstitialAdUnitId()

        RewardedInterstitialAd.load(
            context,
            adUnitId,
            request,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded Interstitial Ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Rewarded Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun isAdReady(): Boolean = rewardedInterstitialAd != null

    fun show(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        val ad = rewardedInterstitialAd
        if (ad == null) {
            onAdFailedToShow("Ad is not ready yet.")
            load(activity)
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null
                load(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedInterstitialAd = null
                load(activity)
                onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                // Ad displayed
            }
        }

        ad.show(activity) { rewardItem ->
            if (!rewardGranted) {
                rewardGranted = true
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward(rewardItem)
            }
        }
    }
}

