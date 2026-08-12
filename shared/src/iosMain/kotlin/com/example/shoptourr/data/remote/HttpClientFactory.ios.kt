package com.example.shoptourr.data.remote

import com.example.shoptourr.security.CertificatePinConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/**
 * Darwin/NSURLSession SPKI pinning is not wired yet (TrustKit / custom delegate later).
 * [pinConfig] / [enforcePinning] are accepted for API parity with Android.
 */
actual fun createPlatformHttpEngine(
    pinConfig: CertificatePinConfig,
    enforcePinning: Boolean,
): HttpClientEngine = Darwin.create()
