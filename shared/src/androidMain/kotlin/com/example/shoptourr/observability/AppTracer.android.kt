package com.example.shoptourr.observability

import androidx.tracing.Trace

actual fun createDefaultTracer(): AppTracer = AndroidTracer

/**
 * androidx.tracing 2.0.0. Stays on the `android.os.Trace` bridge rather than the new
 * in-process [androidx.tracing.Tracer]: those buffers are process-owned and are not picked
 * up by Studio / Perfetto system recordings yet.
 */
internal object AndroidTracer : AppTracer {
    // Mirrors the internal Trace.MAX_TRACE_LABEL_LENGTH; android.os.Trace rejects longer labels.
    private const val MAX_LABEL_LENGTH = 127

    override val isEnabled: Boolean get() = Trace.isEnabled()

    override fun beginSection(label: String) {
        Trace.beginSection(label.take(MAX_LABEL_LENGTH))
    }

    override fun endSection() {
        Trace.endSection()
    }
}
