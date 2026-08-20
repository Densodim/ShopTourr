package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.fake.FakeWishlistRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class WishlistUseCasesTest {

    @Test
    fun `create rejects blank name and city`() = runTest {
        val repo = FakeWishlistRepository()
        assertEquals(
            AppError.Validation("name"),
            CreateWishlistItemUseCase(repo)(
                CreateWishlistDraft(" ", "Lisbon", Money.parse("10.00", "EUR")),
            ).exceptionOrNull(),
        )
        assertEquals(
            AppError.Validation("city"),
            CreateWishlistItemUseCase(repo)(
                CreateWishlistDraft("Pastel", " ", Money.parse("10.00", "EUR")),
            ).exceptionOrNull(),
        )
        assertEquals(0, repo.createCalls)
    }

    @Test
    fun `create rejects a city of only symbols`() = runTest {
        assertEquals(
            AppError.Validation("city"),
            CreateWishlistItemUseCase(FakeWishlistRepository())(
                CreateWishlistDraft("Pastel", "@@@", Money.parse("10.00", "EUR")),
            ).exceptionOrNull(),
        )
    }

    @Test
    fun `create persists trimmed item`() = runTest {
        val repo = FakeWishlistRepository()
        val item = CreateWishlistItemUseCase(repo)(
            CreateWishlistDraft("  Pastel  ", " Lisbon ", Money.parse("1.20", "EUR")),
        ).getOrThrow()
        assertEquals("Pastel", item.name)
        assertEquals("Lisbon", item.city)
        assertEquals(1, repo.createCalls)
    }

    @Test
    fun `delete rejects blank id`() = runTest {
        val result = DeleteWishlistItemUseCase(FakeWishlistRepository())(" ")
        assertEquals(AppError.Validation("id"), result.exceptionOrNull())
    }

    @Test
    fun `delete removes item`() = runTest {
        val repo = FakeWishlistRepository()
        val created = CreateWishlistItemUseCase(repo)(
            CreateWishlistDraft("Pastel", "Lisbon", Money.parse("1.20", "EUR")),
        ).getOrThrow()
        DeleteWishlistItemUseCase(repo)(created.id).getOrThrow()
        assertTrue(repo.items.isEmpty())
    }
}
