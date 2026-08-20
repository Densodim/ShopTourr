package com.example.shoptourr.domain.usecase

import com.example.shoptourr.data.push.RegisteredPushDeviceStore
import com.example.shoptourr.domain.model.PushDevice
import com.example.shoptourr.domain.model.RegisterDeviceDraft
import com.example.shoptourr.domain.push.NotificationPermissionGate
import com.example.shoptourr.domain.push.PushTokenProvider
import com.example.shoptourr.domain.repository.PushRepository

class RegisterPushDeviceUseCase(
    private val pushRepository: PushRepository,
    private val tokenProvider: PushTokenProvider,
    private val registeredDeviceStore: RegisteredPushDeviceStore? = null,
    private val notificationPermission: NotificationPermissionGate = NotificationPermissionGate { true },
) {
    /**
     * Best-effort registration. Asks for OS notification permission after login,
     * then skips when the user denies or the token is not available yet.
     */
    suspend operator fun invoke(): Result<PushDevice?> {
        if (!notificationPermission.ensureGranted()) return Result.success(null)
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
