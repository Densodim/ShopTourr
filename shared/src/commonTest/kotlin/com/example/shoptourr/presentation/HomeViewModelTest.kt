package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.RefreshHomeUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.presentation.home.HomeIntent
import com.example.shoptourr.presentation.home.HomeViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads home snapshot on start and refresh`() = runTest {
        val trips = FakeTripRepository(
            HomeSnapshot("Mila", "Lisbon", upcomingCount = 1, archiveCount = 2)
        )
        val auth = FakeAuthRepository(
            session = AuthSession("a", "r", 1, 1, User("u1", "Mila", "m@v.app", "ru"))
        )
        val vm = HomeViewModel(
            observeHome = ObserveHomeUseCase(auth, trips),
            refreshHome = RefreshHomeUseCase(trips),
        )

        vm.state.test {
            var state = awaitItem()
            if (state.snapshot == null) state = awaitItem()
            assertEquals("Lisbon", state.snapshot?.currentTripCity)
            assertNull(state.error)

            trips.queueRefresh(HomeSnapshot("Mila", "Oslo", 0, 2))
            vm.onIntent(HomeIntent.Refresh)
            val refreshed = awaitItem()
            assertEquals("Oslo", refreshed.snapshot?.currentTripCity)
            cancelAndIgnoreRemainingEvents()
        }
        vm.onCleared()
    }
}
