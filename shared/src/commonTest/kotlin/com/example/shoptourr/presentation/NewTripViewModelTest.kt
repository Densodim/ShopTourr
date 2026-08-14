package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.presentation.trip.NewTripIntent
import com.example.shoptourr.presentation.trip.NewTripUiEvent
import com.example.shoptourr.presentation.trip.NewTripViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class NewTripViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `successful submit emits created event`() = runTest {
        val trips = FakeTripRepository(HomeSnapshot("Mila", null, 0, 0))
        val vm = NewTripViewModel(CreateTripUseCase(trips))

        vm.onIntent(NewTripIntent.CityChanged("Lisbon"))
        vm.onIntent(NewTripIntent.CountryChanged("Portugal"))
        vm.onIntent(NewTripIntent.StartDateChanged("2026-08-01"))
        vm.onIntent(NewTripIntent.EndDateChanged("2026-08-10"))
        vm.onIntent(NewTripIntent.BudgetChanged("1000.00"))

        vm.events.test {
            vm.onIntent(NewTripIntent.Submit)
            val event = awaitItem()
            assertIs<NewTripUiEvent.Created>(event)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun `validation failure shows field errors`() = runTest {
        val trips = FakeTripRepository(HomeSnapshot("Mila", null, 0, 0))
        val vm = NewTripViewModel(CreateTripUseCase(trips))

        vm.onIntent(NewTripIntent.Submit)

        assertEquals("validation_city_required", vm.state.value.fieldErrors.city)
        assertEquals("validation_country_required", vm.state.value.fieldErrors.country)
        assertEquals("validation_start_date_required", vm.state.value.fieldErrors.startDate)
        assertEquals("validation_end_date_required", vm.state.value.fieldErrors.endDate)
        assertEquals("validation_amount_required", vm.state.value.fieldErrors.budget)
        assertNull(vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun `end before start shows date order error`() = runTest {
        val trips = FakeTripRepository(HomeSnapshot("Mila", null, 0, 0))
        val vm = NewTripViewModel(CreateTripUseCase(trips))

        vm.onIntent(NewTripIntent.CityChanged("Lisbon"))
        vm.onIntent(NewTripIntent.CountryChanged("Portugal"))
        vm.onIntent(NewTripIntent.StartDateChanged("2026-08-10"))
        vm.onIntent(NewTripIntent.EndDateChanged("2026-08-01"))
        vm.onIntent(NewTripIntent.BudgetChanged("1000.00"))
        vm.onIntent(NewTripIntent.Submit)

        assertEquals("validation_dates_order", vm.state.value.fieldErrors.endDate)
        vm.onCleared()
    }

    @Test
    fun `repository failure maps to UiError`() = runTest {
        val trips = FakeTripRepository(
            HomeSnapshot("Mila", null, 0, 0),
            createError = AppError.Conflict,
        )
        val vm = NewTripViewModel(CreateTripUseCase(trips))
        vm.onIntent(NewTripIntent.CityChanged("Lisbon"))
        vm.onIntent(NewTripIntent.CountryChanged("Portugal"))
        vm.onIntent(NewTripIntent.StartDateChanged("2026-08-01"))
        vm.onIntent(NewTripIntent.EndDateChanged("2026-08-10"))
        vm.onIntent(NewTripIntent.BudgetChanged("1000.00"))
        vm.onIntent(NewTripIntent.Submit)

        assertEquals("Конфликт", vm.state.value.error?.title)
        vm.onCleared()
    }
}
