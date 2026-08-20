package com.example.shoptourr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.lifecycle.LocalNavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.NavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleContentProvider
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.example.shoptourr.data.media.VoyageImageCaches
import com.example.shoptourr.data.sync.SyncScheduler
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.presentation.forceupdate.ForceUpdateViewModel
import com.example.shoptourr.presentation.lock.AppLockViewModel
import com.example.shoptourr.ui.forceupdate.ForceUpdateGate
import com.example.shoptourr.ui.lock.AppLockGate
import com.example.shoptourr.ui.i18n.VoyageLocaleProvider
import com.example.shoptourr.ui.navigation.MainShellVoyageScreen
import com.example.shoptourr.ui.navigation.WelcomeVoyageScreen
import com.example.shoptourr.ui.navigation.applyDeepLink
import com.example.shoptourr.ui.testing.VoyageTestTagRoot
import com.example.shoptourr.ui.navigation.rememberVoyageViewModel
import com.example.shoptourr.ui.theme.VoyageTheme
import okio.FileSystem
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .diskCache {
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "voyage_image_cache")
                    .maxSizeBytes(32L * 1024 * 1024)
                    .build()
            }
            .build()
            .also { VoyageImageCaches.loader = it }
    }
    VoyageTheme {
        VoyageTestTagRoot {
        val syncScheduler = koinInject<SyncScheduler>()
        val forceUpdateViewModel = rememberVoyageViewModel<ForceUpdateViewModel>()
        val appLockViewModel = koinInject<AppLockViewModel>()
        val pendingDeepLinks = koinInject<PendingDeepLinkStore>()
        val isLoggedIn = koinInject<IsLoggedInUseCase>()
        val appScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            syncScheduler.start(appScope)
        }

        VoyageLocaleProvider {
            ForceUpdateGate(viewModel = forceUpdateViewModel) {
            AppLockGate(viewModel = appLockViewModel) {
                CompositionLocalProvider(
                    LocalNavigatorScreenLifecycleProvider provides NoScreenLifecycleProvider,
                ) {
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
    }
}

/**
 * Voyager 1.0.1 gives every Android screen its own `LifecycleRegistry`, and
 * androidx.lifecycle 2.9+ throws on a DESTROYED → STARTED transition — which is
 * exactly what `replaceAll()` triggers while the outgoing screen is still sliding
 * out, crashing the app on sign-in. Screens here hold no per-screen ViewModelStore
 * (Koin builds every [com.example.shoptourr.presentation.base.BaseViewModel]), so
 * dropping the per-screen lifecycle is enough: `LocalLifecycleOwner` and
 * `LocalViewModelStoreOwner` fall back to the hosting Activity.
 */
private object NoScreenLifecycleProvider : NavigatorScreenLifecycleProvider {
    override fun provide(screen: Screen): List<ScreenLifecycleContentProvider> = emptyList()
}
