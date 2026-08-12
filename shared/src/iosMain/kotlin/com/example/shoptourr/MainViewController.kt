package com.example.shoptourr

import androidx.compose.ui.window.ComposeUIViewController
import com.example.shoptourr.analytics.Analytics
import com.example.shoptourr.analytics.AnalyticsEventQueue
import com.example.shoptourr.analytics.AnalyticsSink
import com.example.shoptourr.analytics.NoOpAnalyticsSink
import com.example.shoptourr.analytics.QueuedAnalytics
import com.example.shoptourr.analytics.SqlDelightAnalyticsEventQueue
import com.example.shoptourr.data.connectivity.IosConnectivityMonitor
import com.example.shoptourr.data.local.AlertsLocalStore
import com.example.shoptourr.data.local.DatabaseDriverFactory
import com.example.shoptourr.data.local.DiaryLocalStore
import com.example.shoptourr.data.local.ExportLocalStore
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.RouteLocalStore
import com.example.shoptourr.data.local.SqlDelightAlertsLocalStore
import com.example.shoptourr.data.local.SqlDelightDiaryLocalStore
import com.example.shoptourr.data.local.SqlDelightExportLocalStore
import com.example.shoptourr.data.local.SqlDelightPurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightRouteLocalStore
import com.example.shoptourr.data.local.SqlDelightStatsLocalStore
import com.example.shoptourr.data.local.SqlDelightTaxFreeLocalStore
import com.example.shoptourr.data.local.SqlDelightTripLocalStore
import com.example.shoptourr.data.local.SqlDelightWishlistLocalStore
import com.example.shoptourr.data.local.StatsLocalStore
import com.example.shoptourr.data.local.TaxFreeLocalStore
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
import kotlinx.datetime.Clock
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
    single<TaxFreeLocalStore> { SqlDelightTaxFreeLocalStore(get()) }
    single<AlertsLocalStore> { SqlDelightAlertsLocalStore(get()) }
    single<RouteLocalStore> { SqlDelightRouteLocalStore(get()) }
    single<StatsLocalStore> { SqlDelightStatsLocalStore(get()) }
    single<ExportLocalStore> { SqlDelightExportLocalStore(get()) }
    single<SyncOutbox> { SqlDelightSyncOutbox(get()) }
    single<AnalyticsEventQueue> { SqlDelightAnalyticsEventQueue(get()) }
    single<AnalyticsSink> { NoOpAnalyticsSink }
    single<Analytics> {
        QueuedAnalytics(
            queue = get(),
            sink = get(),
            isOnline = { true },
            clock = { Clock.System.now().toEpochMilliseconds() },
        )
    }
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
