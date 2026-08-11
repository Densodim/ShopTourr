package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.presentation.purchase.AddPurchaseIntent
import com.example.shoptourr.presentation.purchase.AddPurchaseUiEvent
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.presentation.error.UiError
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
class AddPurchaseViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `successful submit emits created event`() = runTest {
        val repo = FakePurchaseRepository()
        val vm = AddPurchaseViewModel(
            tripId = "lisbon",
            createPurchase = CreatePurchaseUseCase(repo),
        )

        vm.onIntent(AddPurchaseIntent.NameChanged("Pasteis"))
        vm.onIntent(AddPurchaseIntent.AmountChanged("4.50"))
        vm.onIntent(AddPurchaseIntent.CategoryChanged(PurchaseCategory.FOOD))
        vm.onIntent(AddPurchaseIntent.PlaceChanged("Belem"))

        vm.events.test {
            vm.onIntent(AddPurchaseIntent.Submit)
            assertIs<AddPurchaseUiEvent.Created>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
        assertEquals(1, repo.createCalls)
        vm.onCleared()
    }

    @Test
    fun `validation failure maps to UiError`() = runTest {
        val vm = AddPurchaseViewModel(
            tripId = "lisbon",
            createPurchase = CreatePurchaseUseCase(FakePurchaseRepository()),
        )
        vm.onIntent(AddPurchaseIntent.Submit)

        val error = vm.state.value.error
        assertIs<UiError>(error)
        assertEquals("Validation Error", error.title)
        assertEquals("name", error.message)
        vm.onCleared()
    }

    @Test
    fun `repository failure maps to UiError`() = runTest {
        val repo = FakePurchaseRepository(createError = AppError.Conflict)
        val vm = AddPurchaseViewModel(
            tripId = "lisbon",
            createPurchase = CreatePurchaseUseCase(repo),
        )
        vm.onIntent(AddPurchaseIntent.NameChanged("Pasteis"))
        vm.onIntent(AddPurchaseIntent.AmountChanged("4.50"))
        vm.onIntent(AddPurchaseIntent.Submit)

        assertEquals("Conflict", vm.state.value.error?.title)
        vm.onCleared()
    }
}
