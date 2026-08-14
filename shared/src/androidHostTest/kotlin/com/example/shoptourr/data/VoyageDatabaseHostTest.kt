package com.example.shoptourr.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.shoptourr.db.VoyageDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class VoyageDatabaseHostTest {

    @Test
    fun `outbox upsert and pending query`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        VoyageDatabase.Schema.create(driver)
        val db = VoyageDatabase(driver)

        db.syncOutboxEntityQueries.upsert(
            id = "1",
            type = "CREATE_PURCHASE",
            payload_json = """{"a":1}""",
            idempotency_key = "k1",
            created_at_epoch_ms = 1,
            updated_at_epoch_ms = 1,
            failure_count = 0,
            status = "PENDING",
        )

        val pending = db.syncOutboxEntityQueries.selectPending().executeAsList()
        assertEquals(1, pending.size)
        assertEquals("1", pending.first().id)
    }

    @Test
    fun `schema version is 1 and migrate is a no-op`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        VoyageDatabase.Schema.create(driver)
        assertEquals(1L, VoyageDatabase.Schema.version)
        // Next schema change needs 1.sqm plus a regenerated databases/ snapshot.
        VoyageDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 1)
    }
}
