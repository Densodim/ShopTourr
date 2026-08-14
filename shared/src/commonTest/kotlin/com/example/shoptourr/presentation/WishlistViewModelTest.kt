package com.example.shoptourr.presentation

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.WishlistItem
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.ObserveWishlistUseCase
import com.example.shoptourr.domain.usecase.RefreshWishlistUseCase
import com.example.shoptourr.fake.FakeWishlistRepository
import com.example.shoptourr.presentation.wishlist.WishlistIntent
import com.example.shoptourr.presentation.wishlist.WishlistViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads items and can add`() = runTest {
        val repo = FakeWishlistRepository(
            initial = listOf(
                WishlistItem(
                    id = "w1",
                    name = "Tile",
                    city = "Lisbon",
                    targetPrice = Money.parse("5.00", "EUR"),
                    createdAt = "2026-01-01T00:00:00Z",
                )
            )
        )
        val vm = WishlistViewModel(
            observeWishlist = ObserveWishlistUseCase(repo),
            refreshWishlist = RefreshWishlistUseCase(repo),
            createItem = CreateWishlistItemUseCase(repo),
            deleteItem = DeleteWishlistItemUseCase(repo),
        )

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
    fun `validation error shows field errors`() = runTest {
        val vm = WishlistViewModel(
            observeWishlist = ObserveWishlistUseCase(FakeWishlistRepository()),
            refreshWishlist = RefreshWishlistUseCase(FakeWishlistRepository()),
            createItem = CreateWishlistItemUseCase(FakeWishlistRepository()),
            deleteItem = DeleteWishlistItemUseCase(FakeWishlistRepository()),
        )
        vm.onIntent(WishlistIntent.Add)
        assertEquals("validation_name_required", vm.state.value.fieldErrors.name)
        assertEquals("validation_city_required", vm.state.value.fieldErrors.city)
        assertEquals("validation_amount_required", vm.state.value.fieldErrors.price)
        assertEquals(null, vm.state.value.error)
        vm.onCleared()
    }
}
