package com.example.shoptourr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.presentation.trip.NewTripViewModel
import com.example.shoptourr.ui.auth.LoginScreen
import com.example.shoptourr.ui.home.HomeScreen
import com.example.shoptourr.ui.purchase.AddPurchaseScreen
import com.example.shoptourr.ui.theme.VoyageTheme
import com.example.shoptourr.ui.trip.NewTripScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

private sealed interface AppDestination {
    data object Login : AppDestination
    data object Home : AppDestination
    data object NewTrip : AppDestination
    data class AddPurchase(val tripId: String) : AppDestination
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
                    onAddPurchase = { tripId ->
                        destination = AppDestination.AddPurchase(tripId)
                    },
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
            is AppDestination.AddPurchase -> {
                val addPurchaseViewModel = koinInject<AddPurchaseViewModel> {
                    parametersOf(dest.tripId)
                }
                AddPurchaseScreen(
                    viewModel = addPurchaseViewModel,
                    onCreated = { destination = AppDestination.Home },
                    onBack = { destination = AppDestination.Home },
                )
            }
        }
    }
}
