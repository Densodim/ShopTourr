package com.example.shoptourr.domain.hash

fun interface ContentChecksum {
    fun sha256Hex(bytes: ByteArray): String
}
