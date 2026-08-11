package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeTripRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class ObserveHomeUseCaseTest {

    @Test
    fun `maps repository home snapshot for logged in user`() = runTest {
        val auth = FakeAuthRepository(
            session = AuthSession(
                accessToken = "a",
                refreshToken = "r",
                accessExpiresIn = 1,
                refreshExpiresIn = 1,
                user = User("u1", "Mila", "mila@voyage.app", "ru"),
            )
        )
        val trips = FakeTripRepository(
            HomeSnapshot(
                userName = "ignored",
                currentTripCity = "Lisbon",
                upcomingCount = 1,
                archiveCount = 2,
            )
        )
        val home = ObserveHomeUseCase(auth, trips)().first()

        assertEquals("Mila", home.userName)
        assertEquals("Lisbon", home.currentTripCity)
        assertEquals(1, home.upcomingCount)
        assertEquals(2, home.archiveCount)
    }

    @Test
    fun `without user emits empty home`() = runTest {
        val home = ObserveHomeUseCase(FakeAuthRepository(), FakeTripRepository())().first()
        assertEquals("", home.userName)
        assertNull(home.currentTripCity)
    }
}

class TripSummaryGroupingTest {

    @Test
    fun `groups trips into home snapshot counters`() {
        val trips = listOf(
            summary("1", TripStatus.ACTIVE, "Lisbon"),
            summary("2", TripStatus.UPCOMING, "Oslo"),
            summary("3", TripStatus.PAST, "Tokyo"),
            summary("4", TripStatus.PAST, "Rome"),
        )
        val snapshot = TripSummary.toHomeSnapshot(userName = "Mila", trips = trips)
        assertEquals("Mila", snapshot.userName)
        assertEquals("Lisbon", snapshot.currentTripCity)
        assertEquals(1, snapshot.upcomingCount)
        assertEquals(2, snapshot.archiveCount)
    }

    private fun summary(id: String, status: TripStatus, city: String) = TripSummary(
        id = id,
        city = city,
        country = "X",
        status = status,
        startDate = "2026-01-01",
        endDate = "2026-01-07",
        budget = Money.parse("100.00", "EUR"),
        spent = Money.zero("EUR"),
        purchaseCount = 0,
    )
}
