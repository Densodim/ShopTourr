package com.example.shoptourr.observability

// androidx.tracing 2.0.0 publishes only androidJvm and jvm variants — no Apple klibs —
// so iOS keeps the no-op until an os_signpost actual is written.
actual fun createDefaultTracer(): AppTracer = NoOpTracer
