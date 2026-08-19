package com.example.shoptourr.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class X509SpkiTest {

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `sha256 pin matches openssl spki digest`() {
        val der = Base64.decode(SAMPLE_CERT_DER_B64)
        assertEquals("sha256/$SAMPLE_SPKI_PIN", X509Spki.sha256Pin(der))
        assertTrue(
            X509Spki.matches(
                der,
                listOf(PublicKeyPin(SAMPLE_SPKI_PIN)),
            ),
        )
    }

    private companion object {
        const val SAMPLE_SPKI_PIN = "ufFCoBzxxERal+MoSFg90X5tjpcuMm7CzDOolD4NVEw="
        const val SAMPLE_CERT_DER_B64 =
            "MIIDDzCCAfegAwIBAgIUV7ePY9QcZe8jSdgWRJ1GGjRdNVUwDQYJKoZIhvcNAQELBQAwFzEVMBMGA1UEAwwMdGVzdC5leGFtcGxlMB4XDTI2MDgxOTEzMjcxOFoXDTI2MDgyMDEzMjcxOFowFzEVMBMGA1UEAwwMdGVzdC5leGFtcGxlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8URd5TdX7mJM3JBREKI8v5B+K2h0wAXO2hIjrHNTnog3OtA0G154ZfhwUARmFfBZVq4IrQwGhTqR7McHr4GO0veds8u8Q3Ypxu5/lMhAW7ea9lx5vWmz7flr9Lm88aghydqo0ksSvVcDBroi6j3qGl8uldmlxyhGV+wh6Me2Kf/FWh7uvgV8aGSB8yqxYW8nUrIvglA9Ey8Ht9wbpfCoWxUY7yU92R05Q+e+3PickrnUCVlf3ovneELYNVABk6kpzIDMJB75iGZkKjvawSCuUzfvuw/Tr75Ka4ag33yDTb1gew3NX5eF1pJ6PPwTyEsRaI+9VithHTbl1lcIV8YlswIDAQABo1MwUTAdBgNVHQ4EFgQUyslpc96DX46BlgYXI30mUlyqwNswHwYDVR0jBBgwFoAUyslpc96DX46BlgYXI30mUlyqwNswDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAGrR+fJgG8CYF6QFFWhUkFKK+2oWRPzinOU5q5mJyEqsgH55hckL8T31o2KGaYWl3mjdvRNUYlMWxHgpMG8vybXnupzMOlkINEEa6sC8LQelieyEIriRjIwGV5Ab3oRHUBXdpMM6Ux2geTEQHr3GkvGEoxW/uSUQyvHFVUz9vDzmTyIEDBVhk1e4rsaSwMGdYZ7Cgvej6o50q2+F2BGQPNi2PcsFnDLHtJg/3Ms60aCG3y6uAqjB/ufrMxWCiVVB3MM9dxFvCF1VUJYXs8pIHkUmavbAmpV+Rj3isSorzWLfBlcw5nt5fFWxbJvDeeHp3q0fl6nKy30zyi1u2wA2KzA=="
    }
}
