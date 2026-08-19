package com.example.shoptourr.data.hash

import com.example.shoptourr.domain.hash.ContentChecksum
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun createDefaultContentChecksum(): ContentChecksum = ContentChecksum { bytes ->
    sha256Digest(bytes).joinToString("") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256Digest(bytes: ByteArray): ByteArray {
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    bytes.usePinned { pinned ->
        CC_SHA256(pinned.addressOf(0), bytes.size.toUInt(), digest.refTo(0))
    }
    return ByteArray(digest.size) { digest[it].toByte() }
}
