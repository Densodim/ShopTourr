package com.example.shoptourr.observability

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ObservabilityFactoryTest {

    @Test
    fun `blank dsn stays noop`() {
        assertIs<NoOpObservability>(ObservabilityFactory.create(null))
        assertIs<NoOpObservability>(ObservabilityFactory.create("  "))
    }

    @Test
    fun `dsn enables recording buffer until sentry sdk is wired`() {
        val obs = ObservabilityFactory.create("https://key@o0.ingest.sentry.io/1")
        assertIs<RecordingObservability>(obs)
        obs.captureException(IllegalStateException("boom"))
        assertTrue(obs.exceptions.isNotEmpty())
    }
}
