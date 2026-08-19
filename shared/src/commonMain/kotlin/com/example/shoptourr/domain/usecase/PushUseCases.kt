package com.example.shoptourr.domain.usecase

import com.example.shoptourr.data.push.RegisteredPushDeviceStore
import com.example.shoptourr.domain.model.PushDevice
import com.example.shoptourr.domain.model.RegisterDeviceDraft
import com.example.shoptourr.domain.push.PushTokenProvider
import com.example.shoptourr.domain.repository.PushRepository

class RegisterPushDeviceUseCase(
    private val pushRepository: PushRepository,
    private val tokenProvider: PushTokenProvider,
    private val registeredDeviceStore: RegisteredPushDeviceStore? = null,
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
        ).map { device ->
            registeredDeviceStore?.save(device.id)
            device
        }
    }
}

class UnregisterPushDeviceUseCase(
    private val pushRepository: PushRepository,
    private val registeredDeviceStore: RegisteredPushDeviceStore? = null,
) {
    suspend operator fun invoke(deviceId: String? = registeredDeviceStore?.deviceId()): Result<Unit> {
        val id = deviceId?.trim().orEmpty()
        if (id.isEmpty()) return Result.success(Unit)
        return pushRepository.unregisterDevice(id).onSuccess {
            registeredDeviceStore?.clear()
        }
    }
}
