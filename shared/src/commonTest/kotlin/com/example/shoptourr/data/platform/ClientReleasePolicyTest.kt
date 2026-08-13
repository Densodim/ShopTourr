package com.example.shoptourr.data.platform

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientReleasePolicyTest {

    @Test
    fun `http logging is off in release builds`() {
        assertFalse(ClientReleasePolicy.enableHttpLogging(isReleaseBuild = true))
    }

    @Test
    fun `http logging is on in debug builds`() {
        assertTrue(ClientReleasePolicy.enableHttpLogging(isReleaseBuild = false))
    }
}
