package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.fake.FakePurchaseRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CreatePurchaseUseCaseTest {

    @Test
    fun `creates purchase with vat gross and enqueues sync`() = runTest {
        val repo = FakePurchaseRepository()
        val useCase = CreatePurchaseUseCase(repo)

        val result = useCase(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = "Belem",
            ),
        )

        assertTrue(result.isSuccess)
        val purchase = result.getOrThrow()
        assertEquals("Pasteis", purchase.name)
        assertEquals(Money.parse("4.50", "EUR"), purchase.amount)
        assertEquals(1, repo.createCalls)
        assertEquals(1, repo.enqueuedSyncCalls)
    }

    @Test
    fun `rejects blank name`() = runTest {
        val repo = FakePurchaseRepository()
        val result = CreatePurchaseUseCase(repo)(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = "  ",
                category = PurchaseCategory.FOOD,
                amount = Money.parse("1.00", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = null,
            ),
        )
        assertEquals(AppError.Validation("name"), result.exceptionOrNull())
        assertEquals(0, repo.createCalls)
    }

    @Test
    fun `rejects non-positive amount`() = runTest {
        val result = CreatePurchaseUseCase(FakePurchaseRepository())(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = "X",
                category = PurchaseCategory.OTHER,
                amount = Money.zero("EUR"),
                vatIncluded = true,
                vatRatePercent = "0",
                place = null,
            ),
        )
        assertEquals(AppError.Validation("amount"), result.exceptionOrNull())
    }
}
