package com.example.shoptourr.domain.repository

/**
 * Tears down on-device user data (SQLDelight SoT, outbox, profile).
 * Remote config / force-update thresholds are app-level and stay.
 */
interface LocalSessionStore {
    suspend fun clearUserData()
}
