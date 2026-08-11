package com.example.shoptourr.data.hash

import com.example.shoptourr.domain.hash.ContentChecksum
import java.security.MessageDigest

actual fun createDefaultContentChecksum(): ContentChecksum = ContentChecksum { bytes ->
    MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { byte -> "%02x".format(byte) }
}
