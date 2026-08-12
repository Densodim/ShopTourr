package com.example.shoptourr.di

import com.example.shoptourr.data.connectivity.AlwaysOnlineConnectivityMonitor
import com.example.shoptourr.data.hash.createDefaultContentChecksum
import com.example.shoptourr.data.media.FileKitReceiptImageCompressor
import com.example.shoptourr.data.local.InMemoryAlertsLocalStore
import com.example.shoptourr.data.local.InMemoryClientRemoteConfigStore
import com.example.shoptourr.data.local.ClientRemoteConfigStore
import com.example.shoptourr.data.local.InMemoryDiaryLocalStore
import com.example.shoptourr.data.platform.createDefaultAppBuildInfo
import com.example.shoptourr.data.repository.ClientRemoteConfigRepositoryImpl
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.example.shoptourr.domain.repository.ClientRemoteConfigRepository
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
import com.example.shoptourr.data.local.SettingsUserLocalStore
import com.example.shoptourr.data.local.StatsLocalStore
import com.example.shoptourr.data.local.TaxFreeLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.local.UserLocalStore
import com.example.shoptourr.data.local.WishlistLocalStore
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.data.push.createDefaultPushTokenProvider
import com.example.shoptourr.data.remote.AlertsApi
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
import com.example.shoptourr.data.settings.SettingsTokenStore
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
import com.example.shoptourr.domain.usecase.DeleteDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import com.example.shoptourr.domain.usecase.FetchReceiptOcrUseCase
import com.example.shoptourr.domain.usecase.InviteTravelerUseCase
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.domain.usecase.LogoutUseCase
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
import com.example.shoptourr.domain.usecase.RefreshPreferencesUseCase
import com.example.shoptourr.domain.usecase.RefreshProfileUseCase
import com.example.shoptourr.domain.usecase.RefreshRouteUseCase
import com.example.shoptourr.domain.usecase.RefreshStatsUseCase
import com.example.shoptourr.domain.usecase.RefreshTaxFreeUseCase
import com.example.shoptourr.domain.usecase.RefreshTripUseCase
import com.example.shoptourr.domain.usecase.RefreshWishlistUseCase
import com.example.shoptourr.domain.usecase.RegisterPushDeviceUseCase
import com.example.shoptourr.domain.usecase.RegisterUseCase
import com.example.shoptourr.domain.usecase.RequestPasswordResetUseCase
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
import com.example.shoptourr.presentation.diary.DiaryViewModel
import com.example.shoptourr.presentation.export.ExportViewModel
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.presentation.map.RouteViewModel
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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.random.Random

data class AppConfig(
    val apiBaseUrl: String = "https://api.shoptourr.com/api",
)

