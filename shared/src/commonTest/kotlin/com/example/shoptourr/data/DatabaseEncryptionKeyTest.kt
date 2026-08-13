package com.example.shoptourr.data

import com.example.shoptourr.data.local.DatabaseEncryptionKey
import com.example.shoptourr.data.local.EncryptedDatabaseOpenPlan
import com.example.shoptourr.data.local.EncryptedDatabasePolicy
import com.example.shoptourr.data.settings.InMemorySecureKeyValueStore
import com.example.shoptourr.data.settings.SecureTokenStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseEncryptionKeyTest {

    @Test
    fun `generates 64 lowercase hex chars and persists`() {
        val store = InMemorySecureKeyValueStore()
        val bytes = ByteArray(32) { index -> index.toByte() }

        val key = DatabaseEncryptionKey.getOrCreate(store) { bytes }

        assertEquals(64, key.length)
        assertTrue(key.matches(Regex("[0-9a-f]{64}")))
        assertEquals(key, store.getString(DatabaseEncryptionKey.STORE_KEY))
        assertEquals("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", key)
    }

    @Test
    fun `second call returns the same stored key`() {
        val store = InMemorySecureKeyValueStore()
        var calls = 0
        val first = DatabaseEncryptionKey.getOrCreate(store) {
            calls++
            ByteArray(32) { 1 }
        }
        val second = DatabaseEncryptionKey.getOrCreate(store) {
            calls++
            ByteArray(32) { 2 }
        }
        assertEquals(first, second)
        assertEquals(1, calls)
    }

    @Test
    fun `invalid stored value is regenerated`() {
        val store = InMemorySecureKeyValueStore()
        store.putString(DatabaseEncryptionKey.STORE_KEY, "short")

        val key = DatabaseEncryptionKey.getOrCreate(store) { ByteArray(32) { 7 } }

        assertEquals(64, key.length)
        assertEquals(key, store.getString(DatabaseEncryptionKey.STORE_KEY))
    }

    @Test
    fun `auth token clear does not drop the database passphrase`() {
        val store = InMemorySecureKeyValueStore()
        val key = DatabaseEncryptionKey.getOrCreate(store) { ByteArray(32) { 3 } }

        SecureTokenStore(store).clear()

        assertEquals(
            key,
            DatabaseEncryptionKey.getOrCreate(store) { error("must not regenerate") },
        )
    }

    @Test
    fun `open plan prefers existing encrypted file`() {
        assertEquals(
            EncryptedDatabaseOpenPlan.OPEN_ENCRYPTED,
            EncryptedDatabasePolicy.openPlan(encryptedExists = true, plaintextExists = true),
        )
        assertEquals(
            EncryptedDatabaseOpenPlan.OPEN_ENCRYPTED,
            EncryptedDatabasePolicy.openPlan(encryptedExists = true, plaintextExists = false),
        )
    }

    @Test
    fun `open plan migrates leftover plaintext`() {
        assertEquals(
            EncryptedDatabaseOpenPlan.MIGRATE_PLAINTEXT,
            EncryptedDatabasePolicy.openPlan(encryptedExists = false, plaintextExists = true),
        )
    }

    @Test
    fun `open plan creates encrypted when nothing exists`() {
        assertEquals(
            EncryptedDatabaseOpenPlan.CREATE_ENCRYPTED,
            EncryptedDatabasePolicy.openPlan(encryptedExists = false, plaintextExists = false),
        )
    }

    @Test
    fun `attach export sql escapes quotes`() {
        val sql = EncryptedDatabasePolicy.attachEncryptedSql(
            encryptedPath = "/tmp/o'brien.db",
            passphrase = "abc'def",
        )
        assertEquals("ATTACH DATABASE '/tmp/o''brien.db' AS encrypted KEY 'abc''def'", sql)
    }
}
