package com.example.shoptourr.di

import com.example.shoptourr.data.remote.AuthApi
import com.example.shoptourr.data.remote.createPlatformHttpEngine
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.repository.AuthRepositoryImpl
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.russhwolf.settings.Settings
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

data class AppConfig(
    val apiBaseUrl: String = "https://api.shoptourr.com/api",
)

val dataModule = module {
    single<Settings> { Settings() }
    singleOf(::SettingsTokenStore) { bind<TokenStore>() }
    single<SyncOutbox> { InMemorySyncOutbox() }

    single {
        createVoyageHttpClient(
            baseUrl = get<AppConfig>().apiBaseUrl,
            engine = createPlatformHttpEngine(),
            tokenProvider = { get<TokenStore>().accessToken() },
            enableLogging = true,
        )
    }
    single {
        AuthApi(
            client = get(),
            baseUrl = get<AppConfig>().apiBaseUrl,
        )
    }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
}

val domainModule = module {
    factoryOf(::LoginUseCase)
}

val presentationModule = module {
    factoryOf(::AuthViewModel)
}

val sharedModules = listOf(dataModule, domainModule, presentationModule)

fun initKoin(
    config: AppConfig = AppConfig(),
    extraModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) = startKoin {
    appDeclaration()
    modules(
        module { single { config } },
        *sharedModules.toTypedArray(),
        *extraModules.toTypedArray(),
    )
}
