package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.FetchReceiptOcrUseCase
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import com.example.shoptourr.fake.FakeMediaRepository
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.fake.FakeTripRepository
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
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AddPurchaseViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val lisbonTravelers = listOf(
        Traveler(id = "me", name = "Вы", colorHex = "#FFD84D", avatarGlyph = "M", isOwner = true),
        Traveler(id = "a", name = "Alex", colorHex = "#7EE8C4", avatarGlyph = "A", isOwner = false),
        Traveler(id = "k", name = "Kira", colorHex = "#F59890", avatarGlyph = "K", isOwner = false),
    )

    private val lisbonTrip = TripSummary(
        id = "lisbon",
        city = "Lisbon",
        country = "Portugal",
        status = TripStatus.ACTIVE,
        startDate = "2026-04-12",
        endDate = "2026-04-19",
        budget = Money.parse("1200.00", "EUR"),
        spent = Money.zero("EUR"),
        purchaseCount = 0,
        travelers = lisbonTravelers,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(
        purchaseRepo: FakePurchaseRepository = FakePurchaseRepository(),
        mediaRepo: FakeMediaRepository = FakeMediaRepository(),
        tripRepo: FakeTripRepository = FakeTripRepository(trips = listOf(lisbonTrip)),
    ) = AddPurchaseViewModel(
        tripId = "lisbon",
        createPurchase = CreatePurchaseUseCase(purchaseRepo),
        uploadReceipt = UploadReceiptUseCase(mediaRepo),
        fetchReceiptOcr = FetchReceiptOcrUseCase(mediaRepo),
        observeTripDetail = ObserveTripDetailUseCase(tripRepo, purchaseRepo),
    )

    @Test
    fun `successful submit emits created event`() = runTest {
        val repo = FakePurchaseRepository()
        val viewModel = vm(purchaseRepo = repo)

        viewModel.onIntent(AddPurchaseIntent.NameChanged("Pasteis"))
        viewModel.onIntent(AddPurchaseIntent.AmountChanged("4.50"))
        viewModel.onIntent(AddPurchaseIntent.CategoryChanged(PurchaseCategory.FOOD))
        viewModel.onIntent(AddPurchaseIntent.PlaceChanged("Belem"))

        viewModel.events.test {
            viewModel.onIntent(AddPurchaseIntent.Submit)
            assertIs<AddPurchaseUiEvent.Created>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertEquals(1, repo.createCalls)
        viewModel.onCleared()
    }

    @Test
    fun `validation failure maps to UiError`() = runTest {
        val viewModel = vm()
        viewModel.onIntent(AddPurchaseIntent.Submit)

        val error = viewModel.state.value.error
        assertIs<UiError>(error)
        assertEquals("Проверьте поля", error.title)
        assertEquals("name", error.message)
        viewModel.onCleared()
    }

    @Test
    fun `repository failure maps to UiError`() = runTest {
        val repo = FakePurchaseRepository(createError = AppError.Conflict)
        val viewModel = vm(purchaseRepo = repo)
        viewModel.onIntent(AddPurchaseIntent.NameChanged("Pasteis"))
        viewModel.onIntent(AddPurchaseIntent.AmountChanged("4.50"))
        viewModel.onIntent(AddPurchaseIntent.Submit)

        assertEquals("Конфликт", viewModel.state.value.error?.title)
        viewModel.onCleared()
    }

    @Test
    fun `attach receipt uploads and applies ocr`() = runTest {
        val media = FakeMediaRepository()
        val viewModel = vm(mediaRepo = media)
        viewModel.onIntent(
            AddPurchaseIntent.AttachReceipt(
                contentType = "image/jpeg",
                bytes = byteArrayOf(1, 2, 3),
            ),
        )
        assertEquals("media-1", viewModel.state.value.receiptMediaId)
        assertEquals("Pasteis de Belem", viewModel.state.value.ocr?.suggestedName)
        viewModel.onIntent(AddPurchaseIntent.ApplyOcr)
        assertEquals("Pasteis de Belem", viewModel.state.value.name)
        assertEquals("4.50", viewModel.state.value.amount)
        assertEquals(1, media.createCalls)
        assertEquals(1, media.ocrCalls)
        viewModel.onCleared()
    }

    @Test
    fun `loads trip travelers and defaults split to owner`() = runTest {
        val viewModel = vm()
        advanceUntilIdle()

        assertEquals(lisbonTravelers, viewModel.state.value.travelers)
        assertEquals(listOf("me"), viewModel.state.value.selectedTravelerIds)
        viewModel.onCleared()
    }

    @Test
    fun `toggle traveler updates selection and submit sends split ids`() = runTest {
        val repo = FakePurchaseRepository()
        val viewModel = vm(purchaseRepo = repo)
        advanceUntilIdle()

        viewModel.onIntent(AddPurchaseIntent.ToggleTraveler("a"))
        viewModel.onIntent(AddPurchaseIntent.ToggleTraveler("k"))
        assertEquals(listOf("me", "a", "k"), viewModel.state.value.selectedTravelerIds)

        viewModel.onIntent(AddPurchaseIntent.AmountChanged("32.50"))
        assertEquals(Money(minorUnits = 1084, currency = "EUR"), viewModel.state.value.yourShare)

        viewModel.onIntent(AddPurchaseIntent.NameChanged("Dinner"))
        viewModel.onIntent(AddPurchaseIntent.Submit)
        advanceUntilIdle()

        assertEquals(listOf("me", "a", "k"), repo.lastDraft?.splitWithTravelerIds)
        viewModel.onCleared()
    }

    @Test
    fun `cannot deselect last traveler`() = runTest {
        val viewModel = vm()
        advanceUntilIdle()

        viewModel.onIntent(AddPurchaseIntent.ToggleTraveler("me"))
        assertEquals(listOf("me"), viewModel.state.value.selectedTravelerIds)
        assertTrue(viewModel.state.value.selectedTravelerIds.contains("me"))
        viewModel.onCleared()
    }
}
