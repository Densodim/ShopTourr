package com.example.shoptourr.data.remote

import com.example.shoptourr.security.CertificatePinConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner

/**
 * Darwin pins via Ktor's OkHttp-compatible [CertificatePinner] (SPKI sha256 hashes).
 * Hosts without pins keep default ATS handling.
 */
actual fun createPlatformHttpEngine(
    pinConfig: CertificatePinConfig,
    enforcePinning: Boolean,
): HttpClientEngine = Darwin.create {
    if (!enforcePinning || !pinConfig.hasPins) return@create
    val builder = CertificatePinner.Builder()
    for (host in pinConfig.hosts) {
        if (host.pins.isEmpty()) continue
        val formats = host.pins.map { it.okHttpFormat() }.toTypedArray()
        builder.add(host.host, *formats)
        if (host.includeSubdomains) {
            builder.add("**.${host.host}", *formats)
        }
    }
    handleChallenge(builder.build())
}
