package com.example.shoptourr

import android.app.Application
import com.example.shoptourr.data.connectivity.AndroidConnectivityMonitor
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
import com.example.shoptourr.data.settings.AndroidEncryptedSecureStore
import com.example.shoptourr.data.settings.SecureKeyValueStore
import com.example.shoptourr.data.settings.SecureTokenStore
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.SqlDelightSyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.di.initKoin
import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import com.example.shoptourr.domain.push.PushTokenProvider
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module

class VoyageApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            config = AppConfig(),
            extraModules = listOf(androidDatabaseModule),
        ) {
            androidLogger()
            androidContext(this@VoyageApp)
        }
    }
}

private val androidDatabaseModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { createVoyageDatabase(get()) }
    single<TripLocalStore> { SqlDelightTripLocalStore(get()) }
    single<PurchaseLocalStore> { SqlDelightPurchaseLocalStore(get()) }
    single<WishlistLocalStore> { SqlDelightWishlistLocalStore(get()) }
    single<DiaryLocalStore> { SqlDelightDiaryLocalStore(get()) }
    single<SyncOutbox> { SqlDelightSyncOutbox(get()) }
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single<SecureKeyValueStore> { AndroidEncryptedSecureStore(androidContext()) }
    single<TokenStore> {
        SecureTokenStore(
            secure = get(),
            legacy = SettingsTokenStore(get<Settings>()),
        )
    }
    single<PushTokenProvider> { FcmPushTokenProvider(androidContext()) }
}
