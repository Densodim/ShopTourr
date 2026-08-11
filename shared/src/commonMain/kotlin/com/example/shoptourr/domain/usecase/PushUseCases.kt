package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.PushDevice
import com.example.shoptourr.domain.model.RegisterDeviceDraft
import com.example.shoptourr.domain.push.PushTokenProvider
import com.example.shoptourr.domain.repository.PushRepository

class RegisterPushDeviceUseCase(
    private val pushRepository: PushRepository,
    private val tokenProvider: PushTokenProvider,
) {
    /**
     * Best-effort registration. Skips when token is not available yet.
     */
    suspend operator fun invoke(): Result<PushDevice?> {
        val token = tokenProvider.currentToken()?.trim().orEmpty()
        if (token.isEmpty()) return Result.success(null)
        return pushRepository.registerDevice(
            RegisterDeviceDraft(
                token = token,
                platform = tokenProvider.platform,
                appVersion = tokenProvider.appVersion,
                deviceName = tokenProvider.deviceName,
            ),
        )
    }
}

class UnregisterPushDeviceUseCase(
    private val pushRepository: PushRepository,
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        if (deviceId.isBlank()) return Result.failure(AppError.Validation("deviceId"))
        return pushRepository.unregisterDevice(deviceId)
    }
}
