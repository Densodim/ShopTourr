package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.fake.FakeTripRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CreateTripUseCaseTest {

    @Test
    fun `creates trip when fields are valid`() = runTest {
        val repo = FakeTripRepository()
        val result = CreateTripUseCase(repo)(
            CreateTripDraft(
                city = "Lisbon",
                country = "Portugal",
                startDate = "2026-04-12",
                endDate = "2026-04-19",
                budget = Money.parse("1200.00", "EUR"),
            )
        )
        assertTrue(result.isSuccess)
        assertEquals("Lisbon", result.getOrThrow().city)
        assertEquals(1, repo.createCalls)
    }

    @Test
    fun `rejects blank city`() = runTest {
        val result = CreateTripUseCase(FakeTripRepository())(
            CreateTripDraft(
                city = " ",
                country = "Portugal",
                startDate = "2026-04-12",
                endDate = "2026-04-19",
                budget = Money.parse("100.00", "EUR"),
            )
        )
        assertEquals(AppError.Validation("city"), result.exceptionOrNull())
    }

    @Test
    fun `rejects a city that is only symbols`() = runTest {
        val result = CreateTripUseCase(FakeTripRepository())(
            CreateTripDraft(
                city = "@@@",
                country = "Portugal",
                startDate = "2026-04-12",
                endDate = "2026-04-19",
                budget = Money.parse("100.00", "EUR"),
            )
        )
        assertEquals(AppError.Validation("city"), result.exceptionOrNull())
    }

    @Test
    fun `rejects a lowercase country code and a non ISO date`() = runTest {
        val repo = FakeTripRepository()
        assertEquals(
            AppError.Validation("countryCode"),
            CreateTripUseCase(repo)(
                CreateTripDraft(
                    city = "Lisbon",
                    country = "Portugal",
                    startDate = "2026-04-12",
                    endDate = "2026-04-19",
                    budget = Money.parse("100.00", "EUR"),
                    countryCode = "pt",
                )
            ).exceptionOrNull(),
        )
        assertEquals(
            AppError.Validation("startDate"),
            CreateTripUseCase(repo)(
                CreateTripDraft(
                    city = "Lisbon",
                    country = "Portugal",
                    startDate = "12.04.2026",
                    endDate = "2026-04-19",
                    budget = Money.parse("100.00", "EUR"),
                )
            ).exceptionOrNull(),
        )
    }

    @Test
    fun `rejects end before start`() = runTest {
        val result = CreateTripUseCase(FakeTripRepository())(
            CreateTripDraft(
                city = "Oslo",
                country = "Norway",
                startDate = "2026-06-26",
                endDate = "2026-06-20",
                budget = Money.parse("100.00", "NOK"),
            )
        )
        assertEquals(AppError.Validation("dates"), result.exceptionOrNull())
    }
}
