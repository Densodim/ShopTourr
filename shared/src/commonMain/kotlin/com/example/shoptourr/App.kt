package com.example.shoptourr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.presentation.alerts.AlertsViewModel
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.diary.DiaryViewModel
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.presentation.map.RouteViewModel
import com.example.shoptourr.presentation.profile.ProfileViewModel
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.presentation.stats.StatsViewModel
import com.example.shoptourr.presentation.taxfree.TaxFreeViewModel
import com.example.shoptourr.presentation.trip.NewTripViewModel
import com.example.shoptourr.presentation.trip.TripDetailViewModel
import com.example.shoptourr.presentation.wishlist.WishlistViewModel
import com.example.shoptourr.ui.alerts.AlertsScreen
import com.example.shoptourr.ui.auth.LoginScreen
import com.example.shoptourr.ui.diary.DiaryScreen
import com.example.shoptourr.ui.home.HomeScreen
import com.example.shoptourr.ui.map.RouteScreen
import com.example.shoptourr.ui.profile.ProfileScreen
import com.example.shoptourr.ui.purchase.AddPurchaseScreen
import com.example.shoptourr.ui.stats.StatsScreen
import com.example.shoptourr.ui.taxfree.TaxFreeScreen
import com.example.shoptourr.ui.theme.VoyageTheme
import com.example.shoptourr.ui.trip.NewTripScreen
import com.example.shoptourr.ui.trip.TripDetailScreen
import com.example.shoptourr.ui.wishlist.WishlistScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

private sealed interface AppDestination {
    data object Login : AppDestination
    data object Home : AppDestination
    data object NewTrip : AppDestination
    data object Profile : AppDestination
    data object Wishlist : AppDestination
    data class TripDetail(val tripId: String) : AppDestination
    data class AddPurchase(val tripId: String, val returnToDetail: Boolean = false) : AppDestination
    data class Diary(val tripId: String) : AppDestination
    data class TaxFree(val tripId: String) : AppDestination
    data class Alerts(val tripId: String) : AppDestination
    data class Route(val tripId: String) : AppDestination
    data class Stats(val tripId: String) : AppDestination
}

@Composable
@Preview
fun App() {
    VoyageTheme {
        val isLoggedIn = koinInject<IsLoggedInUseCase>()
        var destination by remember {
            mutableStateOf<AppDestination>(
                if (isLoggedIn()) AppDestination.Home else AppDestination.Login
            )
        }

        when (val dest = destination) {
            AppDestination.Login -> {
                val authViewModel = koinInject<AuthViewModel>()
                LoginScreen(
                    viewModel = authViewModel,
                    onLoggedIn = { destination = AppDestination.Home },
                )
            }
            AppDestination.Home -> {
                val homeViewModel = koinInject<HomeViewModel>()
                HomeScreen(
                    viewModel = homeViewModel,
                    onCreateTrip = { destination = AppDestination.NewTrip },
                    onOpenTrip = { tripId -> destination = AppDestination.TripDetail(tripId) },
                    onAddPurchase = { tripId -> destination = AppDestination.AddPurchase(tripId) },
                    onOpenProfile = { destination = AppDestination.Profile },
                    onOpenWishlist = { destination = AppDestination.Wishlist },
                )
            }
            AppDestination.NewTrip -> {
                val newTripViewModel = koinInject<NewTripViewModel>()
                NewTripScreen(
                    viewModel = newTripViewModel,
                    onCreated = { destination = AppDestination.Home },
                    onBack = { destination = AppDestination.Home },
                )
            }
            AppDestination.Profile -> {
                val profileViewModel = koinInject<ProfileViewModel>()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBack = { destination = AppDestination.Home },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
            AppDestination.Wishlist -> {
                val wishlistViewModel = koinInject<WishlistViewModel>()
                WishlistScreen(
                    viewModel = wishlistViewModel,
                    onBack = { destination = AppDestination.Home },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
            is AppDestination.TripDetail -> {
                val tripDetailViewModel = koinInject<TripDetailViewModel> {
                    parametersOf(dest.tripId)
                }
                TripDetailScreen(
                    viewModel = tripDetailViewModel,
                    onAddPurchase = { tripId ->
                        destination = AppDestination.AddPurchase(tripId, returnToDetail = true)
                    },
                    onOpenDiary = { tripId -> destination = AppDestination.Diary(tripId) },
                    onOpenTaxFree = { tripId -> destination = AppDestination.TaxFree(tripId) },
                    onOpenAlerts = { tripId -> destination = AppDestination.Alerts(tripId) },
                    onOpenMap = { tripId -> destination = AppDestination.Route(tripId) },
                    onOpenStats = { tripId -> destination = AppDestination.Stats(tripId) },
                    onBack = { destination = AppDestination.Home },
                )
            }
            is AppDestination.AddPurchase -> {
                val addPurchaseViewModel = koinInject<AddPurchaseViewModel> {
                    parametersOf(dest.tripId)
                }
                AddPurchaseScreen(
                    viewModel = addPurchaseViewModel,
                    onCreated = {
                        destination = if (dest.returnToDetail) {
                            AppDestination.TripDetail(dest.tripId)
                        } else {
                            AppDestination.Home
                        }
                    },
                    onBack = {
                        destination = if (dest.returnToDetail) {
                            AppDestination.TripDetail(dest.tripId)
                        } else {
                            AppDestination.Home
                        }
                    },
                )
            }
            is AppDestination.Diary -> {
                val diaryViewModel = koinInject<DiaryViewModel> { parametersOf(dest.tripId) }
                DiaryScreen(
                    viewModel = diaryViewModel,
                    onBack = { destination = AppDestination.TripDetail(dest.tripId) },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
            is AppDestination.TaxFree -> {
                val taxFreeViewModel = koinInject<TaxFreeViewModel> { parametersOf(dest.tripId) }
                TaxFreeScreen(
                    viewModel = taxFreeViewModel,
                    onBack = { destination = AppDestination.TripDetail(dest.tripId) },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
            is AppDestination.Alerts -> {
                val alertsViewModel = koinInject<AlertsViewModel> { parametersOf(dest.tripId) }
                AlertsScreen(
                    viewModel = alertsViewModel,
                    onBack = { destination = AppDestination.TripDetail(dest.tripId) },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
            is AppDestination.Route -> {
                val routeViewModel = koinInject<RouteViewModel> { parametersOf(dest.tripId) }
                RouteScreen(
                    viewModel = routeViewModel,
                    onBack = { destination = AppDestination.TripDetail(dest.tripId) },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
            is AppDestination.Stats -> {
                val statsViewModel = koinInject<StatsViewModel> { parametersOf(dest.tripId) }
                StatsScreen(
                    viewModel = statsViewModel,
                    onBack = { destination = AppDestination.TripDetail(dest.tripId) },
                    onLoggedOut = { destination = AppDestination.Login },
                )
            }
        }
    }
}
