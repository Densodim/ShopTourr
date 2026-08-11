package com.example.shoptourr.data

import com.example.shoptourr.data.push.DevicePushTokenHolder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DevicePushTokenHolderTest {

    @BeforeTest
    fun reset() {
        DevicePushTokenHolder.update(null)
    }

    @Test
    fun `stores trimmed token`() {
        DevicePushTokenHolder.update("  abc123  ")
        assertEquals("abc123", DevicePushTokenHolder.token)
    }

    @Test
    fun `blank token becomes null`() {
        DevicePushTokenHolder.update("   ")
        assertNull(DevicePushTokenHolder.token)
    }
}
