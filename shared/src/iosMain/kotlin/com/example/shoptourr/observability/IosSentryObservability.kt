package com.example.shoptourr.observability

/**
 * Swift [SentrySDK] hooks. [iOSApp] sets these after `SentrySDK.start` so
 * Kotlin [IosSentryObservability] can forward crashes without cocoapods.
 */
fun interface IosSentryExceptionCapture {
    fun capture(message: String, stack: String)
}

fun interface IosSentryBreadcrumbCapture {
    fun capture(message: String, category: String)
}

fun interface IosSentryTagSetter {
    fun set(key: String, value: String)
}

object IosSentryBridge {
    var exceptionCapture: IosSentryExceptionCapture? = null
    var breadcrumbCapture: IosSentryBreadcrumbCapture? = null
    var tagSetter: IosSentryTagSetter? = null
}

/** Swift cannot construct Kotlin fun interfaces; pass closures instead. */
fun bindIosSentryHooks(
    onException: (message: String, stack: String) -> Unit,
    onBreadcrumb: (message: String, category: String) -> Unit,
    onTag: (key: String, value: String) -> Unit,
) {
    IosSentryBridge.exceptionCapture = IosSentryExceptionCapture { message, stack ->
        onException(message, stack)
    }
    IosSentryBridge.breadcrumbCapture = IosSentryBreadcrumbCapture { message, category ->
        onBreadcrumb(message, category)
    }
    IosSentryBridge.tagSetter = IosSentryTagSetter { key, value -> onTag(key, value) }
}

class IosSentryObservability : Observability {
    override fun captureException(throwable: Throwable, extras: Map<String, String>) {
        val extraLines = extras.entries.joinToString("\n") { (key, value) -> "$key=$value" }
        val stack = buildString {
            if (extraLines.isNotEmpty()) {
                append(extraLines)
                append('\n')
            }
            append(throwable.stackTraceToString())
        }
        IosSentryBridge.exceptionCapture?.capture(
            throwable.message ?: throwable::class.simpleName.orEmpty(),
            stack,
        )
    }

    override fun addBreadcrumb(message: String, category: String, data: Map<String, String>) {
        val suffix = if (data.isEmpty()) "" else " " + data.entries.joinToString { "${it.key}=${it.value}" }
        IosSentryBridge.breadcrumbCapture?.capture(message + suffix, category)
    }

    override fun setTag(key: String, value: String) {
        IosSentryBridge.tagSetter?.set(key, value)
    }
}
