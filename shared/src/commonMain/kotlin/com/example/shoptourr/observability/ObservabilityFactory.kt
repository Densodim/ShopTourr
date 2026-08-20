package com.example.shoptourr.observability

/**
 * Empty DSN stays no-op. A non-blank DSN uses [createPlatformObservability]:
 * Recording buffer on Android host tests, [IosSentryObservability] on iOS.
 * Android production still overrides with Sentry via `VoyageApp` extraModules.
 */
object ObservabilityFactory {
    fun create(dsn: String?): Observability =
        if (dsn.isNullOrBlank()) NoOpObservability else createPlatformObservability()
}

internal expect fun createPlatformObservability(): Observability
