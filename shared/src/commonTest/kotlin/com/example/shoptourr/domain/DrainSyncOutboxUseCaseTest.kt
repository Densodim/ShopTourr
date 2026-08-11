package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.SyncDrainResult
import com.example.shoptourr.domain.usecase.CreateDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import com.example.shoptourr.domain.usecase.RefreshHomeUseCase
import com.example.shoptourr.fake.FakeDiaryRepository
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.fake.FakeSyncRepository
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.fake.FakeWishlistRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DrainSyncOutboxUseCaseTest {

    @Test
    fun `drain delegates to sync repository`() = runTest {
        val sync = FakeSyncRepository(Result.success(SyncDrainResult(2, 1)))
        val result = DrainSyncOutboxUseCase(sync)(limit = 5).getOrThrow()
        assertEquals(1, sync.drainCalls)
        assertEquals(5, sync.lastLimit)
        assertEquals(2, result.successCount)
        assertEquals(1, result.failureCount)
    }

    @Test
    fun `create purchase drains outbox after local success`() = runTest {
        val sync = FakeSyncRepository()
        val useCase = CreatePurchaseUseCase(
            purchaseRepository = FakePurchaseRepository(),
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )
        useCase(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = null,
            ),
        ).getOrThrow()
        assertEquals(1, sync.drainCalls)
    }

    @Test
    fun `create purchase does not drain on validation failure`() = runTest {
        val sync = FakeSyncRepository()
        val useCase = CreatePurchaseUseCase(
            purchaseRepository = FakePurchaseRepository(),
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )
        val result = useCase(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = " ",
                category = PurchaseCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = null,
            ),
        )
        assertTrue(result.isFailure)
        assertEquals(0, sync.drainCalls)
    }

    @Test
    fun `create trip drains outbox after local success`() = runTest {
        val sync = FakeSyncRepository()
        CreateTripUseCase(
            tripRepository = FakeTripRepository(),
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )(
            CreateTripDraft(
                city = "Lisbon",
                country = "Portugal",
                startDate = "2026-04-12",
                endDate = "2026-04-19",
                budget = Money.parse("1200.00", "EUR"),
            ),
        ).getOrThrow()
        assertEquals(1, sync.drainCalls)
    }

    @Test
    fun `create wishlist drains outbox after local success`() = runTest {
        val sync = FakeSyncRepository()
        CreateWishlistItemUseCase(
            wishlistRepository = FakeWishlistRepository(),
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )(
            CreateWishlistDraft("Pastel", "Lisbon", Money.parse("1.20", "EUR")),
        ).getOrThrow()
        assertEquals(1, sync.drainCalls)
    }

    @Test
    fun `create diary drains outbox after local success`() = runTest {
        val sync = FakeSyncRepository()
        CreateDiaryEntryUseCase(
            diaryRepository = FakeDiaryRepository(),
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )(
            "lisbon",
            CreateDiaryDraft(mood = "happy", text = "Pasteis"),
        ).getOrThrow()
        assertEquals(1, sync.drainCalls)
    }
}
