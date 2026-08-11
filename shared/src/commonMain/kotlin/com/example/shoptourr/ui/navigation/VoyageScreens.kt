package com.example.shoptourr.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.shoptourr.presentation.alerts.AlertsViewModel
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.diary.DiaryViewModel
import com.example.shoptourr.presentation.export.ExportViewModel
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
import com.example.shoptourr.ui.export.ExportScreen
import com.example.shoptourr.ui.home.HomeScreen
import com.example.shoptourr.ui.map.RouteScreen
import com.example.shoptourr.ui.profile.ProfileScreen
import com.example.shoptourr.ui.purchase.AddPurchaseScreen
import com.example.shoptourr.ui.stats.StatsScreen
import com.example.shoptourr.ui.taxfree.TaxFreeScreen
import com.example.shoptourr.ui.trip.NewTripScreen
import com.example.shoptourr.ui.trip.TripDetailScreen
import com.example.shoptourr.ui.wishlist.WishlistScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

object LoginVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<AuthViewModel>()
        LoginScreen(
            viewModel = viewModel,
            onLoggedIn = { navigator.replaceAll(HomeVoyageScreen) },
        )
    }
}

object HomeVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<HomeViewModel>()
        HomeScreen(
            viewModel = viewModel,
            onCreateTrip = { navigator.push(NewTripVoyageScreen) },
            onOpenTrip = { tripId -> navigator.push(TripDetailVoyageScreen(tripId)) },
            onAddPurchase = { tripId -> navigator.push(AddPurchaseVoyageScreen(tripId)) },
            onOpenProfile = { navigator.push(ProfileVoyageScreen) },
            onOpenWishlist = { navigator.push(WishlistVoyageScreen) },
        )
    }
}

object NewTripVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<NewTripViewModel>()
        NewTripScreen(
            viewModel = viewModel,
            onCreated = { navigator.pop() },
            onBack = { navigator.pop() },
        )
    }
}

object ProfileVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<ProfileViewModel>()
        ProfileScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

object WishlistVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<WishlistViewModel>()
        WishlistScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

data class TripDetailVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<TripDetailViewModel> { parametersOf(tripId) }
        TripDetailScreen(
            viewModel = viewModel,
            onAddPurchase = { id -> navigator.push(AddPurchaseVoyageScreen(id)) },
            onOpenDiary = { id -> navigator.push(DiaryVoyageScreen(id)) },
            onOpenTaxFree = { id -> navigator.push(TaxFreeVoyageScreen(id)) },
            onOpenAlerts = { id -> navigator.push(AlertsVoyageScreen(id)) },
            onOpenMap = { id -> navigator.push(RouteVoyageScreen(id)) },
            onOpenStats = { id -> navigator.push(StatsVoyageScreen(id)) },
            onOpenExport = { id -> navigator.push(ExportVoyageScreen(id)) },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
            onBack = { navigator.pop() },
        )
    }
}

data class AddPurchaseVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<AddPurchaseViewModel> { parametersOf(tripId) }
        AddPurchaseScreen(
            viewModel = viewModel,
            onCreated = { navigator.pop() },
            onBack = { navigator.pop() },
        )
    }
}

data class DiaryVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<DiaryViewModel> { parametersOf(tripId) }
        DiaryScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

data class TaxFreeVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<TaxFreeViewModel> { parametersOf(tripId) }
        TaxFreeScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

data class AlertsVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<AlertsViewModel> { parametersOf(tripId) }
        AlertsScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

data class RouteVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<RouteViewModel> { parametersOf(tripId) }
        RouteScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

data class StatsVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<StatsViewModel> { parametersOf(tripId) }
        StatsScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}

data class ExportVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<ExportViewModel> { parametersOf(tripId) }
        ExportScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(LoginVoyageScreen) },
        )
    }
}
