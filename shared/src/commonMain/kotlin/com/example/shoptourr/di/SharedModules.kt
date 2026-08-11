package com.example.shoptourr.di

import com.example.shoptourr.data.local.InMemoryPurchaseLocalStore
import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.remote.AuthApi
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.createPlatformHttpEngine
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.repository.AuthRepositoryImpl
import com.example.shoptourr.data.repository.PurchaseRepositoryImpl
import com.example.shoptourr.data.repository.TripRepositoryImpl
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.repository.PurchaseRepository
import com.example.shoptourr.domain.repository.TripRepository
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.domain.usecase.ObserveHomeUseCase
import com.example.shoptourr.domain.usecase.ObserveTripDetailUseCase
import com.example.shoptourr.domain.usecase.RefreshHomeUseCase
import com.example.shoptourr.epochMillis
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.home.HomeViewModel
import com.example.shoptourr.presentation.purchase.AddPurchaseViewModel
import com.example.shoptourr.presentation.trip.NewTripViewModel
import com.example.shoptourr.presentation.trip.TripDetailViewModel
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
    single<SyncOutbox> { InMemorySyncOutbox() }
    singleOf(::InMemoryTripLocalStore) { bind<TripLocalStore>() }
    singleOf(::InMemoryPurchaseLocalStore) { bind<PurchaseLocalStore>() }

    single {
        createVoyageHttpClient(
            baseUrl = get<AppConfig>().apiBaseUrl,
            engine = createPlatformHttpEngine(),
            tokenProvider = { get<TokenStore>().accessToken() },
            enableLogging = true,
        )
    }
    single { AuthApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { HomeApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { PurchaseApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    single { TripApi(client = get(), baseUrl = get<AppConfig>().apiBaseUrl) }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
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
    single {
        SyncOutboxProcessor(
            outbox = get(),
            purchaseApi = get(),
            purchaseLocalStore = get(),
            tripApi = get(),
            tripLocalStore = get(),
            clock = { epochMillis() },
        )
    }
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::IsLoggedInUseCase)
    factoryOf(::ObserveHomeUseCase)
    factoryOf(::RefreshHomeUseCase)
    factoryOf(::CreatePurchaseUseCase)
    factoryOf(::CreateTripUseCase)
    factoryOf(::ObserveTripDetailUseCase)
}

val presentationModule = module {
    factoryOf(::AuthViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::NewTripViewModel)
    factory { params ->
        TripDetailViewModel(
            tripId = params.get(),
            observeTripDetail = get(),
        )
    }
    factory { params ->
        AddPurchaseViewModel(
            tripId = params.get(),
            createPurchase = get(),
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
