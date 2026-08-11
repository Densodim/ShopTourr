package com.example.shoptourr

import android.app.Application
import com.example.shoptourr.data.local.DatabaseDriverFactory
import com.example.shoptourr.data.local.createVoyageDatabase
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module

class VoyageApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            config = AppConfig(),
            extraModules = listOf(
                module {
                    single { DatabaseDriverFactory(androidContext()) }
                    single { createVoyageDatabase(get()) }
                }
            ),
        ) {
            androidLogger()
            androidContext(this@VoyageApp)
        }
    }
}
