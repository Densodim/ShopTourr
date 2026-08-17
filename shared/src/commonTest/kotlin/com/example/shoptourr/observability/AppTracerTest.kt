package com.example.shoptourr.observability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppTracerTest {
    @Test
    fun traceRecordsSectionAndReturnsResult() {
        val tracer = RecordingTracer()

        val result = tracer.trace("load-trip") { 42 }

        assertEquals(42, result)
        assertEquals(listOf("load-trip"), tracer.sections)
        assertTrue(tracer.openSections.isEmpty())
    }

    @Test
    fun traceClosesSectionWhenBlockThrows() {
        val tracer = RecordingTracer()

        assertFailsWith<IllegalStateException> {
            tracer.trace("load-trip") { error("boom") }
        }

        assertEquals(listOf("load-trip"), tracer.sections)
        assertTrue(tracer.openSections.isEmpty())
    }

    @Test
    fun nestedSectionsCloseInOrder() {
        val tracer = RecordingTracer()

        tracer.trace("outer") {
            tracer.trace("inner") {
                assertEquals(listOf("outer", "inner"), tracer.openSections)
            }
            assertEquals(listOf("outer"), tracer.openSections)
        }

        assertEquals(listOf("outer", "inner"), tracer.sections)
        assertTrue(tracer.openSections.isEmpty())
    }

    @Test
    fun noOpTracerRecordsNothing() {
        assertFalse(NoOpTracer.isEnabled)

        val result = NoOpTracer.trace("load-trip") { "ok" }

        assertEquals("ok", result)
    }
}
