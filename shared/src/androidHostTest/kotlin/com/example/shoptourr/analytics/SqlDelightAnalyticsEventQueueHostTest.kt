package com.example.shoptourr.analytics

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.shoptourr.db.VoyageDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SqlDelightAnalyticsEventQueueHostTest {

    private fun database(): VoyageDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        VoyageDatabase.Schema.create(driver)
        return VoyageDatabase(driver)
    }

    @Test
    fun `enqueue pending and remove persists across reads`() = runTest {
        val queue = SqlDelightAnalyticsEventQueue(database())
        queue.enqueue(
            AnalyticsEvent(
                id = "a1",
                name = "screen_view",
                properties = mapOf("screen" to "home"),
                timestampEpochMs = 10L,
            ),
        )
        queue.enqueue(
            AnalyticsEvent(
                id = "a2",
                name = "purchase_created",
                properties = emptyMap(),
                timestampEpochMs = 20L,
            ),
        )

        val pending = queue.pending()
        assertEquals(2, pending.size)
        assertEquals("a1", pending.first().id)
        assertEquals("home", pending.first().properties["screen"])

        queue.removeAll(listOf(pending.first()))
        assertEquals(1, queue.size())
        assertEquals("a2", queue.pending().single().id)
    }

    @Test
    fun `enqueue drops oldest when over maxPending`() = runTest {
        val queue = SqlDelightAnalyticsEventQueue(database(), maxPending = 2)
        queue.enqueue(AnalyticsEvent("1", "e1", emptyMap(), 1L))
        queue.enqueue(AnalyticsEvent("2", "e2", emptyMap(), 2L))
        queue.enqueue(AnalyticsEvent("3", "e3", emptyMap(), 3L))

        val pending = queue.pending()
        assertEquals(2, pending.size)
        assertEquals(listOf("2", "3"), pending.map { it.id })
    }
}
