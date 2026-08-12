package com.example.shoptourr.observability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservabilityTest {

    @Test
    fun `recording capture stores exception and extras`() {
        val obs = RecordingObservability()
        val error = IllegalStateException("boom")
        obs.captureException(error, mapOf("screen" to "home"))
        assertEquals(1, obs.exceptions.size)
        assertEquals(error, obs.exceptions.single().throwable)
        assertEquals("home", obs.exceptions.single().extras["screen"])
    }

    @Test
    fun `recording breadcrumb keeps category and request id`() {
        val obs = RecordingObservability()
        obs.addBreadcrumb(
            message = "http.request",
            category = "http",
            data = mapOf("request_id" to "abc-123"),
        )
        assertEquals(1, obs.breadcrumbs.size)
        assertEquals("http", obs.breadcrumbs.single().category)
        assertEquals("abc-123", obs.breadcrumbs.single().data["request_id"])
    }

    @Test
    fun `noop does not throw`() {
        NoOpObservability.captureException(RuntimeException("x"))
        NoOpObservability.addBreadcrumb("ok")
        NoOpObservability.setTag("env", "test")
        assertTrue(true)
    }
}
