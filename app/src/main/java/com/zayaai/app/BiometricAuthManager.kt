package com.zayaai.app

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Extension to safely resolve FragmentActivity from Compose Context
 */
fun Context.findFragmentActivity(): FragmentActivity? {
    if (this is FragmentActivity) return this
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class BiometricAuthManager(private val context: Context) {

    private val tag = "BiometricAuthManager"

    /**
     * Check if the device has biometric hardware and enrolled credentials.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        
        // 1. Check strong biometric
        val strong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (strong == BiometricManager.BIOMETRIC_SUCCESS) return true

        // 2. Check weak / convenience biometric (optical under-display sensors on Motorola, Pixel, Samsung, etc.)
        val weak = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (weak == BiometricManager.BIOMETRIC_SUCCESS) return true

        // 3. Check strong with device credential
        val strongWithCred = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (strongWithCred == BiometricManager.BIOMETRIC_SUCCESS) return true

        // 4. Check device credential only
        val cred = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        if (cred == BiometricManager.BIOMETRIC_SUCCESS) return true

        return false
    }

    /**
     * Check if specifically biometric (fingerprint/face) is enrolled.
     */
    fun hasBiometricEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS ||
               biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show the system biometric prompt with robust fallbacks for all device manufacturers.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricManager = BiometricManager.from(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(tag, "onAuthenticationError: code=$errorCode, msg=$errString")
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED) {
                    onError("Cancelled")
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(tag, "onAuthenticationSucceeded")
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(tag, "onAuthenticationFailed: fingerprint not recognized, user can retry")
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        // Try Strategy 1: Standard biometric (Strong + Weak) with negative button
        // This is the most universal and reliable mode across all Android manufacturers.
        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("AI ZAYA Secure Access")
                .setSubtitle("Touch the fingerprint sensor to unlock")
                .setNegativeButtonText("Use PIN")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
            return
        } catch (e1: Exception) {
            Log.w(tag, "Strategy 1 failed: ${e1.message}")
        }

        // Try Strategy 2: Strong with Device Credential (no negative button allowed)
        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("AI ZAYA Secure Access")
                .setSubtitle("Touch the fingerprint sensor or verify identity")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
            return
        } catch (e2: Exception) {
            Log.w(tag, "Strategy 2 failed: ${e2.message}")
        }

        // Try Strategy 3: Default PromptInfo without explicit authenticators
        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("AI ZAYA Secure Access")
                .setSubtitle("Touch the fingerprint sensor to unlock")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
            return
        } catch (e3: Exception) {
            Log.e(tag, "All biometric prompt strategies failed: ${e3.message}")
            onError(e3.localizedMessage ?: "Unable to launch biometric authentication")
        }
    }
}