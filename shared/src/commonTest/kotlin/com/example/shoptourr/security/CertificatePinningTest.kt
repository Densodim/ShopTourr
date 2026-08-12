package com.example.shoptourr.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CertificatePinningTest {

    @Test
    fun `policy disables pinning on debug builds even with pins`() {
        val config = CertificatePinConfig.voyageApi(
            pins = listOf("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="),
        )
        assertFalse(CertificatePinPolicy.shouldEnforce(isReleaseBuild = false, config = config))
    }

    @Test
    fun `policy disables pinning when release but no pins configured`() {
        assertFalse(
            CertificatePinPolicy.shouldEnforce(
                isReleaseBuild = true,
                config = CertificatePinConfig.Empty,
            ),
        )
    }

    @Test
    fun `policy enables pinning on release when host pins exist`() {
        val config = CertificatePinConfig.voyageApi(
            pins = listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="),
        )
        assertTrue(CertificatePinPolicy.shouldEnforce(isReleaseBuild = true, config = config))
    }

    @Test
    fun `okHttpFormat prefixes sha256 when missing`() {
        val pin = PublicKeyPin("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        assertEquals(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
            pin.okHttpFormat(),
        )
    }

    @Test
    fun `okHttpFormat keeps existing sha256 prefix`() {
        val pin = PublicKeyPin("sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
        assertEquals(
            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=",
            pin.okHttpFormat(),
        )
    }

    @Test
    fun `voyageApi targets api shoptourr host`() {
        val config = CertificatePinConfig.voyageApi(
            pins = listOf("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="),
        )
        assertEquals(1, config.hosts.size)
        assertEquals("api.shoptourr.com", config.hosts.single().host)
        assertEquals(1, config.hosts.single().pins.size)
    }
}
