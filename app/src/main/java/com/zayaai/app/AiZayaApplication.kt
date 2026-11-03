package com.zayaai.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.zayaai.app.ads.AdMobManager
import com.zayaai.app.ads.AppOpenAdManager
import com.zayaai.app.ads.RewardedAdManager

/**
 * Main Application class for AI ZAYA.
 * Initializes Firebase, AdMob, and lifecycle ad managers cleanly.
 */
class AiZayaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Google Mobile Ads SDK (AdMob)
        AdMobManager.initializeMobileAds(this)

        // Initialize App Open Ad Manager with lifecycle observer
        AppOpenAdManager.getInstance(this)

        // Preload rewarded ads for smooth user experience
        RewardedAdManager.load(this)
    }
}

