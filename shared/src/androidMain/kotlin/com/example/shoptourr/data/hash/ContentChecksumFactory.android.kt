package com.example.shoptourr.data.hash

import com.example.shoptourr.domain.hash.ContentChecksum
import java.security.MessageDigest

actual fun createDefaultContentChecksum(): ContentChecksum = ContentChecksum { bytes ->
    sha256Digest(bytes).joinToString("") { byte -> "%02x".format(byte) }
}

internal actual fun sha256Digest(bytes: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(bytes)
