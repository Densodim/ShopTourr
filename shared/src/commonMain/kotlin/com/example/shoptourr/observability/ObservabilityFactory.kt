package com.example.shoptourr.observability

/**
 * Empty DSN stays no-op. A non-blank DSN enables [RecordingObservability] in shared tests;
 * Android production overrides this with Sentry via `VoyageApp` extraModules.
 */
object ObservabilityFactory {
    fun create(dsn: String?): Observability =
        if (dsn.isNullOrBlank()) NoOpObservability else RecordingObservability()
}
