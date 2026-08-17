package com.example.shoptourr.observability

/**
 * Named trace sections. Android forwards them to androidx.tracing, so they show up in
 * Perfetto / Studio system traces; iOS stays no-op until a signpost bridge lands.
 */
interface AppTracer {
    /** False when nobody is recording, so callers can skip building expensive labels. */
    val isEnabled: Boolean
    fun beginSection(label: String)
    fun endSection()
}

expect fun createDefaultTracer(): AppTracer

object NoOpTracer : AppTracer {
    override val isEnabled: Boolean get() = false
    override fun beginSection(label: String) = Unit
    override fun endSection() = Unit
}

class RecordingTracer : AppTracer {
    private val open = mutableListOf<String>()

    val sections = mutableListOf<String>()
    val openSections: List<String> get() = open.toList()

    override val isEnabled: Boolean get() = true

    override fun beginSection(label: String) {
        open += label
        sections += label
    }

    override fun endSection() {
        open.removeLastOrNull()
    }
}

/** Traces [block] as one section; the section closes even when [block] throws. */
inline fun <T> AppTracer.trace(label: String, block: () -> T): T {
    beginSection(label)
    return try {
        block()
    } finally {
        endSection()
    }
}
