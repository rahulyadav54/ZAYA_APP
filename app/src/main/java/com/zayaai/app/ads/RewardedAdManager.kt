package com.zayaai.app.ads
import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manages Optional Rewarded Advertisements for AI ZAYA.
 * Allows users to voluntarily watch an advertisement to unlock rewards
 * such as extra AI usage or study tools hints.
 */
object RewardedAdManager {

    private const val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun load(context: Context, onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedAd != null) {
            return
        }

        isLoading = true
        val request = AdRequest.Builder().build()
        val adUnitId = AdMobConfig.getRewardedAdUnitId()

        RewardedAd.load(
            context,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded Ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun isAdReady(): Boolean = rewardedAd != null

    /**
     * Displays the Rewarded Ad if loaded.
     * Prevents duplicate rewards and manages clean disposal.
     */
    fun show(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onAdFailedToShow("Ad is not ready yet. Please try again in a moment.")
            load(activity)
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                // Preload next ad
                load(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
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

