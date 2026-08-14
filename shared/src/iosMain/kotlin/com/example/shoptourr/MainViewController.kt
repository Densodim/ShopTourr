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
import com.example.shoptourr.data.local.SqlDelightLocalCacheInventory
import com.example.shoptourr.data.local.LocalCacheInventory
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
import com.example.shoptourr.data.platform.StaticAppBuildInfo
import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.russhwolf.settings.Settings
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import com.example.shoptourr.epochMillis
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoinOrNull
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

private val iosDatabaseModule = module {
    single { IosKeychainSecureStore() } bind SecureKeyValueStore::class
    single { DatabaseDriverFactory(get()) }
    single { createVoyageDatabase(get()) }
    single { SqlDelightTripLocalStore(get()) } bind TripLocalStore::class
    single { SqlDelightPurchaseLocalStore(get()) } bind PurchaseLocalStore::class
    single { SqlDelightWishlistLocalStore(get()) } bind WishlistLocalStore::class
    single { SqlDelightDiaryLocalStore(get()) } bind DiaryLocalStore::class
    single { SqlDelightTaxFreeLocalStore(get()) } bind TaxFreeLocalStore::class
    single { SqlDelightAlertsLocalStore(get()) } bind AlertsLocalStore::class
    single { SqlDelightRouteLocalStore(get()) } bind RouteLocalStore::class
    single { SqlDelightStatsLocalStore(get()) } bind StatsLocalStore::class
    single { SqlDelightExportLocalStore(get()) } bind ExportLocalStore::class
    single { SqlDelightLocalCacheInventory(get()) } bind LocalCacheInventory::class
    single { SqlDelightSyncOutbox(get()) } bind SyncOutbox::class
    single { SqlDelightAnalyticsEventQueue(get()) } bind AnalyticsEventQueue::class
    single { NoOpAnalyticsSink } bind AnalyticsSink::class
    single {
        QueuedAnalytics(
            queue = get(),
            sink = get(),
            isOnline = { true },
            clock = { epochMillis() },
        )
    } bind Analytics::class
    single { IosConnectivityMonitor() } bind ConnectivityMonitor::class
    single {
        SecureTokenStore(
            secure = get(),
            legacy = SettingsTokenStore(get<Settings>()),
        )
    } bind TokenStore::class
    single {
        val raw = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
        val buildNumber = raw?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        @OptIn(ExperimentalNativeApi::class)
        StaticAppBuildInfo(
            platform = ClientPlatform.IOS,
            buildNumber = buildNumber,
            isReleaseBuild = !Platform.isDebugBinary,
        )
    } bind AppBuildInfo::class
}

fun MainViewController(): UIViewController {
    if (getKoinOrNull() == null) {
        @OptIn(ExperimentalNativeApi::class)
        initKoin(
            config = AppConfig.forClient(
                isReleaseBuild = !Platform.isDebugBinary,
                platform = ClientPlatform.IOS,
            ),
            extraModules = listOf(iosDatabaseModule),
        )
    }
    return ComposeUIViewController { App() }
}
