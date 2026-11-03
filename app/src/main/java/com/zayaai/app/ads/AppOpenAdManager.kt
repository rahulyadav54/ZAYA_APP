package com.zayaai.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Manages Google AdMob App Open Advertisements.
 * Follows Google's official lifecycle and frequency guidelines:
 * - 4-hour expiration validation.
 * - Minimum cooldown between displays to avoid annoying users.
 * - Suppressed during splash screen, onboarding, or critical user flows.
 */
class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var lastShownTime: Long = 0

    private var currentActivity: Activity? = null

    // Suppress ads during initial onboarding, splash, or active AI streaming
    var isSuppressed: Boolean = false

    companion object {
        private const val TAG = "AppOpenAdManager"
        // Google AdMob recommended ad validity period (4 hours in millis)
        private const val FOUR_HOURS_MILLIS = 4 * 3600 * 1000L
        // Cooldown between app open ad impressions (at least 10 minutes)
        private const val COOLDOWN_MILLIS = 10 * 60 * 1000L

        @Volatile
        private var INSTANCE: AppOpenAdManager? = null

        fun getInstance(application: Application): AppOpenAdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppOpenAdManager(application).also {
                    INSTANCE = it
                    it.init()
                }
            }
        }
    }

    private fun init() {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadAd(application)
    }

    /**
     * Request an App Open Ad if not already loading or loaded.
     */
    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        val adUnitId = AdMobConfig.getAppOpenAdUnitId()

        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(TAG, "App Open Ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    appOpenAd = null
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Check if ad exists and was loaded within 4 hours.
     */
    private fun isAdAvailable(): Boolean {
        val wasLoadedWithinFourHours = (Date().time - loadTime) < FOUR_HOURS_MILLIS
        return appOpenAd != null && wasLoadedWithinFourHours
    }

    /**
     * Lifecycle observer method triggered when app enters foreground.
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showAdIfAvailable()
    }

    /**
     * Displays the App Open Ad if conditions are met:
     * - An activity is available
     * - Ad is not suppressed
     * - Cooldown period has passed
     * - Not already displaying
     */
    fun showAdIfAvailable(onShowComplete: () -> Unit = {}) {
        val activity = currentActivity
        if (activity == null || isShowingAd || isSuppressed) {
            onShowComplete()
            return
        }

        val currentTime = Date().time
        if ((currentTime - lastShownTime) < COOLDOWN_MILLIS) {
            // Respect user experience cooldown
            onShowComplete()
            return
        }

        if (!isAdAvailable()) {
            loadAd(activity)
            onShowComplete()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                lastShownTime = Date().time
                loadAd(activity)
                onShowComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
                onShowComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }

        appOpenAd?.show(activity)
    }

    // --- Activity Lifecycle Tracking ---
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}

