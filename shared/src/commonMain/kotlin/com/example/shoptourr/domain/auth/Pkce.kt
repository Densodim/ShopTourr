package com.example.shoptourr.domain.auth

import com.example.shoptourr.data.hash.sha256Digest
import com.example.shoptourr.data.local.platformSecureRandomBytes

object Pkce {
    fun verifier(): String = base64UrlNoPad(platformSecureRandomBytes(32))

    fun challengeS256(verifier: String): String =
        base64UrlNoPad(sha256Digest(verifier.encodeToByteArray()))

    fun nonce(): String = toHex(platformSecureRandomBytes(32))

    fun toHex(bytes: ByteArray): String {
        val hex = "0123456789abcdef"
        return buildString(bytes.size * 2) {
            bytes.forEach { byte ->
                val value = byte.toInt() and 0xFF
                append(hex[value shr 4])
                append(hex[value and 0x0F])
            }
        }
    }

    fun base64UrlNoPad(bytes: ByteArray): String {
        val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        val out = StringBuilder((bytes.size + 2) / 3 * 4)
        var index = 0
        while (index < bytes.size) {
            val remaining = bytes.size - index
            val b0 = bytes[index].toInt() and 0xFF
            val b1 = if (remaining > 1) bytes[index + 1].toInt() and 0xFF else 0
            val b2 = if (remaining > 2) bytes[index + 2].toInt() and 0xFF else 0
            out.append(table[b0 shr 2])
            out.append(table[((b0 and 0x03) shl 4) or (b1 shr 4)])
            if (remaining > 1) {
                out.append(table[((b1 and 0x0F) shl 2) or (b2 shr 6)])
            }
            if (remaining > 2) {
                out.append(table[b2 and 0x3F])
            }
            index += 3
        }
        return out.toString()
    }
}
