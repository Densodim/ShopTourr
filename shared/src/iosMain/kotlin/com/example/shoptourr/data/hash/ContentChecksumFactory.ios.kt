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
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    bytes.usePinned { pinned ->
        CC_SHA256(pinned.addressOf(0), bytes.size.toUInt(), digest.refTo(0))
    }
    digest.joinToString("") { byte ->
        byte.toString(16).padStart(2, '0')
    }
}
