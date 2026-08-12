package com.example.shoptourr.data.remote

import com.example.shoptourr.security.CertificatePinConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner

actual fun createPlatformHttpEngine(
    pinConfig: CertificatePinConfig,
    enforcePinning: Boolean,
): HttpClientEngine = OkHttp.create {
    if (enforcePinning && pinConfig.hasPins) {
        val builder = CertificatePinner.Builder()
        for (host in pinConfig.hosts) {
            if (host.pins.isEmpty()) continue
            val formats = host.pins.map { it.okHttpFormat() }.toTypedArray()
            builder.add(host.host, *formats)
            if (host.includeSubdomains) {
                builder.add("**.${host.host}", *formats)
            }
        }
        val pinner = builder.build()
        config {
            certificatePinner(pinner)
        }
    }
}
