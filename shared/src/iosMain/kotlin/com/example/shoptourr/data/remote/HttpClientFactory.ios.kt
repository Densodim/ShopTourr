package com.example.shoptourr.data.remote

import com.example.shoptourr.security.CertificatePinConfig
import com.example.shoptourr.security.CertificatePinMatcher
import com.example.shoptourr.security.PublicKeyPin
import com.example.shoptourr.security.X509Spki
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateRef
import platform.Security.SecTrustGetCertificateAtIndex
import platform.Security.SecTrustRef
import platform.posix.memcpy

/**
 * Darwin pins the leaf certificate SPKI when [enforcePinning] is true.
 * Hosts without pins keep default ATS handling. Mismatch or missing leaf cancels the challenge.
 */
actual fun createPlatformHttpEngine(
    pinConfig: CertificatePinConfig,
    enforcePinning: Boolean,
): HttpClientEngine = Darwin.create {
    if (!enforcePinning || !pinConfig.hasPins) return@create
    handleChallenge { _, _, challenge, completionHandler ->
        if (challenge.protectionSpace.authenticationMethod != NSURLAuthenticationMethodServerTrust) {
            completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
            return@handleChallenge
        }
        val expected = CertificatePinMatcher.pinsForHost(challenge.protectionSpace.host, pinConfig)
        if (expected.isEmpty()) {
            completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
            return@handleChallenge
        }
        val trust = challenge.protectionSpace.serverTrust
        if (trust == null || !DarwinCertificatePins.matches(trust, expected)) {
            completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
            return@handleChallenge
        }
        completionHandler(
            NSURLSessionAuthChallengeUseCredential,
            NSURLCredential.credentialForTrust(trust),
        )
    }
}

internal object DarwinCertificatePins {
    fun matches(trust: SecTrustRef, expected: List<PublicKeyPin>): Boolean {
        val der = leafCertificateDer(trust) ?: return false
        return X509Spki.matches(der, expected)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun leafCertificateDer(trust: SecTrustRef): ByteArray? {
        val certificate = SecTrustGetCertificateAtIndex(trust, 0) ?: return null
        val data = SecCertificateCopyData(certificate as SecCertificateRef) ?: return null
        return (data as NSData).toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val count = length.toInt()
    if (count == 0) return ByteArray(0)
    val out = ByteArray(count)
    bytes?.let { pointer ->
        memcpy(out.refTo(0), pointer, length)
    }
    return out
}
