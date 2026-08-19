package com.example.shoptourr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.shoptourr.presentation.alerts.AlertsViewModel
import com.example.shoptourr.presentation.auth.AuthIntent
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.auth.ForgotPasswordViewModel
import com.example.shoptourr.presentation.auth.ResetPasswordViewModel
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
import com.example.shoptourr.ui.auth.ForgotPasswordScreen
import com.example.shoptourr.ui.auth.ResetPasswordScreen
import com.example.shoptourr.ui.auth.LoginScreen
import com.example.shoptourr.ui.auth.WelcomeScreen
import com.example.shoptourr.ui.components.VoyageTabBar
import com.example.shoptourr.ui.diary.DiaryScreen
import com.example.shoptourr.ui.export.ExportScreen
import com.example.shoptourr.ui.home.HomeScreen
import com.example.shoptourr.ui.legal.AboutScreen
import com.example.shoptourr.ui.legal.PrivacyScreen
import com.example.shoptourr.ui.legal.SupportScreen
import com.example.shoptourr.ui.map.RouteScreen
import com.example.shoptourr.ui.profile.ProfileScreen
import com.example.shoptourr.ui.purchase.AddPurchaseScreen
import com.example.shoptourr.ui.settings.SettingsScreen
import com.example.shoptourr.ui.stats.StatsScreen
import com.example.shoptourr.ui.taxfree.TaxFreeScreen
import com.example.shoptourr.ui.theme.VoyageTokens
import com.example.shoptourr.ui.trip.NewTripScreen
import com.example.shoptourr.ui.trip.TripDetailScreen
import com.example.shoptourr.ui.wishlist.WishlistScreen

object WelcomeVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        WelcomeScreen(
            onSignUp = { navigator.push(LoginVoyageScreen(registerMode = true)) },
            onSignIn = { navigator.push(LoginVoyageScreen(registerMode = false)) },
        )
    }
}

data class LoginVoyageScreen(val registerMode: Boolean = false) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<AuthViewModel>()
        LaunchedEffect(registerMode) {
            viewModel.onIntent(AuthIntent.SetRegisterMode(registerMode))
        }
        LoginScreen(
            viewModel = viewModel,
            onLoggedIn = { navigator.replaceAll(MainShellVoyageScreen) },
            onForgotPassword = { navigator.push(ForgotPasswordVoyageScreen) },
            onBack = if (navigator.canPop) {{ navigator.pop() }} else null,
        )
    }
}

object ForgotPasswordVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<ForgotPasswordViewModel>()
        ForgotPasswordScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onEnterCode = { email -> navigator.push(ResetPasswordVoyageScreen(email = email)) },
        )
    }
}

data class ResetPasswordVoyageScreen(
    val email: String = "",
    val token: String = "",
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<ResetPasswordViewModel>()
        ResetPasswordScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onSignIn = { navigator.replaceAll(LoginVoyageScreen(registerMode = false)) },
            prefillEmail = email,
            prefillToken = token,
        )
    }
}

object MainShellVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var tab by rememberSaveable { mutableStateOf(VoyageTab.Home.name) }
        val current = VoyageTab.entries.firstOrNull { it.name == tab } ?: VoyageTab.Home

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VoyageTokens.bg),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val homeViewModel = rememberVoyageViewModel<HomeViewModel>()
                val wishlistViewModel = rememberVoyageViewModel<WishlistViewModel>()
                val profileViewModel = rememberVoyageViewModel<ProfileViewModel>()
                when (current) {
                    VoyageTab.Home -> {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onCreateTrip = { navigator.push(NewTripVoyageScreen) },
                            onOpenTrip = { tripId -> navigator.push(TripDetailVoyageScreen(tripId)) },
                            onAddPurchase = { tripId -> navigator.push(AddPurchaseVoyageScreen(tripId)) },
                            onOpenMap = { tripId -> navigator.push(RouteVoyageScreen(tripId)) },
                            onOpenStats = { tripId -> navigator.push(StatsVoyageScreen(tripId)) },
                            onOpenProfile = { tab = VoyageTab.Profile.name },
                        )
                    }
                    VoyageTab.Wishlist -> {
                        WishlistScreen(
                            viewModel = wishlistViewModel,
                            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
                        )
                    }
                    VoyageTab.Profile -> {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
                            onOpenSettings = { navigator.push(SettingsVoyageScreen) },
                            onOpenSupport = { navigator.push(SupportVoyageScreen) },
                            onEditProfile = { navigator.push(EditProfileVoyageScreen) },
                        )
                    }
                }
            }
            VoyageTabBar(
                current = current,
                onChange = { tab = it.name },
            )
        }
    }
}

