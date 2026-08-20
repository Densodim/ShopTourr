package com.example.shoptourr.observability

internal actual fun createPlatformObservability(): Observability = RecordingObservability()
