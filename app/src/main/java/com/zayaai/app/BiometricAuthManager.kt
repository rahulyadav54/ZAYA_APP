package com.zayaai.app

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Extension to safely resolve FragmentActivity from Compose Context
 */
fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class BiometricAuthManager(private val context: Context) {

    private val strongWithCredential = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    private val weakWithCredential = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * Check if the device can authenticate with biometric OR device credential (screen lock).
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(strongWithCredential) == BiometricManager.BIOMETRIC_SUCCESS) return true
        if (biometricManager.canAuthenticate(weakWithCredential) == BiometricManager.BIOMETRIC_SUCCESS) return true
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) return true
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) return true
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
     * Show the system biometric prompt.
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
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onError here  —  the system prompt handles retry automatically
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("AI ZAYA Secure Access")
            .setSubtitle("Verify your identity to unlock")

        // Determine best supported authenticators
        if (biometricManager.canAuthenticate(strongWithCredential) == BiometricManager.BIOMETRIC_SUCCESS) {
            builder.setAllowedAuthenticators(strongWithCredential)
        } else if (biometricManager.canAuthenticate(weakWithCredential) == BiometricManager.BIOMETRIC_SUCCESS) {
            builder.setAllowedAuthenticators(weakWithCredential)
        } else {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG)
            builder.setNegativeButtonText("Cancel")
        }

        try {
            biometricPrompt.authenticate(builder.build())
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Authentication failed to start")
        }
    }
}
