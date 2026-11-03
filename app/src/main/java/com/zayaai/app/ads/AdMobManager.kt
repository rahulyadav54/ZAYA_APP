package com.zayaai.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AdMob Initialization and User Messaging Platform (UMP) Consent Manager.
 * Safely initializes Google Mobile Ads SDK and requests user consent where required by GDPR/ePrivacy.
 */
object AdMobManager {

    private const val TAG = "AdMobManager"
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var isInitialized = false

    /**
     * Initialize AdMob and UMP Consent.
     * Can be called from Activity or Application.
     */
    fun initializeWithConsent(activity: Activity, onComplete: () -> Unit = {}) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.errorCode} - ${formError.message}")
                    }

                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                    onComplete()
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent info update failed: ${requestConsentError.errorCode} - ${requestConsentError.message}")
                if (consentInformation.canRequestAds()) {
                    initializeMobileAds(activity)
                }
                onComplete()
            }
        )

        // Check if consent has already been gathered previously
        if (consentInformation.canRequestAds()) {
            initializeMobileAds(activity)
        }
    }

    /**
     * Initializes the MobileAds SDK once per app lifecycle.
     */
    fun initializeMobileAds(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        try {
            // Background thread initialization for optimal app start performance
            Thread {
                MobileAds.initialize(context) { status ->
                    isInitialized = true
                    val adapterStatusMap = status.adapterStatusMap
                    for ((adapterClass, adapterStatus) in adapterStatusMap) {
                        Log.d(TAG, "Adapter: $adapterClass, State: ${adapterStatus.initializationState}")
                    }
                    Log.i(TAG, "AdMob MobileAds initialized successfully.")
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun isReady(): Boolean = isInitialized
}

