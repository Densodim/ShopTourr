package com.example.shoptourr.domain.lock

enum class BiometricAvailability {
    AVAILABLE,
    NOT_ENROLLED,
    UNAVAILABLE,
}

interface BiometricAuthenticator {
    suspend fun availability(): BiometricAvailability
    suspend fun authenticate(reason: String): Boolean
}
