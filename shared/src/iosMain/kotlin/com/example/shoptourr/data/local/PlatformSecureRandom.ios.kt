package com.example.shoptourr.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual fun platformSecureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    val status = bytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, size.convert(), pinned.addressOf(0))
    }
    check(status == errSecSuccess) { "SecRandomCopyBytes failed" }
    return bytes
}
