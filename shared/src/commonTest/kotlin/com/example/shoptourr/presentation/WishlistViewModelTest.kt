package com.example.shoptourr.presentation

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.usecase.ConvertWishlistItemToPurchaseUseCase
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.ObserveWishlistUseCase
import com.example.shoptourr.domain.usecase.RefreshWishlistUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakePurchaseRepository
import com.example.shoptourr.fake.FakeTripRepository
import com.example.shoptourr.fake.FakeUserRepository
import com.example.shoptourr.fake.FakeWishlistRepository
import com.example.shoptourr.presentation.wishlist.WishlistIntent
import com.example.shoptourr.presentation.wishlist.WishlistViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val tile = WishlistItem(
        id = "w1",
        name = "Tile",
        city = "Lisbon",
        targetPrice = Money.parse("5.00", "EUR"),
        createdAt = "2026-01-01T00:00:00Z",
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads items and can add`() = runTest {
        val repo = FakeWishlistRepository(initial = listOf(tile))
        val vm = createVm(wishes = repo)

        assertEquals(1, vm.state.value.items.size)

        vm.onIntent(WishlistIntent.NameChanged("Pastel"))
        vm.onIntent(WishlistIntent.CityChanged("Lisbon"))
        vm.onIntent(WishlistIntent.PriceChanged("1.20"))
        vm.onIntent(WishlistIntent.Add)

        assertEquals(2, vm.state.value.items.size)
        assertTrue(vm.state.value.items.any { it.name == "Pastel" })
        assertEquals("", vm.state.value.nameDraft)
        vm.onCleared()
    }

    @Test
    fun `bought is offered only for the city of the current trip`() = runTest {
        val vm = createVm(
            wishes = FakeWishlistRepository(initial = listOf(tile)),
            home = lisbonHome(),
        )
        assertTrue(vm.state.value.canBuy(tile))
        assertFalse(
            vm.state.value.canBuy(tile.copy(id = "w2", city = "Oslo")),
        )
        vm.onCleared()
    }

    @Test
    fun `bought turns a matching wish into a purchase`() = runTest {
        val wishes = FakeWishlistRepository(initial = listOf(tile))
        val purchases = FakePurchaseRepository()
        val vm = createVm(wishes = wishes, purchases = purchases, home = lisbonHome())
        vm.onIntent(WishlistIntent.Bought("w1"))
        assertEquals(1, purchases.createCalls)
        assertEquals("Tile", purchases.lastDraft?.name)
        assertTrue(wishes.items.none { it.id == "w1" })
        vm.onCleared()
    }

    @Test
    fun `validation error shows field errors`() = runTest {
        val vm = createVm()
        vm.onIntent(WishlistIntent.Add)
        assertEquals("validation_name_required", vm.state.value.fieldErrors.name)
        assertEquals("validation_city_required", vm.state.value.fieldErrors.city)
        assertEquals("validation_amount_required", vm.state.value.fieldErrors.price)
        assertEquals(null, vm.state.value.error)
        vm.onCleared()
    }

    private fun lisbonHome() = HomeSnapshot(
        userName = "Mila",
        currentTripCity = "Lisbon",
        upcomingCount = 0,
        archiveCount = 0,
        currentTripId = "lisbon",
    )

    private fun createVm(
        wishes: FakeWishlistRepository = FakeWishlistRepository(),
        purchases: FakePurchaseRepository = FakePurchaseRepository(),
        home: HomeSnapshot = HomeSnapshot("", null, 0, 0),
    ) = WishlistViewModel(
        observeWishlist = ObserveWishlistUseCase(wishes),
        refreshWishlist = RefreshWishlistUseCase(wishes),
        createItem = CreateWishlistItemUseCase(wishes),
        deleteItem = DeleteWishlistItemUseCase(wishes),
        convertToPurchase = ConvertWishlistItemToPurchaseUseCase(
            createPurchase = CreatePurchaseUseCase(purchases),
            deleteWishlistItem = DeleteWishlistItemUseCase(wishes),
        ),
        observeHome = ObserveHomeUseCase(
            authRepository = FakeAuthRepository(loggedInOverride = true),
            tripRepository = FakeTripRepository(home),
            userRepository = FakeUserRepository(),
        ),
    )
}
