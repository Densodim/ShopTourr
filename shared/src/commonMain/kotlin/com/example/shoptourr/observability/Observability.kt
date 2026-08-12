package com.example.shoptourr.observability

/**
 * Crash / breadcrumb facade. Default is [NoOpObservability];
 * swap for Sentry Kotlin MP when DSN is configured.
 */
interface Observability {
    fun captureException(throwable: Throwable, extras: Map<String, String> = emptyMap())
    fun addBreadcrumb(
        message: String,
        category: String = "app",
        data: Map<String, String> = emptyMap(),
    )
    fun setTag(key: String, value: String)
}

object NoOpObservability : Observability {
    override fun captureException(throwable: Throwable, extras: Map<String, String>) = Unit
    override fun addBreadcrumb(message: String, category: String, data: Map<String, String>) = Unit
    override fun setTag(key: String, value: String) = Unit
}

data class CapturedException(
    val throwable: Throwable,
    val extras: Map<String, String>,
)

data class CapturedBreadcrumb(
    val message: String,
    val category: String,
    val data: Map<String, String>,
)

class RecordingObservability : Observability {
    val exceptions = mutableListOf<CapturedException>()
    val breadcrumbs = mutableListOf<CapturedBreadcrumb>()
    val tags = mutableMapOf<String, String>()

    override fun captureException(throwable: Throwable, extras: Map<String, String>) {
        exceptions += CapturedException(throwable, extras)
    }

    override fun addBreadcrumb(message: String, category: String, data: Map<String, String>) {
        breadcrumbs += CapturedBreadcrumb(message, category, data)
    }

    override fun setTag(key: String, value: String) {
        tags[key] = value
    }
}
