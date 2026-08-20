package com.example.shoptourr.domain.push

/**
 * OS notification permission. Push registration must not prompt on cold start;
 * call this from [com.example.shoptourr.domain.usecase.RegisterPushDeviceUseCase]
 * after a successful login.
 */
fun interface NotificationPermissionGate {
    suspend fun ensureGranted(): Boolean
}
