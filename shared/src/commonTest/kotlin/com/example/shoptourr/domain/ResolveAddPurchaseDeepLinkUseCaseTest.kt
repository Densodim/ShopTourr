package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.ResolveAddPurchaseDeepLinkUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.fake.FakeUserRepository
import com.example.shoptourr.navigation.VoyageNavigationTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class ResolveAddPurchaseDeepLinkUseCaseTest {

    @Test
    fun `keeps an explicit trip id`() = runTest {
        val target = resolve()(VoyageNavigationTarget.AddPurchase("lisbon"))
        assertIs<VoyageNavigationTarget.AddPurchase>(target)
        assertEquals("lisbon", target.tripId)
    }

    @Test
    fun `fills in the current trip when the shortcut has none`() = runTest {
        val target = resolve(home = lisbonHome())(VoyageNavigationTarget.AddPurchase(null))
        assertIs<VoyageNavigationTarget.AddPurchase>(target)
        assertEquals("lisbon", target.tripId)
    }

    @Test
    fun `opens home when there is no current trip`() = runTest {
        val target = resolve()(VoyageNavigationTarget.AddPurchase(null))
        assertEquals(VoyageNavigationTarget.Home, target)
    }

    @Test
    fun `leaves other destinations alone`() = runTest {
        val target = resolve()(VoyageNavigationTarget.TripAlerts("oslo"))
        assertEquals(VoyageNavigationTarget.TripAlerts("oslo"), target)
    }

    private fun lisbonHome() = HomeSnapshot(
        userName = "Mila",
        currentTripCity = "Lisbon",
        upcomingCount = 0,
        archiveCount = 0,
        currentTripId = "lisbon",
    )

    private fun resolve(
        home: HomeSnapshot = HomeSnapshot("", null, 0, 0),
    ) = ResolveAddPurchaseDeepLinkUseCase(
        observeHome = ObserveHomeUseCase(
            authRepository = FakeAuthRepository(loggedInOverride = true),
            tripRepository = FakeTripRepository(home),
            userRepository = FakeUserRepository(),
        ),
    )
}
