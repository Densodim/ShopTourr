package com.example.shoptourr

import androidx.compose.ui.window.ComposeUIViewController
import com.example.shoptourr.data.local.DatabaseDriverFactory
import com.example.shoptourr.data.local.PurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightPurchaseLocalStore
import com.example.shoptourr.data.local.SqlDelightTripLocalStore
import com.example.shoptourr.data.local.TripLocalStore
import com.example.shoptourr.data.local.createVoyageDatabase
import com.example.shoptourr.data.sync.SqlDelightSyncOutbox
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.di.initKoin
import com.example.shoptourr.epochMillis
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import platform.UIKit.UIViewController

private val iosDatabaseModule = module {
    single { DatabaseDriverFactory() }
    single { createVoyageDatabase(get()) }
    single<TripLocalStore> { SqlDelightTripLocalStore(get()) }
    single<PurchaseLocalStore> { SqlDelightPurchaseLocalStore(get()) }
    single<SyncOutbox> { SqlDelightSyncOutbox(get()) }
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

fun MainViewController(): UIViewController {
    if (GlobalContext.getOrNull() == null) {
        initKoin(
            config = AppConfig(),
            extraModules = listOf(iosDatabaseModule),
        )
    }
    return ComposeUIViewController { App() }
}
