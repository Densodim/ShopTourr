package com.example.shoptourr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.shoptourr.data.sync.SyncScheduler
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.ui.i18n.VoyageLocaleProvider
import com.example.shoptourr.ui.navigation.MainShellVoyageScreen
import com.example.shoptourr.ui.navigation.WelcomeVoyageScreen
import com.example.shoptourr.ui.theme.VoyageTheme
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    VoyageTheme {
        val syncScheduler = koinInject<SyncScheduler>()
        val appScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            syncScheduler.start(appScope)
        }

        val isLoggedIn = koinInject<IsLoggedInUseCase>()
        VoyageLocaleProvider {
            Navigator(
                screen = if (isLoggedIn()) MainShellVoyageScreen else WelcomeVoyageScreen,
            ) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
