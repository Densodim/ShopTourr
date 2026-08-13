package com.example.shoptourr.data.local

import com.example.shoptourr.data.settings.SecureKeyValueStore

object DatabaseEncryptionKey {
    const val STORE_KEY = "db.sqlcipher.passphrase"
    const val BYTE_COUNT = 32

    fun getOrCreate(
        store: SecureKeyValueStore,
        randomBytes: () -> ByteArray = { platformSecureRandomBytes(BYTE_COUNT) },
    ): String {
        val existing = store.getString(STORE_KEY)
        if (existing != null && isValid(existing)) return existing
        val generated = toHex(randomBytes().also { require(it.size == BYTE_COUNT) })
        store.putString(STORE_KEY, generated)
        return generated
    }

    fun isValid(value: String): Boolean =
        value.length == BYTE_COUNT * 2 &&
            value.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

    private fun toHex(bytes: ByteArray): String = buildString(bytes.size * 2) {
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            append(HEX[v ushr 4])
            append(HEX[v and 0x0F])
        }
    }

    private const val HEX = "0123456789abcdef"
}
