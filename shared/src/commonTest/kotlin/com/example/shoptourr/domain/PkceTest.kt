package com.example.shoptourr.domain

import com.example.shoptourr.domain.auth.Pkce
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PkceTest {

    @Test
    fun `s256 challenge is url-safe base64 without padding`() {
        val challenge = Pkce.challengeS256("abc")
        assertFalse(challenge.contains('+'))
        assertFalse(challenge.contains('/'))
        assertFalse(challenge.contains('='))
        assertTrue(challenge.isNotBlank())
    }

    @Test
    fun `hex encoding is stable`() {
        assertEquals("0001ff", Pkce.toHex(byteArrayOf(0, 1, 0xFF.toByte())))
    }
}
