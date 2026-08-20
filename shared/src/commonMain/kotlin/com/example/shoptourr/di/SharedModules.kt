package com.example.shoptourr.di

import com.example.shoptourr.data.connectivity.AlwaysOnlineConnectivityMonitor
import com.example.shoptourr.data.hash.createDefaultContentChecksum
import com.example.shoptourr.data.media.FileKitReceiptImageCompressor
import com.example.shoptourr.data.media.SettingsUploadCheckpointStore
import com.example.shoptourr.data.media.UploadCheckpointStore
import com.example.shoptourr.data.push.InMemoryRegisteredPushDeviceStore
import com.example.shoptourr.data.push.RegisteredPushDeviceStore
import com.example.shoptourr.data.remote.KtorAuthTokenCache
import com.example.shoptourr.domain.auth.SocialAuthClient
import com.example.shoptourr.domain.auth.UnavailableSocialAuthClient
import com.example.shoptourr.domain.session.AuthTokenCache
import com.example.shoptourr.data.local.InMemoryAlertsLocalStore
import com.example.shoptourr.data.local.ClientRemoteConfigStore
import com.example.shoptourr.data.local.CompositeLocalSessionStore
import com.example.shoptourr.data.local.InMemoryDiaryLocalStore
import com.example.shoptourr.data.platform.ClientReleasePolicy
import com.example.shoptourr.data.platform.createDefaultAppBuildInfo
import com.example.shoptourr.data.repository.ClientRemoteConfigRepositoryImpl
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.example.shoptourr.domain.repository.ClientRemoteConfigRepository
import com.example.shoptourr.domain.repository.LocalSessionStore
import com.example.shoptourr.domain.usecase.EvaluateForceUpdateUseCase
import com.example.shoptourr.domain.usecase.ObserveFeatureFlagUseCase
import com.example.shoptourr.domain.usecase.RefreshClientRemoteConfigUseCase
import com.example.shoptourr.presentation.forceupdate.ForceUpdateViewModel
import com.example.shoptourr.data.local.InMemoryExportLocalStore
import com.example.shoptourr.data.local.InMemoryPurchaseLocalStore
import com.example.shoptourr.data.local.InMemoryRouteLocalStore
import com.example.shoptourr.data.local.InMemoryStatsLocalStore
import com.example.shoptourr.data.local.InMemoryTaxFreeLocalStore
import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.local.InMemoryWishlistLocalStore
import com.example.shoptourr.data.local.AlertsLocalStore
import com.example.shoptourr.data.local.DiaryLocalStore
import com.example.shoptourr.data.local.ExportLocalStore
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.RouteLocalStore
import com.example.shoptourr.data.local.SettingsClientRemoteConfigStore
import com.example.shoptourr.data.local.SettingsUserLocalStore
import com.example.shoptourr.data.local.StatsLocalStore
import com.example.shoptourr.data.local.TaxFreeLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.local.UserLocalStore
import com.example.shoptourr.data.local.WishlistLocalStore
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.navigation.flushPendingDeepLinkUris
import com.example.shoptourr.data.local.InMemoryLocalCacheInventory
import com.example.shoptourr.data.local.LocalCacheInventory
import com.example.shoptourr.data.sync.BackgroundSyncScheduler
import com.example.shoptourr.data.sync.InMemorySyncConflictNotifier
import com.example.shoptourr.data.sync.NoOpBackgroundSyncScheduler
import com.example.shoptourr.domain.repository.SyncConflictNotifier
import com.example.shoptourr.domain.usecase.AcknowledgeSyncConflictUseCase
import com.example.shoptourr.domain.usecase.EvictLocalCacheUseCase
import com.example.shoptourr.domain.usecase.ObserveSyncConflictsUseCase
import com.example.shoptourr.domain.usecase.RefreshPurchasesUseCase
import com.example.shoptourr.observability.AppTracer
import com.example.shoptourr.observability.Observability
import com.example.shoptourr.observability.ObservabilityFactory
import com.example.shoptourr.observability.createDefaultTracer
import com.example.shoptourr.analytics.Analytics
import com.example.shoptourr.analytics.AnalyticsConsentStore
import com.example.shoptourr.analytics.AnalyticsEventQueue
import com.example.shoptourr.analytics.AnalyticsSink
import com.example.shoptourr.analytics.HttpAnalyticsSink
import com.example.shoptourr.analytics.NoOpAnalytics
import com.example.shoptourr.analytics.SettingsAnalyticsConsentStore
import com.example.shoptourr.security.CertificatePinConfig
import com.example.shoptourr.security.CertificatePinPolicy
import com.example.shoptourr.security.VoyageCertificatePins
import com.example.shoptourr.data.lock.SettingsAppLockStore
import com.example.shoptourr.data.lock.createBiometricAuthenticator
import com.example.shoptourr.data.push.createDefaultPushTokenProvider
import com.example.shoptourr.data.push.createNotificationPermissionGate
import com.example.shoptourr.data.share.createShareSheet
import com.example.shoptourr.domain.lock.AppLockStore
import com.example.shoptourr.domain.lock.BiometricAuthenticator
import com.example.shoptourr.domain.push.NotificationPermissionGate
import com.example.shoptourr.domain.share.ShareSheet
import com.example.shoptourr.data.remote.AlertsApi
import com.example.shoptourr.data.remote.AnalyticsApi
import com.example.shoptourr.data.remote.AuthApi
import com.example.shoptourr.data.remote.DiaryApi
import com.example.shoptourr.data.remote.ExportApi
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.data.remote.MediaApi
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.PushApi
import com.example.shoptourr.data.remote.RouteApi
import com.example.shoptourr.data.remote.StatsApi
import com.example.shoptourr.data.remote.TaxFreeApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.UserApi
import com.example.shoptourr.data.remote.WishlistApi
import com.example.shoptourr.data.remote.createPlatformHttpEngine
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.remote.installVoyageHttpTimeouts
import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.data.repository.AlertsRepositoryImpl
import com.example.shoptourr.data.repository.AuthRepositoryImpl
import com.example.shoptourr.data.repository.DiaryRepositoryImpl
import com.example.shoptourr.data.repository.ExportRepositoryImpl
import com.example.shoptourr.data.repository.MediaRepositoryImpl
import com.example.shoptourr.data.repository.PurchaseRepositoryImpl
import com.example.shoptourr.data.repository.PushRepositoryImpl
import com.example.shoptourr.data.repository.RouteRepositoryImpl
import com.example.shoptourr.data.repository.StatsRepositoryImpl
import com.example.shoptourr.data.repository.SyncRepositoryImpl
import com.example.shoptourr.data.repository.TaxFreeRepositoryImpl
import com.example.shoptourr.data.repository.TripRepositoryImpl
import com.example.shoptourr.data.repository.UserRepositoryImpl
import com.example.shoptourr.data.repository.WishlistRepositoryImpl
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.data.sync.SyncScheduler
import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import com.example.shoptourr.domain.hash.ContentChecksum
import com.example.shoptourr.domain.media.ReceiptImageCompressor
import com.example.shoptourr.domain.push.PushTokenProvider
import com.example.shoptourr.domain.repository.AlertsRepository
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.repository.DiaryRepository
import com.example.shoptourr.domain.repository.ExportRepository
import com.example.shoptourr.domain.repository.MediaRepository
import com.example.shoptourr.domain.repository.PurchaseRepository
import com.example.shoptourr.domain.repository.PushRepository
import com.example.shoptourr.domain.repository.RouteRepository
import com.example.shoptourr.domain.repository.StatsRepository
import com.example.shoptourr.domain.repository.SyncRepository
import com.example.shoptourr.domain.repository.TaxFreeRepository
import com.example.shoptourr.domain.repository.TripRepository
import com.example.shoptourr.domain.repository.UserRepository
import com.example.shoptourr.domain.repository.WishlistRepository
import com.example.shoptourr.domain.usecase.ActivatePremiumUseCase
import com.example.shoptourr.domain.usecase.AddTravelerUseCase
import com.example.shoptourr.domain.usecase.CreateDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.CreateExportUseCase
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.ConvertWishlistItemToPurchaseUseCase
import com.example.shoptourr.domain.usecase.DeleteDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import com.example.shoptourr.domain.usecase.FetchReceiptOcrUseCase
import com.example.shoptourr.domain.usecase.InviteTravelerUseCase
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.domain.usecase.LogoutUseCase
import com.example.shoptourr.domain.usecase.DeleteAccountUseCase
import com.example.shoptourr.domain.usecase.ObserveAlertsUseCase
import com.example.shoptourr.domain.usecase.ObserveConnectivityUseCase
import com.example.shoptourr.domain.usecase.ObserveDiaryUseCase
import com.example.shoptourr.domain.usecase.ObserveExportJobUseCase
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.ObservePreferencesUseCase
import com.example.shoptourr.domain.usecase.ObservePremiumUseCase
import com.example.shoptourr.domain.usecase.ObserveProfileUseCase
import com.example.shoptourr.domain.usecase.ObserveRouteUseCase
import com.example.shoptourr.domain.usecase.ObserveStatsUseCase
import com.example.shoptourr.domain.usecase.ObserveTaxFreeUseCase
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.domain.usecase.ObserveWishlistUseCase
import com.example.shoptourr.domain.usecase.RefreshAlertsUseCase
import com.example.shoptourr.domain.usecase.RefreshDiaryUseCase
import com.example.shoptourr.domain.usecase.RefreshExchangeRateUseCase
import com.example.shoptourr.domain.usecase.RefreshExportJobUseCase
import com.example.shoptourr.domain.usecase.RefreshHomeUseCase
import com.example.shoptourr.domain.usecase.ResolveAddPurchaseDeepLinkUseCase
import com.example.shoptourr.domain.usecase.RefreshPreferencesUseCase
import com.example.shoptourr.domain.usecase.RefreshProfileUseCase
import com.example.shoptourr.domain.usecase.RefreshRouteUseCase
import com.example.shoptourr.domain.usecase.RefreshStatsUseCase
import com.example.shoptourr.domain.usecase.RefreshTaxFreeUseCase
import com.example.shoptourr.domain.usecase.RefreshTripUseCase
import com.example.shoptourr.domain.usecase.RefreshWishlistUseCase
import com.example.shoptourr.domain.usecase.RegisterPushDeviceUseCase
import com.example.shoptourr.domain.usecase.RegisterUseCase
import com.example.shoptourr.domain.usecase.SocialLoginUseCase
import com.example.shoptourr.domain.usecase.RequestPasswordResetUseCase
import com.example.shoptourr.domain.usecase.ResetPasswordUseCase
import com.example.shoptourr.domain.usecase.UnregisterPushDeviceUseCase
import com.example.shoptourr.domain.usecase.UpdatePreferencesUseCase
import com.example.shoptourr.domain.usecase.UpdateProfileUseCase
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import com.example.shoptourr.epochMillis
import io.ktor.client.HttpClient
import kotlinx.datetime.Instant
import org.koin.core.qualifier.named
import com.example.shoptourr.presentation.alerts.AlertsViewModel
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.auth.ForgotPasswordViewModel
import com.example.shoptourr.presentation.auth.ResetPasswordViewModel
import com.example.shoptourr.presentation.diary.DiaryViewModel
import com.example.shoptourr.presentation.export.ExportViewModel
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.presentation.lock.AppLockViewModel
import com.example.shoptourr.presentation.map.RouteViewModel
import com.example.shoptourr.presentation.privacy.PrivacyViewModel
import com.example.shoptourr.presentation.profile.ProfileViewModel
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.presentation.stats.StatsViewModel
import com.example.shoptourr.presentation.taxfree.TaxFreeViewModel
import com.example.shoptourr.presentation.trip.NewTripViewModel
import com.example.shoptourr.presentation.trip.TripDetailViewModel
import com.example.shoptourr.presentation.wishlist.WishlistViewModel
import com.russhwolf.settings.Settings
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import kotlin.random.Random

