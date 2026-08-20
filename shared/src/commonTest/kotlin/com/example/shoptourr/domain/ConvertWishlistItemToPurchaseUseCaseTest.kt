package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.usecase.ConvertWishlistItemToPurchaseUseCase
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.canConvertWishlistItem
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.fake.FakeWishlistRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ConvertWishlistItemToPurchaseUseCaseTest {

    private val pastel = WishlistItem(
        id = "w1",
        name = "Pastel",
        city = "Lisbon",
        targetPrice = Money.parse("1.20", "EUR"),
        createdAt = "2026-08-11T00:00:00Z",
    )

    @Test
    fun `matching city logs the spend and drops the wish`() = runTest {
        val wishes = FakeWishlistRepository(initial = listOf(pastel))
        val purchases = FakePurchaseRepository()
        val purchase = ConvertWishlistItemToPurchaseUseCase(
            createPurchase = CreatePurchaseUseCase(purchases),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishes),
        )(item = pastel, tripId = "lisbon", tripCity = "Lisbon").getOrThrow()

        assertEquals("Pastel", purchase.name)
        assertEquals("Lisbon", purchase.place)
        assertEquals(PurchaseCategory.SOUVENIRS, purchase.category)
        assertEquals(Money.parse("1.20", "EUR"), purchase.amount)
        assertEquals("0", purchases.lastDraft?.vatRatePercent)
        assertEquals(true, purchases.lastDraft?.vatIncluded)
        assertEquals(1, purchases.createCalls)
        assertTrue(wishes.items.none { it.id == "w1" })
    }

    @Test
    fun `city match ignores case and padding`() = runTest {
        val wishes = FakeWishlistRepository(initial = listOf(pastel))
        val purchases = FakePurchaseRepository()
        ConvertWishlistItemToPurchaseUseCase(
            createPurchase = CreatePurchaseUseCase(purchases),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishes),
        )(item = pastel, tripId = "lisbon", tripCity = "  lisbon  ").getOrThrow()
        assertEquals(1, purchases.createCalls)
    }

    @Test
    fun `no active trip does not spend`() = runTest {
        val wishes = FakeWishlistRepository(initial = listOf(pastel))
        val purchases = FakePurchaseRepository()
        val result = ConvertWishlistItemToPurchaseUseCase(
            createPurchase = CreatePurchaseUseCase(purchases),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishes),
        )(item = pastel, tripId = null, tripCity = "Lisbon")
        assertEquals(AppError.Validation("tripId"), result.exceptionOrNull())
        assertEquals(0, purchases.createCalls)
        assertEquals(1, wishes.items.size)
    }

    @Test
    fun `a different city does not spend`() = runTest {
        val wishes = FakeWishlistRepository(initial = listOf(pastel))
        val purchases = FakePurchaseRepository()
        val result = ConvertWishlistItemToPurchaseUseCase(
            createPurchase = CreatePurchaseUseCase(purchases),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishes),
        )(item = pastel, tripId = "oslo", tripCity = "Oslo")
        assertEquals(AppError.Validation("city"), result.exceptionOrNull())
        assertEquals(0, purchases.createCalls)
        assertEquals(1, wishes.items.size)
    }

    @Test
    fun `create failure leaves the wish`() = runTest {
        val wishes = FakeWishlistRepository(initial = listOf(pastel))
        val purchases = FakePurchaseRepository(createError = AppError.Network)
        val result = ConvertWishlistItemToPurchaseUseCase(
            createPurchase = CreatePurchaseUseCase(purchases),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishes),
        )(item = pastel, tripId = "lisbon", tripCity = "Lisbon")
        assertEquals(AppError.Network, result.exceptionOrNull())
        assertEquals(1, wishes.items.size)
    }

    @Test
    fun `canConvert is true only in the same city`() {
        assertTrue(canConvertWishlistItem(pastel, "Lisbon"))
        assertTrue(canConvertWishlistItem(pastel, " lisbon "))
        assertFalse(canConvertWishlistItem(pastel, "Oslo"))
        assertFalse(canConvertWishlistItem(pastel, null))
        assertFalse(canConvertWishlistItem(pastel, " "))
    }
}
