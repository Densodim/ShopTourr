package com.example.shoptourr.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.shoptourr.data.local.SqlDelightPurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightTripLocalStore
import com.example.shoptourr.data.sync.SqlDelightSyncOutbox
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.VatCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SqlDelightStoresHostTest {

    private fun database(): VoyageDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        VoyageDatabase.Schema.create(driver)
        return VoyageDatabase(driver)
    }

    @Test
    fun `trip store replaceAll and observe`() = runTest {
        val store = SqlDelightTripLocalStore(database())
        store.replaceAll(
            listOf(
                TripSummary(
                    id = "1",
                    city = "Lisbon",
                    country = "Portugal",
                    status = TripStatus.ACTIVE,
                    startDate = "2026-04-12",
                    endDate = "2026-04-19",
                    budget = Money.parse("1200.00", "EUR"),
                    spent = Money.parse("100.00", "EUR"),
                    purchaseCount = 2,
                    dayCount = 7,
                )
            )
        )
        assertEquals("Lisbon", store.all().single().city)
        assertEquals("Lisbon", store.observeAll().first().single().city)
    }

    @Test
    fun `purchase store upsert and replaceId`() = runTest {
        val store = SqlDelightPurchaseLocalStore(database())
        val vat = VatCalculator.breakdown(Money.parse("4.50", "EUR"), "23", true)
        store.upsert(
            Purchase(
                id = "local-1",
                tripId = "lisbon",
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = vat.gross,
                vat = vat,
                taxRefundEligible = false,
                place = "Belem",
                purchaseDate = "2026-04-15",
                purchaseTime = "10:24",
                pendingSync = true,
            )
        )
        store.replaceId(
            oldId = "local-1",
            purchase = Purchase(
                id = "server-1",
                tripId = "lisbon",
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = vat.gross,
                vat = vat,
                taxRefundEligible = false,
                place = "Belem",
                purchaseDate = "2026-04-15",
                purchaseTime = "10:24",
                pendingSync = false,
            )
        )
        val items = store.observeByTrip("lisbon").first()
        assertEquals(listOf("server-1"), items.map { it.id })
        assertEquals(false, items.single().pendingSync)
    }

    @Test
    fun `outbox enqueue pending and success`() = runTest {
        val outbox = SqlDelightSyncOutbox(database())
        outbox.enqueue(
            SyncOutboxEntry(
                id = "o1",
                type = SyncMutationType.CREATE_PURCHASE,
                payloadJson = "{}",
                idempotencyKey = "k1",
                createdAtEpochMs = 1L,
            )
        )
        assertEquals(1, outbox.pending().size)
        outbox.markFailure("o1", updatedAtEpochMs = 2L)
        assertEquals(1, outbox.pending().single().failureCount)
        outbox.markSuccess("o1")
        assertTrue(outbox.pending().isEmpty())
    }
}
