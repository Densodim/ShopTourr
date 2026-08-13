package com.example.shoptourr.security

/**
 * SPKI public-key pin (base64 SHA-256), optionally already prefixed with `sha256/`.
 * Used by OkHttp [okhttp3.CertificatePinner] on Android release builds.
 */
data class PublicKeyPin(val sha256Base64: String) {
    init {
        require(sha256Base64.isNotBlank()) { "pin must not be blank" }
    }

    fun okHttpFormat(): String =
        if (sha256Base64.startsWith("sha256/")) sha256Base64 else "sha256/$sha256Base64"
}

data class HostPinSet(
    val host: String,
    val pins: List<PublicKeyPin>,
    val includeSubdomains: Boolean = false,
) {
    init {
        require(host.isNotBlank()) { "host must not be blank" }
    }
}

data class CertificatePinConfig(
    val hosts: List<HostPinSet> = emptyList(),
) {
    val hasPins: Boolean get() = hosts.any { it.pins.isNotEmpty() }

    companion object {
        val Empty = CertificatePinConfig()

        /** Production API host; pass real leaf/backup SPKI hashes before store release. */
        fun voyageApi(pins: List<String>): CertificatePinConfig =
            CertificatePinConfig(
                hosts = listOf(
                    HostPinSet(
                        host = "api.shoptourr.com",
                        pins = pins.map { raw ->
                            PublicKeyPin(raw.removePrefix("sha256/"))
                        },
                    ),
                ),
            )
    }
}

object CertificatePinPolicy {
    /** Pin only in release, and only when at least one host has pins. Fail-open otherwise. */
    fun shouldEnforce(isReleaseBuild: Boolean, config: CertificatePinConfig): Boolean =
        isReleaseBuild && config.hasPins
}

object CertificatePinMatcher {
    fun pinsForHost(hostname: String, config: CertificatePinConfig): List<PublicKeyPin> {
        for (host in config.hosts) {
            if (hostname.equals(host.host, ignoreCase = true)) return host.pins
            if (host.includeSubdomains && hostname.endsWith(".${host.host}", ignoreCase = true)) {
                return host.pins
            }
        }
        return emptyList()
    }
}

/** Default config until real SPKI hashes are checked in (empty → pinning off). */
object VoyageCertificatePins {
    val configured: CertificatePinConfig = CertificatePinConfig.Empty
}
