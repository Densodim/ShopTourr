package com.example.shoptourr.data.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.shoptourr.data.auth.AndroidAuthHost
import com.example.shoptourr.domain.lock.BiometricAuthenticator
import com.example.shoptourr.domain.lock.BiometricAvailability
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

class AndroidBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun availability(): BiometricAvailability {
        val activity = AndroidAuthHost.currentActivity() ?: return BiometricAvailability.UNAVAILABLE
        return when (BiometricManager.from(activity).canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            else -> BiometricAvailability.UNAVAILABLE
        }
    }

    override suspend fun authenticate(reason: String): Boolean {
        val activity = AndroidAuthHost.currentActivity() as? FragmentActivity ?: return false
        return suspendCancellableCoroutine { continuation ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                },
            )
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(reason)
                    .setAllowedAuthenticators(AUTHENTICATORS)
                    .build(),
            )
        }
    }
}

actual fun createBiometricAuthenticator(): BiometricAuthenticator =
    AndroidBiometricAuthenticator()
