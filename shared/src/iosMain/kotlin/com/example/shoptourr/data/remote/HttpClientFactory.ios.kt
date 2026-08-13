package com.example.shoptourr.data.remote

import com.example.shoptourr.security.CertificatePinConfig
import com.example.shoptourr.security.CertificatePinMatcher
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling

/**
 * Darwin engine consults [CertificatePinConfig] when [enforcePinning] is true.
 * Hosts without pins use default ATS handling. SPKI hash compare can replace
 * the default-handling branch once production pins are checked in.
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
        CertificatePinMatcher.pinsForHost(challenge.protectionSpace.host, pinConfig)
        completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
    }
}
