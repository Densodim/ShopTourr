package com.example.shoptourr.data

import com.example.shoptourr.data.settings.SettingsTokenStore
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncOutboxStatus
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SyncOutboxTest {

    @Test
    fun `enqueue then peek pending in FIFO order`() = runTest {
        val outbox = InMemorySyncOutbox()
        outbox.enqueue(
            SyncOutboxEntry(
                id = "1",
                type = SyncMutationType.CREATE_PURCHASE,
                payloadJson = """{"tripId":"t1"}""",
                idempotencyKey = "k1",
                createdAtEpochMs = 1L,
            )
        )
        outbox.enqueue(
            SyncOutboxEntry(
                id = "2",
                type = SyncMutationType.CREATE_DIARY,
                payloadJson = """{"tripId":"t1"}""",
                idempotencyKey = "k2",
                createdAtEpochMs = 2L,
            )
        )

        val pending = outbox.pending()
        assertEquals(listOf("1", "2"), pending.map { it.id })
        assertTrue(pending.all { it.status == SyncOutboxStatus.PENDING })
    }

    @Test
    fun `mark success removes entry`() = runTest {
        val outbox = InMemorySyncOutbox()
        outbox.enqueue(
            SyncOutboxEntry(
                id = "1",
                type = SyncMutationType.CREATE_PURCHASE,
                payloadJson = "{}",
                idempotencyKey = "k1",
                createdAtEpochMs = 1L,
            )
        )
        outbox.markSuccess("1")
        assertTrue(outbox.pending().isEmpty())
    }

    @Test
    fun `mark failure bumps failureCount and keeps pending`() = runTest {
        val outbox = InMemorySyncOutbox()
        outbox.enqueue(
            SyncOutboxEntry(
                id = "1",
                type = SyncMutationType.CREATE_PURCHASE,
                payloadJson = "{}",
                idempotencyKey = "k1",
                createdAtEpochMs = 1L,
            )
        )
        outbox.markFailure("1", updatedAtEpochMs = 10L)
        val entry = outbox.pending().single()
        assertEquals(1, entry.failureCount)
        assertEquals(10L, entry.updatedAtEpochMs)
        assertEquals(SyncOutboxStatus.PENDING, entry.status)
    }
}

class SettingsTokenStoreTest {

    @Test
    fun `save load and clear tokens`() {
        val store = SettingsTokenStore(MapSettings())
        assertNull(store.accessToken())

        store.saveTokens(accessToken = "a", refreshToken = "r")
        assertEquals("a", store.accessToken())
        assertEquals("r", store.refreshToken())

        store.clear()
        assertNull(store.accessToken())
        assertNull(store.refreshToken())
    }
}
