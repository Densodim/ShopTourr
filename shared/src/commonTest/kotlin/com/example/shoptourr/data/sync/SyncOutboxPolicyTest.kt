package com.example.shoptourr.data.sync

import com.example.shoptourr.domain.error.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SyncOutboxPolicyTest {

    @Test
    fun `backoff doubles until cap`() {
        assertEquals(0L, SyncOutboxPolicy.backoffMs(0))
        assertEquals(1_000L, SyncOutboxPolicy.backoffMs(1))
        assertEquals(2_000L, SyncOutboxPolicy.backoffMs(2))
        assertEquals(4_000L, SyncOutboxPolicy.backoffMs(3))
        assertEquals(15 * 60_000L, SyncOutboxPolicy.backoffMs(20))
    }

    @Test
    fun `isDue respects failure backoff window`() {
        val entry = SyncOutboxEntry(
            id = "1",
            type = SyncMutationType.CREATE_PURCHASE,
            payloadJson = "{}",
            idempotencyKey = "k",
            createdAtEpochMs = 0L,
            updatedAtEpochMs = 1_000L,
            failureCount = 2,
            status = SyncOutboxStatus.PENDING,
        )
        assertFalse(SyncOutboxPolicy.isDue(entry, nowEpochMs = 2_000L))
        assertTrue(SyncOutboxPolicy.isDue(entry, nowEpochMs = 1_000L + 2_000L))
    }

    @Test
    fun `enqueue caps pending size`() = runTest {
        val outbox = InMemorySyncOutbox(maxPending = 2)
        outbox.enqueue(
            SyncOutboxEntry("a", SyncMutationType.CREATE_PURCHASE, "{}", "a", 1L),
        )
        outbox.enqueue(
            SyncOutboxEntry("b", SyncMutationType.CREATE_PURCHASE, "{}", "b", 2L),
        )
        val error = assertFailsWith<AppError.Validation> {
            outbox.enqueue(
                SyncOutboxEntry("c", SyncMutationType.CREATE_PURCHASE, "{}", "c", 3L),
            )
        }
        assertEquals("outbox_full", error.message)
    }

    @Test
    fun `markFailure permanently fails after max`() = runTest {
        val outbox = InMemorySyncOutbox(maxFailures = 2)
        outbox.enqueue(
            SyncOutboxEntry("a", SyncMutationType.CREATE_PURCHASE, "{}", "a", 1L),
        )
        outbox.markFailure("a", updatedAtEpochMs = 2L)
        assertEquals(SyncOutboxStatus.PENDING, outbox.pending().single().status)
        outbox.markFailure("a", updatedAtEpochMs = 3L)
        assertEquals(SyncOutboxStatus.FAILED, outbox.pending().single().status)
        assertFalse(SyncOutboxPolicy.isDue(outbox.pending().single(), nowEpochMs = 100_000L))
    }
}