data class AppConfig(
    val apiBaseUrl: String = PRODUCTION_API_BASE_URL,
    val sentryDsn: String? = null,
    val googleWebClientId: String = "",
    val googleIosClientId: String = "",
    val appleServicesId: String = "",
) {
    companion object {
        const val PRODUCTION_API_BASE_URL = "https://api.shoptourr.com/api"
        /** Android emulator → host loopback. Physical device: `adb reverse tcp:8083 tcp:8083` + [JVM_LOCAL_API]. */
        const val ANDROID_EMULATOR_LOCAL_API = "http://10.0.2.2:8083/api"
        const val IOS_SIMULATOR_LOCAL_API = "http://127.0.0.1:8083/api"
        /** JVM host tests and `adb reverse` against local ShopTourBoot. */
        const val JVM_LOCAL_API = "http://127.0.0.1:8083/api"

        fun forClient(
            isReleaseBuild: Boolean,
            platform: ClientPlatform,
        ): AppConfig = AppConfig(
            apiBaseUrl = if (isReleaseBuild) {
                PRODUCTION_API_BASE_URL
            } else {
                when (platform) {
                    ClientPlatform.ANDROID -> ANDROID_EMULATOR_LOCAL_API
                    ClientPlatform.IOS -> IOS_SIMULATOR_LOCAL_API
                }
            },
        )
    }
}

