package com.example.shoptourr.data.local

import java.security.SecureRandom

actual fun platformSecureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    SecureRandom().nextBytes(bytes)
    return bytes
}
