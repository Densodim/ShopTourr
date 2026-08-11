package com.example.shoptourr

import androidx.compose.ui.window.ComposeUIViewController
import com.example.shoptourr.data.connectivity.IosConnectivityMonitor
import com.example.shoptourr.data.local.DatabaseDriverFactory
import com.example.shoptourr.data.local.DiaryLocalStore
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightDiaryLocalStore
import com.example.shoptourr.data.local.SqlDelightPurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightTripLocalStore
import com.example.shoptourr.data.local.SqlDelightWishlistLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.local.WishlistLocalStore
import com.example.shoptourr.data.local.createVoyageDatabase
import com.example.shoptourr.data.settings.IosKeychainSecureStore
import com.example.shoptourr.data.settings.SecureKeyValueStore
import com.example.shoptourr.data.settings.SecureTokenStore
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.SqlDelightSyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.di.initKoin
import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoinOrNull
import platform.UIKit.UIViewController

private val iosDatabaseModule = module {
    single { DatabaseDriverFactory() }
    single { createVoyageDatabase(get()) }
    single<TripLocalStore> { SqlDelightTripLocalStore(get()) }
    single<PurchaseLocalStore> { SqlDelightPurchaseLocalStore(get()) }
    single<WishlistLocalStore> { SqlDelightWishlistLocalStore(get()) }
    single<DiaryLocalStore> { SqlDelightDiaryLocalStore(get()) }
    single<SyncOutbox> { SqlDelightSyncOutbox(get()) }
    single<ConnectivityMonitor> { IosConnectivityMonitor() }
    single<SecureKeyValueStore> { IosKeychainSecureStore() }
    single<TokenStore> {
        SecureTokenStore(
            secure = get(),
            legacy = SettingsTokenStore(get<Settings>()),
        )
    }
}

fun MainViewController(): UIViewController {
    if (getKoinOrNull() == null) {
        initKoin(
            config = AppConfig(),
            extraModules = listOf(iosDatabaseModule),
        )
    }
    return ComposeUIViewController { App() }
}
