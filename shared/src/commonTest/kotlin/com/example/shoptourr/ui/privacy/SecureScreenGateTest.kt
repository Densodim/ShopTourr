package com.example.shoptourr.ui.privacy

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecureScreenGateTest {

    @Test
    fun `sensitive content hides while captured or in the app switcher`() {
        assertFalse(SecureScreenPolicy.hideSensitiveContent(enabled = false, screenCaptured = true, appInBackground = true))
        assertFalse(SecureScreenPolicy.hideSensitiveContent(enabled = true, screenCaptured = false, appInBackground = false))
        assertTrue(SecureScreenPolicy.hideSensitiveContent(enabled = true, screenCaptured = true, appInBackground = false))
        assertTrue(SecureScreenPolicy.hideSensitiveContent(enabled = true, screenCaptured = false, appInBackground = true))
    }

    @Test
    fun `nested holders keep the screen secure until the last one releases`() {
        val gate = SecureScreenGate()
        assertTrue(gate.acquire())
        assertFalse(gate.acquire())
        assertFalse(gate.release())
        assertTrue(gate.release())
        assertFalse(gate.release())
    }
}