val dataModule = module {
    single<Settings> { Settings() }
    singleOf(::SettingsTokenStore) { bind<TokenStore>() }
    singleOf(::SettingsUserLocalStore) { bind<UserLocalStore>() }
    single<SyncOutbox> { InMemorySyncOutbox() }
    singleOf(::InMemoryTripLocalStore) { bind<TripLocalStore>() }
    singleOf(::InMemoryPurchaseLocalStore) { bind<PurchaseLocalStore>() }
    singleOf(::InMemoryWishlistLocalStore) { bind<WishlistLocalStore>() }
    singleOf(::InMemoryDiaryLocalStore) { bind<DiaryLocalStore>() }
    singleOf(::InMemoryTaxFreeLocalStore) { bind<TaxFreeLocalStore>() }
    singleOf(::InMemoryAlertsLocalStore) { bind<AlertsLocalStore>() }
    singleOf(::InMemoryRouteLocalStore) { bind<RouteLocalStore>() }
    singleOf(::InMemoryStatsLocalStore) { bind<StatsLocalStore>() }
    singleOf(::InMemoryExportLocalStore) { bind<ExportLocalStore>() }
    singleOf(::InMemoryClientRemoteConfigStore) { bind<ClientRemoteConfigStore>() }
    single { PendingDeepLinkStore() }
    single<ConnectivityMonitor> { AlwaysOnlineConnectivityMonitor() }
    single<PushTokenProvider> { createDefaultPushTokenProvider() }
    single<AppBuildInfo> { createDefaultAppBuildInfo() }
    single<ContentChecksum> { createDefaultContentChecksum() }
    single<ReceiptImageCompressor> { FileKitReceiptImageCompressor() }

    single {
        createVoyageHttpClient(
            baseUrl = get<AppConfig>().apiBaseUrl,
            engine = createPlatformHttpEngine(),
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
            enableLogging = true,
        )
    }
    single(named("uploadHttpClient")) {
        HttpClient(createPlatformHttpEngine()) {
            expectSuccess = false
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
    single {
        MediaApi(
            client = get(),
            uploadClient = get(named("uploadHttpClient")),
            baseUrl = get<AppConfig>().apiBaseUrl,
        )
    }
    single {
        AuthRepositoryImpl(
            api = get(),
            tokenStore = get(),
            userLocalStore = get(),
        )
    } bind AuthRepository::class
    single {
        TripRepositoryImpl(
            homeApi = get(),
            tripApi = get(),
            localStore = get(),
            outbox = get(),
            idGenerator = { "t-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
            clock = { epochMillis() },
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
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    single {
        ClientRemoteConfigRepositoryImpl(
            api = get(),
            localStore = get(),
        )
    } bind ClientRemoteConfigRepository::class
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
    singleOf(::TaxFreeRepositoryImpl) { bind<TaxFreeRepository>() }
    singleOf(::AlertsRepositoryImpl) { bind<AlertsRepository>() }
    singleOf(::RouteRepositoryImpl) { bind<RouteRepository>() }
    singleOf(::StatsRepositoryImpl) { bind<StatsRepository>() }
    singleOf(::ExportRepositoryImpl) { bind<ExportRepository>() }
    single {
        MediaRepositoryImpl(
            api = get(),
            idempotencyKey = { "m-${epochMillis()}-${Random.nextInt(100000, 999999)}" },
        )
    } bind MediaRepository::class
    singleOf(::PushRepositoryImpl) { bind<PushRepository>() }
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
        )
    }
    singleOf(::SyncRepositoryImpl) { bind<SyncRepository>() }
    singleOf(::SyncScheduler)
}

val domainModule = module {
    factoryOf(::RegisterPushDeviceUseCase)
    factoryOf(::UnregisterPushDeviceUseCase)
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
    factoryOf(::RequestPasswordResetUseCase)
    factoryOf(::ObserveConnectivityUseCase)
    factoryOf(::IsLoggedInUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ObserveHomeUseCase)
    factoryOf(::DrainSyncOutboxUseCase)
    factory {
        RefreshHomeUseCase(
            tripRepository = get(),
            drainSyncOutbox = get(),
        )
    }
    factory {
        CreatePurchaseUseCase(
            purchaseRepository = get(),
            drainSyncOutbox = get(),
        )
    }
    factory {
        UploadReceiptUseCase(
            mediaRepository = get(),
            checksum = get(),
            compressor = get(),
        )
    }
    factoryOf(::FetchReceiptOcrUseCase)
    factory {
        CreateTripUseCase(
            tripRepository = get(),
            drainSyncOutbox = get(),
        )
    }
    factoryOf(::ObserveTripDetailUseCase)
    factoryOf(::ObserveProfileUseCase)
    factoryOf(::ObservePreferencesUseCase)
    factoryOf(::RefreshProfileUseCase)
    factoryOf(::RefreshPreferencesUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::UpdatePreferencesUseCase)
    factoryOf(::ObserveWishlistUseCase)
    factoryOf(::RefreshWishlistUseCase)
    factory {
        CreateWishlistItemUseCase(
            wishlistRepository = get(),
            drainSyncOutbox = get(),
        )
    }
    factoryOf(::DeleteWishlistItemUseCase)
    factoryOf(::ObserveDiaryUseCase)
    factoryOf(::RefreshDiaryUseCase)
    factory {
        CreateDiaryEntryUseCase(
            diaryRepository = get(),
            drainSyncOutbox = get(),
        )
    }
    factoryOf(::DeleteDiaryEntryUseCase)
    factoryOf(::ObserveTaxFreeUseCase)
    factoryOf(::RefreshTaxFreeUseCase)
    factoryOf(::ObserveAlertsUseCase)
    factoryOf(::RefreshAlertsUseCase)
    factoryOf(::ObserveRouteUseCase)
    factoryOf(::RefreshRouteUseCase)
    factoryOf(::ObserveStatsUseCase)
    factoryOf(::RefreshStatsUseCase)
    factoryOf(::ObserveExportJobUseCase)
    factoryOf(::CreateExportUseCase)
    factoryOf(::RefreshExportJobUseCase)
    factoryOf(::RefreshTripUseCase)
    factoryOf(::AddTravelerUseCase)
    factoryOf(::InviteTravelerUseCase)
    factoryOf(::RefreshExchangeRateUseCase)
    factoryOf(::ActivatePremiumUseCase)
    factoryOf(::ObservePremiumUseCase)
    factoryOf(::RefreshClientRemoteConfigUseCase)
    factoryOf(::EvaluateForceUpdateUseCase)
    factoryOf(::ObserveFeatureFlagUseCase)
}

val presentationModule = module {
    factoryOf(::AuthViewModel)
    factoryOf(::ForgotPasswordViewModel)
    factoryOf(::ForceUpdateViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::NewTripViewModel)
    factoryOf(::ProfileViewModel)
    factoryOf(::WishlistViewModel)
    factory { params ->
        TripDetailViewModel(
            tripId = params.get(),
            observeTripDetail = get(),
            refreshTrip = get(),
            addTraveler = get(),
            inviteTraveler = get(),
            refreshExchangeRate = get(),
        )
    }
    factory { params ->
        AddPurchaseViewModel(
            tripId = params.get(),
            createPurchase = get(),
            uploadReceipt = get(),
            fetchReceiptOcr = get(),
            observeTripDetail = get(),
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
        )
    }
}

val sharedModules = listOf(dataModule, domainModule, presentationModule)

fun initKoin(
    config: AppConfig = AppConfig(),
    extraModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) = startKoin {
    allowOverride(true)
    appDeclaration()
    modules(
        module { single { config } },
        *sharedModules.toTypedArray(),
        *extraModules.toTypedArray(),
    )
}
