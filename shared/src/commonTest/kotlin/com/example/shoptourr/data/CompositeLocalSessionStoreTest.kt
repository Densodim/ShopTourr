package com.example.shoptourr.data

import com.example.shoptourr.analytics.InMemoryAnalyticsEventQueue
import com.example.shoptourr.data.local.InMemoryDiaryLocalStore
import com.example.shoptourr.data.local.InMemoryExportLocalStore
import com.example.shoptourr.data.local.InMemoryPurchaseLocalStore
import com.example.shoptourr.data.local.InMemoryRouteLocalStore
import com.example.shoptourr.data.local.InMemoryStatsLocalStore
import com.example.shoptourr.data.local.InMemoryTaxFreeLocalStore
import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.local.InMemoryUserLocalStore
import com.example.shoptourr.data.local.InMemoryWishlistLocalStore
import com.example.shoptourr.data.local.InMemoryAlertsLocalStore
import com.example.shoptourr.data.local.CompositeLocalSessionStore
import com.example.shoptourr.data.push.DevicePushTokenHolder
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.example.shoptourr.domain.model.VatCalculator
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.navigation.VoyageNavigationTarget
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CompositeLocalSessionStoreTest {

    @BeforeTest
    fun resetPushToken() {
        DevicePushTokenHolder.update(null)
    }

    @Test
    fun `clearUserData wipes trips purchases wishlist outbox profile and push token`() = runTest {
        val trips = InMemoryTripLocalStore()
        val purchases = InMemoryPurchaseLocalStore()
        val wishlist = InMemoryWishlistLocalStore()
        val user = InMemoryUserLocalStore()
        val outbox = InMemorySyncOutbox()
        val analytics = InMemoryAnalyticsEventQueue()
        val deepLinks = PendingDeepLinkStore()
        val checkpoints = com.example.shoptourr.data.media.InMemoryUploadCheckpointStore()
        val vat = VatCalculator.breakdown(Money.parse("4.50", "EUR"), "23", true)

        trips.replaceAll(
            listOf(
                TripSummary(
                    id = "lisbon",
                    city = "Lisbon",
                    country = "Portugal",
                    status = TripStatus.ACTIVE,
                    startDate = "2026-04-12",
                    endDate = "2026-04-19",
                    budget = Money.parse("1200.00", "EUR"),
                    spent = Money.parse("100.00", "EUR"),
                    purchaseCount = 1,
                ),
            ),
        )
        purchases.upsert(
            Purchase(
                id = "p1",
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
        user.saveProfile(
            UserProfile(
                id = "u1",
                displayName = "Mila",
                email = "mila@voyage.app",
                locale = "ru",
                preferredCurrency = "EUR",
                theme = ThemeMode.SYSTEM,
                pushNotificationsEnabled = true,
                memberSince = "2026-01-01",
                stats = UserStats(1, 1, 1),
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
        analytics.enqueue(
            com.example.shoptourr.analytics.AnalyticsEvent(
                id = "e1",
                name = "purchase_created",
                timestampEpochMs = 1L,
            ),
        )
        deepLinks.offer(VoyageNavigationTarget.TripDetail("lisbon"))
        DevicePushTokenHolder.update("fcm-token")
        checkpoints.save("media-1", 128L)

        CompositeLocalSessionStore(
            userLocalStore = user,
            tripLocalStore = trips,
            purchaseLocalStore = purchases,
            wishlistLocalStore = wishlist,
            diaryLocalStore = InMemoryDiaryLocalStore(),
            taxFreeLocalStore = InMemoryTaxFreeLocalStore(),
            alertsLocalStore = InMemoryAlertsLocalStore(),
            routeLocalStore = InMemoryRouteLocalStore(),
            statsLocalStore = InMemoryStatsLocalStore(),
            exportLocalStore = InMemoryExportLocalStore(),
            outbox = outbox,
            analyticsQueue = analytics,
            pendingDeepLinks = deepLinks,
            uploadCheckpoints = checkpoints,
        ).clearUserData()

        assertTrue(trips.all().isEmpty())
        assertTrue(purchases.getById("p1") == null)
        assertTrue(wishlist.all().isEmpty())
        assertTrue(outbox.pending().isEmpty())
        assertTrue(analytics.pending().isEmpty())
        assertNull(user.profile())
        assertNull(deepLinks.consume())
        assertNull(DevicePushTokenHolder.token)
        assertEquals(0L, checkpoints.offsetBytes("media-1"))
    }
}
