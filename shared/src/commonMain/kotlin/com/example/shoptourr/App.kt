package com.example.shoptourr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.shoptourr.data.sync.SyncScheduler
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.presentation.forceupdate.ForceUpdateViewModel
import com.example.shoptourr.ui.forceupdate.ForceUpdateGate
import com.example.shoptourr.ui.i18n.VoyageLocaleProvider
import com.example.shoptourr.ui.navigation.MainShellVoyageScreen
import com.example.shoptourr.ui.navigation.WelcomeVoyageScreen
import com.example.shoptourr.ui.navigation.applyDeepLink
import com.example.shoptourr.ui.testing.VoyageTestTagRoot
import com.example.shoptourr.ui.navigation.rememberVoyageViewModel
import com.example.shoptourr.ui.theme.VoyageTheme
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    VoyageTheme {
        VoyageTestTagRoot {
        val syncScheduler = koinInject<SyncScheduler>()
        val forceUpdateViewModel = rememberVoyageViewModel<ForceUpdateViewModel>()
        val pendingDeepLinks = koinInject<PendingDeepLinkStore>()
        val isLoggedIn = koinInject<IsLoggedInUseCase>()
        val appScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            syncScheduler.start(appScope)
        }

        VoyageLocaleProvider {
            ForceUpdateGate(viewModel = forceUpdateViewModel) {
                Navigator(
                    screen = if (isLoggedIn()) MainShellVoyageScreen else WelcomeVoyageScreen,
                ) { navigator ->
                    LaunchedEffect(pendingDeepLinks, isLoggedIn) {
                        pendingDeepLinks.observe().collect { target ->
                            if (target == null) return@collect
                            if (!isLoggedIn()) return@collect
                            pendingDeepLinks.consume()
                            navigator.applyDeepLink(target)
                        }
                    }
                    SlideTransition(navigator)
                }
            }
        }
        }
    }
}
