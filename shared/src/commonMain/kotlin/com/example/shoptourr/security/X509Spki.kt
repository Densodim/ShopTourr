package com.example.shoptourr.security

import com.example.shoptourr.data.hash.sha256Digest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Extracts SubjectPublicKeyInfo from an X.509 certificate DER and hashes it the same
 * way OkHttp [okhttp3.CertificatePinner] does (`SHA-256` of `PublicKey.getEncoded()`).
 */
object X509Spki {
    fun sha256Pin(certDer: ByteArray): String {
        val digest = sha256Digest(extract(certDer))
        @OptIn(ExperimentalEncodingApi::class)
        return "sha256/${Base64.encode(digest)}"
    }

    fun matches(certDer: ByteArray, expected: List<PublicKeyPin>): Boolean {
        val pin = sha256Pin(certDer)
        return expected.any { it.okHttpFormat() == pin }
    }

    internal fun extract(certDer: ByteArray): ByteArray {
        val certificate = Der.read(certDer, 0)
        require(certificate.tag == 0x30) { "certificate must be a SEQUENCE" }
        val tbs = Der.read(certificate.content, 0)
        require(tbs.tag == 0x30) { "tbsCertificate must be a SEQUENCE" }
        var offset = 0
        val first = Der.read(tbs.content, 0)
        if (first.tag == 0xA0) {
            offset = first.end
        }
        repeat(5) {
            offset = Der.read(tbs.content, offset).end
        }
        val spki = Der.read(tbs.content, offset)
        require(spki.tag == 0x30) { "subjectPublicKeyInfo must be a SEQUENCE" }
        return tbs.content.copyOfRange(spki.start, spki.end)
    }
}

private data class DerElement(
    val tag: Int,
    val start: Int,
    val end: Int,
    val content: ByteArray,
)

private object Der {
    fun read(buffer: ByteArray, offset: Int): DerElement {
        require(offset in buffer.indices) { "DER offset out of range" }
        val tag = buffer[offset].toInt() and 0xFF
        var cursor = offset + 1
        val lengthByte = buffer[cursor].toInt() and 0xFF
        cursor += 1
        val length = if (lengthByte < 0x80) {
            lengthByte
        } else {
            val count = lengthByte and 0x7F
            require(count in 1..3) { "unsupported DER length" }
            var value = 0
            repeat(count) {
                value = (value shl 8) or (buffer[cursor].toInt() and 0xFF)
                cursor += 1
            }
            value
        }
        val contentStart = cursor
        val contentEnd = contentStart + length
        require(contentEnd <= buffer.size) { "truncated DER" }
        return DerElement(
            tag = tag,
            start = offset,
            end = contentEnd,
            content = buffer.copyOfRange(contentStart, contentEnd),
        )
    }
}
