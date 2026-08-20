package com.example.shoptourr.data.lock

import com.example.shoptourr.domain.lock.BiometricAuthenticator
import com.example.shoptourr.domain.lock.BiometricAvailability
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorBiometryNotEnrolled
import platform.LocalAuthentication.LAErrorPasscodeNotSet
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun availability(): BiometricAvailability {
        val context = LAContext()
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val can = context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error.ptr)
            if (can) return BiometricAvailability.AVAILABLE
            return when (error.value?.code) {
                LAErrorBiometryNotEnrolled,
                LAErrorPasscodeNotSet,
                -> BiometricAvailability.NOT_ENROLLED
                else -> BiometricAvailability.UNAVAILABLE
            }
        }
    }

    override suspend fun authenticate(reason: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val context = LAContext()
            context.evaluatePolicy(LAPolicyDeviceOwnerAuthentication, reason) { success, _ ->
                if (continuation.isActive) continuation.resume(success)
            }
        }
}

actual fun createBiometricAuthenticator(): BiometricAuthenticator =
    IosBiometricAuthenticator()