/** @deprecated Prefer [MainShellVoyageScreen]; kept for deep-link restore compatibility. */
object HomeVoyageScreen : Screen {
    @Composable
    override fun Content() {
        MainShellVoyageScreen.Content()
    }
}

object NewTripVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<NewTripViewModel>()
        NewTripScreen(
            viewModel = viewModel,
            onCreated = { navigator.pop() },
            onBack = { navigator.pop() },
        )
    }
}

object SettingsVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<ProfileViewModel>()
        SettingsScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
            onOpenPrivacy = { navigator.push(PrivacyVoyageScreen) },
            onOpenAbout = { navigator.push(AboutVoyageScreen) },
            onOpenSupport = { navigator.push(SupportVoyageScreen) },
            onEditProfile = { navigator.push(EditProfileVoyageScreen) },
        )
    }
}

object EditProfileVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<ProfileViewModel>()
        ProfileScreen(
            viewModel = viewModel,
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
            onBack = { navigator.pop() },
            editMode = true,
        )
    }
}

object PrivacyVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PrivacyScreen(onBack = { navigator.pop() })
    }
}

object AboutVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AboutScreen(onBack = { navigator.pop() })
    }
}

object SupportVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SupportScreen(onBack = { navigator.pop() })
    }
}

object ProfileVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<ProfileViewModel>()
        ProfileScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
            onOpenSettings = { navigator.push(SettingsVoyageScreen) },
            onOpenSupport = { navigator.push(SupportVoyageScreen) },
            onEditProfile = { navigator.push(EditProfileVoyageScreen) },
        )
    }
}

object WishlistVoyageScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<WishlistViewModel>()
        WishlistScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}

data class TripDetailVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<TripDetailViewModel>(tripId)
        TripDetailScreen(
            viewModel = viewModel,
            onAddPurchase = { id -> navigator.push(AddPurchaseVoyageScreen(id)) },
            onOpenDiary = { id -> navigator.push(DiaryVoyageScreen(id)) },
            onOpenTaxFree = { id -> navigator.push(TaxFreeVoyageScreen(id)) },
            onOpenAlerts = { id -> navigator.push(AlertsVoyageScreen(id)) },
            onOpenMap = { id -> navigator.push(RouteVoyageScreen(id)) },
            onOpenStats = { id -> navigator.push(StatsVoyageScreen(id)) },
            onOpenExport = { id -> navigator.push(ExportVoyageScreen(id)) },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
            onBack = { navigator.pop() },
        )
    }
}

data class AddPurchaseVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<AddPurchaseViewModel>(tripId)
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
        val viewModel = rememberVoyageViewModel<DiaryViewModel>(tripId)
        DiaryScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}

data class TaxFreeVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<TaxFreeViewModel>(tripId)
        TaxFreeScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}

data class AlertsVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<AlertsViewModel>(tripId)
        AlertsScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}

data class RouteVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<RouteViewModel>(tripId)
        RouteScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}

data class StatsVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<StatsViewModel>(tripId)
        StatsScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}

data class ExportVoyageScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberVoyageViewModel<ExportViewModel>(tripId)
        ExportScreen(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onLoggedOut = { navigator.replaceAll(WelcomeVoyageScreen) },
        )
    }
}
