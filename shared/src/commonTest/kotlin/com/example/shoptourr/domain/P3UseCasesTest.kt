package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateTravelerDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.example.shoptourr.domain.usecase.ActivatePremiumUseCase
import com.example.shoptourr.domain.usecase.AddTravelerUseCase
import com.example.shoptourr.domain.usecase.InviteTravelerUseCase
import com.example.shoptourr.domain.usecase.RefreshExchangeRateUseCase
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.fake.FakeUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class P3UseCasesTest {

    private val trip = TripSummary(
        id = "lisbon",
        city = "Lisbon",
        country = "Portugal",
        status = TripStatus.ACTIVE,
        startDate = "2026-08-01",
        endDate = "2026-08-10",
        budget = Money.parse("1000.00", "EUR"),
        spent = Money.zero("EUR"),
        purchaseCount = 0,
    )

    @Test
    fun `add traveler rejects blank name`() = runTest {
        assertEquals(
            AppError.Validation("name"),
            AddTravelerUseCase(FakeTripRepository(trips = listOf(trip)))
                .invoke("lisbon", CreateTravelerDraft(name = " "))
                .exceptionOrNull(),
        )
    }

    @Test
    fun `invite traveler returns pending invite`() = runTest {
        val repo = FakeTripRepository(trips = listOf(trip))
        val invite = InviteTravelerUseCase(repo)("lisbon", "friend@voyage.app").getOrThrow()
        assertEquals("friend@voyage.app", invite.email)
        assertEquals(1, repo.inviteCalls)
    }

    @Test
    fun `refresh fx updates rate`() = runTest {
        val repo = FakeTripRepository(trips = listOf(trip))
        val rate = RefreshExchangeRateUseCase(repo)("lisbon").getOrThrow()
        assertEquals("98.50", rate.rate)
        assertEquals(1, repo.fxRefreshCalls)
    }

    @Test
    fun `activate premium upgrades plan`() = runTest {
        val profile = UserProfile(
            id = "u1",
            displayName = "Mila",
            email = "mila@voyage.app",
            locale = "ru",
            preferredCurrency = "RUB",
            theme = com.example.shoptourr.domain.model.ThemeMode.SYSTEM,
            pushNotificationsEnabled = true,
            memberSince = "2026-01-01",
            premiumPlan = PremiumPlan.FREE,
            stats = UserStats(0, 0, 0),
        )
        val repo = FakeUserRepository(profile = profile)
        val updated = ActivatePremiumUseCase(repo)(PremiumPlan.PLUS).getOrThrow()
        assertTrue(updated.isPremium)
        assertEquals(1, repo.activatePremiumCalls)
    }
}
