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
    fun `dsn enables a capturing observability`() {
        val obs = ObservabilityFactory.create("https://key@o0.ingest.sentry.io/1")
        assertTrue(obs !is NoOpObservability)
        obs.captureException(IllegalStateException("boom"))
    }
}
