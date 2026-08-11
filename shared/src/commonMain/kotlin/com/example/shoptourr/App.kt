package com.example.shoptourr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.ui.auth.LoginScreen
import com.example.shoptourr.ui.home.HomeScreen
import com.example.shoptourr.ui.theme.VoyageTheme
import org.koin.compose.koinInject

private sealed interface AppDestination {
    data object Login : AppDestination
    data object Home : AppDestination
}

@Composable
@Preview
fun App() {
    VoyageTheme {
        val tokenStore = koinInject<TokenStore>()
        var destination by remember {
            mutableStateOf<AppDestination>(
                if (tokenStore.isLoggedIn()) AppDestination.Home else AppDestination.Login
            )
        }

        when (destination) {
            AppDestination.Login -> {
                val authViewModel = koinInject<AuthViewModel>()
                LoginScreen(
                    viewModel = authViewModel,
                    onLoggedIn = { destination = AppDestination.Home },
                )
            }
            AppDestination.Home -> {
                val homeViewModel = koinInject<HomeViewModel>()
                HomeScreen(viewModel = homeViewModel)
            }
        }
    }
}

private fun TokenStore.isLoggedIn(): Boolean = accessToken() != null
