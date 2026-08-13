package com.example.shoptourr.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.shoptourr.data.local.SqlDelightAlertsLocalStore
import com.example.shoptourr.data.local.SqlDelightDiaryLocalStore
import com.example.shoptourr.data.local.SqlDelightExportLocalStore
import com.example.shoptourr.data.local.SqlDelightPurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightRouteLocalStore
import com.example.shoptourr.data.local.SqlDelightStatsLocalStore
import com.example.shoptourr.data.local.SqlDelightTaxFreeLocalStore
import com.example.shoptourr.data.local.SqlDelightLocalCacheInventory
import com.example.shoptourr.data.local.SqlDelightTripLocalStore
import com.example.shoptourr.data.local.SqlDelightWishlistLocalStore
import com.example.shoptourr.data.sync.SqlDelightSyncOutbox
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.AlertSeverity
import com.example.shoptourr.domain.model.AlertType
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TaxFreeRules
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.model.WishlistItem
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

    @Test
    fun `wishlist store replace and observe`() = runTest {
        val store = SqlDelightWishlistLocalStore(database())
        store.replaceAll(
            listOf(
                WishlistItem(
                    id = "w1",
                    name = "Tile",
                    city = "Lisbon",
                    targetPrice = Money.parse("12.00", "EUR"),
                    createdAt = "2026-04-12T10:00:00Z",
                )
            )
        )
        assertEquals("Tile", store.all().single().name)
        assertEquals("Tile", store.observe().first().single().name)
        store.remove("w1")
        assertTrue(store.all().isEmpty())
    }

    @Test
    fun `diary store groups by date`() = runTest {
        val store = SqlDelightDiaryLocalStore(database())
        store.upsertEntry(
            DiaryEntry(
                id = "d1",
                tripId = "lisbon",
                entryDate = "2026-04-15",
                mood = "good",
                text = "Pasteis",
                createdAt = "2026-04-15T10:00:00Z",
                updatedAt = "2026-04-15T10:00:00Z",
            )
        )
        store.upsertEntry(
            DiaryEntry(
                id = "d2",
                tripId = "lisbon",
                entryDate = "2026-04-16",
                mood = "ok",
                text = "Tram",
                createdAt = "2026-04-16T10:00:00Z",
                updatedAt = "2026-04-16T10:00:00Z",
            )
        )
        val days = store.observe("lisbon").first()
        assertEquals(listOf("2026-04-16", "2026-04-15"), days.map { it.date })
        store.removeEntry("lisbon", "d1")
        assertEquals(1, store.observe("lisbon").first().single().entries.size)
    }

    @Test
    fun `trip cache stores tax free alerts route stats export`() = runTest {
        val db = database()
        val taxFree = SqlDelightTaxFreeLocalStore(db, clock = { 1L })
        val alerts = SqlDelightAlertsLocalStore(db, clock = { 1L })
        val route = SqlDelightRouteLocalStore(db, clock = { 1L })
        val stats = SqlDelightStatsLocalStore(db, clock = { 1L })
        val export = SqlDelightExportLocalStore(db, clock = { 1L })

        taxFree.save(
            TaxFreeSummary(
                tripId = "lisbon",
                rules = TaxFreeRules(
                    currency = "EUR",
                    minimumPurchase = Money.parse("50.00", "EUR"),
                    estimatedRefundRate = "0.13",
                    regionLabel = "EU",
                ),
                eligibleCount = 0,
                eligibleTotal = Money.parse("0.00", "EUR"),
                estimatedRefundTotal = Money.parse("0.00", "EUR"),
                items = emptyList(),
            )
        )
        alerts.replaceAll(
            "lisbon",
            listOf(
                BudgetAlert(
                    id = "a1",
                    type = AlertType.BUDGET_ALMOST_GONE,
                    severity = AlertSeverity.WARNING,
                    titleKey = "t",
                    bodyKey = "b",
                    createdAt = "2026-08-11T00:00:00Z",
                    read = false,
                ),
            ),
        )
        route.save(TripRoute(tripId = "lisbon", stopCount = 0, stops = emptyList()))
        stats.save(
            TripStats(
                tripId = "lisbon",
                totalSpent = Money.parse("10.00", "EUR"),
                budget = Money.parse("100.00", "EUR"),
                dailyAverage = Money.parse("1.00", "EUR"),
                remaining = Money.parse("90.00", "EUR"),
                onBudget = true,
                byCategory = emptyList(),
                byDay = emptyList(),
            )
        )
        export.save(
            ExportJob(
                id = "e1",
                tripId = "lisbon",
                format = ExportFormat.CSV,
                status = ExportJobStatus.QUEUED,
                createdAt = "2026-08-11T00:00:00Z",
            )
        )

        assertEquals("lisbon", taxFree.observe("lisbon").first()!!.tripId)
        assertEquals("a1", alerts.observe("lisbon").first().single().id)
        assertEquals(0, route.observe("lisbon").first()!!.stopCount)
        assertEquals("10.00", stats.observe("lisbon").first()!!.totalSpent.toDecimalString())
        assertEquals(ExportJobStatus.QUEUED, export.observe("lisbon").first()!!.status)
    }

    @Test
    fun `clearAll wipes trips purchases wishlist and outbox`() = runTest {
        val db = database()
        val trips = SqlDelightTripLocalStore(db)
        val purchases = SqlDelightPurchaseLocalStore(db)
        val wishlist = SqlDelightWishlistLocalStore(db)
        val outbox = SqlDelightSyncOutbox(db)
        val vat = VatCalculator.breakdown(Money.parse("4.50", "EUR"), "23", true)

        trips.replaceAll(
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
                    purchaseCount = 1,
                    dayCount = 7,
                ),
            ),
        )
        purchases.upsert(
            Purchase(
                id = "p1",
                tripId = "1",
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = vat.gross,
                vat = vat,
                taxRefundEligible = false,
                place = "Belem",
                purchaseDate = "2026-04-15",
                purchaseTime = "10:24",
                pendingSync = true,
            ),
        )
        wishlist.upsert(
            WishlistItem(
                id = "w1",
                name = "Tile",
                city = "Lisbon",
                targetPrice = Money.parse("20.00", "EUR"),
                createdAt = "2026-04-15T00:00:00Z",
            ),
        )
        outbox.enqueue(
            SyncOutboxEntry(
                id = "o1",
                type = SyncMutationType.CREATE_PURCHASE,
                payloadJson = "{}",
                idempotencyKey = "k1",
                createdAtEpochMs = 1L,
            ),
        )

        trips.clearAll()
        purchases.clearAll()
        wishlist.clearAll()
        outbox.clearAll()

        assertTrue(trips.all().isEmpty())
        assertTrue(purchases.observeByTrip("1").first().isEmpty())
        assertTrue(wishlist.all().isEmpty())
        assertTrue(outbox.pending().isEmpty())
    }

    @Test
    fun `trip cache inventory evicts by trip id`() = runTest {
        val db = database()
        val taxFree = SqlDelightTaxFreeLocalStore(db, clock = { 1_000L })
        taxFree.save(
            TaxFreeSummary(
                tripId = "old",
                rules = TaxFreeRules(
                    currency = "EUR",
                    minimumPurchase = Money.parse("50.00", "EUR"),
                    estimatedRefundRate = "0.13",
                    regionLabel = "EU",
                ),
                eligibleCount = 0,
                eligibleTotal = Money.parse("0.00", "EUR"),
                estimatedRefundTotal = Money.parse("10.00", "EUR"),
                items = emptyList(),
            ),
        )
        val inventory = SqlDelightLocalCacheInventory(db)
        inventory.evictTripCache(setOf("old"))
        assertTrue(inventory.tripCacheRecords().isEmpty())
    }
}
