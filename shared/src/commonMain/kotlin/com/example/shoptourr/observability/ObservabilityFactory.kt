package com.example.shoptourr.observability

/**
 * Sentry-ready factory: empty DSN stays no-op so release APK size stays under budget
 * until a real DSN is checked in. Non-blank DSN enables an in-process buffer that
 * the Sentry MP SDK can replace without changing call sites.
 */
object ObservabilityFactory {
    fun create(dsn: String?): Observability =
        if (dsn.isNullOrBlank()) NoOpObservability else RecordingObservability()
}
