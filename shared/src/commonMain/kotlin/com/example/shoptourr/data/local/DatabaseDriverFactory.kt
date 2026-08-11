package com.example.shoptourr.data.local

import app.cash.sqldelight.db.SqlDriver
import com.example.shoptourr.db.VoyageDatabase

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createVoyageDatabase(driverFactory: DatabaseDriverFactory): VoyageDatabase =
    VoyageDatabase(driverFactory.createDriver())