val dataModule = module {
    single<Settings> { Settings() }
    // Overridden by platform extraModules with SecureTokenStore.
    single<TokenStore> {
        error("TokenStore must be provided by platform extraModules (SecureTokenStore)")
    }
    single<SocialAuthClient> { UnavailableSocialAuthClient() }
    single<SettingsUserLocalStore>() bind UserLocalStore::class
    single<InMemorySyncOutbox>() bind SyncOutbox::class
    single<InMemoryTripLocalStore>() bind TripLocalStore::class
    single<InMemoryPurchaseLocalStore>() bind PurchaseLocalStore::class
    single<InMemoryWishlistLocalStore>() bind WishlistLocalStore::class
    single<InMemoryDiaryLocalStore>() bind DiaryLocalStore::class
    single<InMemoryTaxFreeLocalStore>() bind TaxFreeLocalStore::class
    single<InMemoryAlertsLocalStore>() bind AlertsLocalStore::class
    single<InMemoryRouteLocalStore>() bind RouteLocalStore::class
    single<InMemoryStatsLocalStore>() bind StatsLocalStore::class
    single<InMemoryExportLocalStore>() bind ExportLocalStore::class
    single<ClientRemoteConfigStore> { SettingsClientRemoteConfigStore(get()) }
    single<PendingDeepLinkStore>()
    single<LocalSessionStore> {
        CompositeLocalSessionStore(
            userLocalStore = get(),
            tripLocalStore = get(),
            purchaseLocalStore = get(),
            wishlistLocalStore = get(),
            diaryLocalStore = get(),
            taxFreeLocalStore = get(),
            alertsLocalStore = get(),
            routeLocalStore = get(),
            statsLocalStore = get(),
            exportLocalStore = get(),
            outbox = get(),
            analyticsQueue = getOrNull<AnalyticsEventQueue>(),
            pendingDeepLinks = get(),
            uploadCheckpoints = get(),
        )
    }
    single<InMemoryLocalCacheInventory>() bind LocalCacheInventory::class
    single<InMemorySyncConflictNotifier>() bind SyncConflictNotifier::class
    single<NoOpBackgroundSyncScheduler>() bind BackgroundSyncScheduler::class
    single<Observability> { ObservabilityFactory.create(get<AppConfig>().sentryDsn) }
    single<AppTracer> { createDefaultTracer() }
    single { NoOpAnalytics } bind Analytics::class
    single<SettingsAnalyticsConsentStore> { SettingsAnalyticsConsentStore(get()) } bind AnalyticsConsentStore::class
    single<AlwaysOnlineConnectivityMonitor>() bind ConnectivityMonitor::class
    single<PushTokenProvider> { createDefaultPushTokenProvider() }
    single<NotificationPermissionGate> { createNotificationPermissionGate() }
    single<ShareSheet> { createShareSheet() }
    single<BiometricAuthenticator> { createBiometricAuthenticator() }
    single<SettingsAppLockStore> { SettingsAppLockStore(get()) } bind AppLockStore::class
    single<AppBuildInfo> { createDefaultAppBuildInfo() }
    single<ContentChecksum> { createDefaultContentChecksum() }
    single<ReceiptImageCompressor> { FileKitReceiptImageCompressor() }
    single<InMemoryRegisteredPushDeviceStore>() bind RegisteredPushDeviceStore::class
    single<SettingsUploadCheckpointStore> { SettingsUploadCheckpointStore(get()) } bind UploadCheckpointStore::class
    single<AuthTokenCache> { KtorAuthTokenCache(get()) }

    single {
        val pinConfig = pinConfigForBuild(get())
        val enforcePinning = CertificatePinPolicy.shouldEnforce(
            isReleaseBuild = get<AppBuildInfo>().isReleaseBuild,
            config = pinConfig,
        )
        createVoyageHttpClient(
            baseUrl = get<AppConfig>().apiBaseUrl,
            engine = createPlatformHttpEngine(
                pinConfig = pinConfig,
                enforcePinning = enforcePinning,
            ),
            tokenProvider = { get<TokenStore>().accessToken() },
            refreshTokenProvider = { get<TokenStore>().refreshToken() },
            refreshTokens = {
                val result = get<AuthRepository>().refresh()
                result.getOrNull()?.let { session ->
                    io.ktor.client.plugins.auth.providers.BearerTokens(
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken,
                    )
                }
            },
            enableLogging = ClientReleasePolicy.enableHttpLogging(
                isReleaseBuild = get<AppBuildInfo>().isReleaseBuild,
            ),
            observability = get(),
        )
    }
    single(named("uploadHttpClient")) {
        val pinConfig = pinConfigForBuild(get())
        val enforcePinning = CertificatePinPolicy.shouldEnforce(
            isReleaseBuild = get<AppBuildInfo>().isReleaseBuild,
            config = pinConfig,
        )
        HttpClient(
            createPlatformHttpEngine(
                pinConfig = pinConfig,
                enforcePinning = enforcePinning,
            ),
        ) {
            expectSuccess = false
            installVoyageHttpTimeouts()
        }
    }
    single { AuthApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { HomeApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { PurchaseApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { TripApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { UserApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { WishlistApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { DiaryApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { TaxFreeApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { AlertsApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { RouteApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { StatsApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { ExportApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { PushApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { AnalyticsApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { HttpAnalyticsSink(get()) } bind AnalyticsSink::class
    single {
        MediaApi(
            client = get(),
            uploadClient = get(named("uploadHttpClient")),
            baseUrl = get<AppConfig>().apiBaseUrl,
        )
    }
    single<AuthRepositoryImpl>() bind AuthRepository::class
    single {
        TripRepositoryImpl(
            homeApi = get(),
            tripApi = get(),
            localStore = get(),
            outbox = get(),
            idGenerator = { "t-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
            clock = { epochMillis() },
            userLocalStore = get(),
        )
    } bind TripRepository::class
    single {
        PurchaseRepositoryImpl(
            api = get(),
            localStore = get(),
            outbox = get(),
            idGenerator = { "p-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
            clock = { epochMillis() },
        )
    } bind PurchaseRepository::class
    single<UserRepositoryImpl>() bind UserRepository::class
    single<ClientRemoteConfigRepositoryImpl>() bind ClientRemoteConfigRepository::class
    single {
        WishlistRepositoryImpl(
            api = get(),
            localStore = get(),
            outbox = get(),
            idGenerator = { "w-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
            clock = { epochMillis() },
        )
    } bind WishlistRepository::class
    single {
        DiaryRepositoryImpl(
            api = get(),
            localStore = get(),
            outbox = get(),
            idGenerator = { "d-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
            clock = { epochMillis() },
            today = {
                Instant.fromEpochMilliseconds(epochMillis()).toString().substringBefore('T')
            },
        )
    } bind DiaryRepository::class
    single<TaxFreeRepositoryImpl>() bind TaxFreeRepository::class
    single<AlertsRepositoryImpl>() bind AlertsRepository::class
    single<RouteRepositoryImpl>() bind RouteRepository::class
    single<StatsRepositoryImpl>() bind StatsRepository::class
    single<ExportRepositoryImpl>() bind ExportRepository::class
    single {
        MediaRepositoryImpl(
            api = get(),
            idempotencyKey = { "m-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
            checkpoints = get(),
        )
    } bind MediaRepository::class
    single<PushRepositoryImpl>() bind PushRepository::class
    single {
        SyncOutboxProcessor(
            outbox = get(),
            purchaseApi = get(),
            purchaseLocalStore = get(),
            tripApi = get(),
            tripLocalStore = get(),
            wishlistApi = get(),
            wishlistLocalStore = get(),
            diaryApi = get(),
            diaryLocalStore = get(),
            clock = { epochMillis() },
            conflictNotifier = get(),
        )
    }
    single<SyncRepositoryImpl>() bind SyncRepository::class
    single<SyncScheduler>()
}

val domainModule = module {
    factory<RegisterPushDeviceUseCase>()
    factory<UnregisterPushDeviceUseCase>()
    factory {
        LoginUseCase(
            authRepository = get(),
            registerPushDevice = get(),
        )
    }
    factory {
        RegisterUseCase(
            authRepository = get(),
            registerPushDevice = get(),
        )
    }
    factory {
        SocialLoginUseCase(
            socialAuth = get(),
            authRepository = get(),
            registerPushDevice = get(),
        )
    }
    factory<RequestPasswordResetUseCase>()
    factory<ResetPasswordUseCase>()
    factory<ObserveConnectivityUseCase>()
    factory<IsLoggedInUseCase>()
    factory {
        LogoutUseCase(
            authRepository = get(),
            localSessionStore = get(),
            unregisterPushDevice = get(),
            authTokenCache = get(),
        )
    }
    factory<DeleteAccountUseCase>()
    factory<ObserveHomeUseCase>()
    factory<DrainSyncOutboxUseCase>()
    factory<RefreshHomeUseCase>()
    factory<ResolveAddPurchaseDeepLinkUseCase>()
    factory<CreatePurchaseUseCase>()
    factory<UploadReceiptUseCase>()
    factory<FetchReceiptOcrUseCase>()
    factory<CreateTripUseCase>()
    factory<ObserveTripDetailUseCase>()
    factory<ObserveProfileUseCase>()
    factory<ObservePreferencesUseCase>()
    factory<RefreshProfileUseCase>()
    factory<RefreshPreferencesUseCase>()
    factory<UpdateProfileUseCase>()
    factory<UpdatePreferencesUseCase>()
    factory<ObserveWishlistUseCase>()
    factory<RefreshWishlistUseCase>()
    factory<CreateWishlistItemUseCase>()
    factory<DeleteWishlistItemUseCase>()
    factory<ConvertWishlistItemToPurchaseUseCase>()
    factory<ObserveDiaryUseCase>()
    factory<RefreshDiaryUseCase>()
    factory<CreateDiaryEntryUseCase>()
    factory<DeleteDiaryEntryUseCase>()
    factory<ObserveTaxFreeUseCase>()
    factory<RefreshTaxFreeUseCase>()
    factory<ObserveAlertsUseCase>()
    factory<RefreshAlertsUseCase>()
    factory<ObserveRouteUseCase>()
    factory<RefreshRouteUseCase>()
    factory<ObserveStatsUseCase>()
    factory<RefreshStatsUseCase>()
    factory<ObserveExportJobUseCase>()
    factory<CreateExportUseCase>()
    factory<RefreshExportJobUseCase>()
    factory<RefreshTripUseCase>()
    factory<AddTravelerUseCase>()
    factory<InviteTravelerUseCase>()
    factory<RefreshExchangeRateUseCase>()
    factory<ActivatePremiumUseCase>()
    factory<ObservePremiumUseCase>()
    factory<RefreshClientRemoteConfigUseCase>()
    factory<EvaluateForceUpdateUseCase>()
    factory<ObserveFeatureFlagUseCase>()
    factory {
        EvictLocalCacheUseCase(
            inventory = get(),
            clock = { epochMillis() },
        )
    }
    factory<RefreshPurchasesUseCase>()
    factory<ObserveSyncConflictsUseCase>()
    factory<AcknowledgeSyncConflictUseCase>()
}

val presentationModule = module {
    factory<AuthViewModel>()
    factory<ForgotPasswordViewModel>()
    factory<ResetPasswordViewModel>()
    factory<ForceUpdateViewModel>()
    single<AppLockViewModel>()
    factory<HomeViewModel>()
    factory<NewTripViewModel>()
    factory<ProfileViewModel>()
    factory<PrivacyViewModel>()
    factory<WishlistViewModel>()
    factory { params ->
        TripDetailViewModel(
            tripId = params.get(),
            observeTripDetail = get(),
            refreshTrip = get(),
            addTraveler = get(),
            inviteTraveler = get(),
            refreshExchangeRate = get(),
            refreshPurchases = get(),
            shareSheet = get(),
        )
    }
    factory { params ->
        AddPurchaseViewModel(
            tripId = params.get(),
            createPurchase = get(),
            uploadReceipt = get(),
            fetchReceiptOcr = get(),
            observeTripDetail = get(),
            observeFeatureFlag = get(),
        )
    }
    factory { params ->
        DiaryViewModel(
            tripId = params.get(),
            observeDiary = get(),
            refreshDiary = get(),
            createEntry = get(),
            deleteEntry = get(),
        )
    }
    factory { params ->
        TaxFreeViewModel(
            tripId = params.get(),
            observeTaxFree = get(),
            refreshTaxFree = get(),
        )
    }
    factory { params ->
        AlertsViewModel(
            tripId = params.get(),
            observeAlerts = get(),
            refreshAlerts = get(),
        )
    }
    factory { params ->
        RouteViewModel(
            tripId = params.get(),
            observeRoute = get(),
            refreshRoute = get(),
            observeFeatureFlag = get(),
        )
    }
    factory { params ->
        StatsViewModel(
            tripId = params.get(),
            observeStats = get(),
            refreshStats = get(),
        )
    }
    factory { params ->
        ExportViewModel(
            tripId = params.get(),
            observeExportJob = get(),
            createExport = get(),
            refreshExportJob = get(),
            observePremium = get(),
            observeFeatureFlag = get(),
        )
    }
}

val sharedModules = listOf(dataModule, domainModule, presentationModule)

private fun pinConfigForBuild(appBuild: AppBuildInfo): CertificatePinConfig {
    val config = VoyageCertificatePins.configured
    check(!CertificatePinPolicy.isMisconfiguredRelease(appBuild.isReleaseBuild, config)) {
        "Release builds must ship SPKI pins in VoyageCertificatePins"
    }
    return config
}

fun initKoin(
    config: AppConfig = AppConfig(),
    extraModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) = startKoin {
    allowOverride(true)
    appDeclaration()
    modules(
        module { single { config } },
        dataModule,
        domainModule,
        presentationModule,
    )
    // Platform extras (drivers/stores) stay dynamic — KOIN-W003 is expected here.
    if (extraModules.isNotEmpty()) {
        modules(extraModules)
    }
}.also {
    flushPendingDeepLinkUris()
}
