package com.example.shoptourr

import android.app.Application
import android.content.pm.ApplicationInfo
import com.example.shoptourr.analytics.Analytics
import com.example.shoptourr.analytics.AnalyticsConsentStore
import com.example.shoptourr.analytics.AnalyticsEventQueue
import com.example.shoptourr.analytics.AnalyticsSink
import com.example.shoptourr.analytics.NoOpAnalyticsSink
import com.example.shoptourr.analytics.QueuedAnalytics
import com.example.shoptourr.analytics.SqlDelightAnalyticsEventQueue
import com.example.shoptourr.data.connectivity.AndroidConnectivityMonitor
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
import com.example.shoptourr.data.push.RegisteredPushDeviceStore
import com.example.shoptourr.data.push.SecureRegisteredPushDeviceStore
import com.example.shoptourr.data.settings.AndroidEncryptedSecureStore
import com.example.shoptourr.data.settings.SecureKeyValueStore
import com.example.shoptourr.data.settings.SecureTokenStore
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.BackgroundSyncScheduler
import com.example.shoptourr.data.sync.SqlDelightSyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.sync.AndroidBackgroundSyncScheduler
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.di.initKoin
import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import com.example.shoptourr.data.platform.StaticAppBuildInfo
import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.example.shoptourr.domain.push.PushTokenProvider
import com.example.shoptourr.observability.NoOpObservability
import com.example.shoptourr.observability.Observability
import com.example.shoptourr.observability.createDefaultTracer
import com.example.shoptourr.observability.trace
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module

class VoyageApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val tracer = createDefaultTracer()
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val sentryDsn = BuildConfig.SENTRY_DSN.takeIf { it.isNotBlank() }
        initSentryIfConfigured(this, sentryDsn, debuggable)
        tracer.trace("VoyageApp.initKoin") {
            initKoin(
                config = AppConfig.forClient(
                    isReleaseBuild = !debuggable,
                    platform = ClientPlatform.ANDROID,
                ).copy(sentryDsn = sentryDsn),
                extraModules = listOf(androidDatabaseModule),
            ) {
                androidLogger()
                androidContext(this@VoyageApp)
            }
        }
        tracer.trace("VoyageApp.scheduleBackgroundSync") {
            org.koin.core.context.GlobalContext.get().get<BackgroundSyncScheduler>().schedule()
        }
    }
}

private val androidDatabaseModule = module {
    single<SecureKeyValueStore> { AndroidEncryptedSecureStore(androidContext()) }
    single { DatabaseDriverFactory(androidContext(), get()) }
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
    single<LocalCacheInventory> { SqlDelightLocalCacheInventory(get()) }
    single<SyncOutbox> { SqlDelightSyncOutbox(get()) }
    single<AnalyticsEventQueue> { SqlDelightAnalyticsEventQueue(get()) }
    single<AnalyticsSink> { NoOpAnalyticsSink }
    single<Analytics> {
        QueuedAnalytics(
            queue = get(),
            sink = get(),
            isOnline = { get<ConnectivityMonitor>().currentIsOnline() },
            clock = { System.currentTimeMillis() },
            consent = { get<AnalyticsConsentStore>().isGranted() },
        )
    }
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single<TokenStore> {
        SecureTokenStore(
            secure = get(),
            legacy = SettingsTokenStore(get<Settings>()),
        )
    }
    single<RegisteredPushDeviceStore> { SecureRegisteredPushDeviceStore(get()) }
    single<PushTokenProvider> { FcmPushTokenProvider(androidContext()) }
    single<BackgroundSyncScheduler> { AndroidBackgroundSyncScheduler(androidContext()) }
    single<AppBuildInfo> {
        val context = androidContext()
        val buildNumber = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
        }.getOrElse { 1L }.toInt().coerceAtLeast(1)
        val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        StaticAppBuildInfo(
            platform = ClientPlatform.ANDROID,
            buildNumber = buildNumber,
            isReleaseBuild = !debuggable,
        )
    }
    single<Observability> {
        val dsn = get<AppConfig>().sentryDsn
        if (dsn.isNullOrBlank()) NoOpObservability else AndroidSentryObservability()
    }
}
