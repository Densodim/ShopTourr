package com.example.shoptourr.fake

import com.example.shoptourr.domain.model.PushDevice
import com.example.shoptourr.domain.model.RegisterDeviceDraft
import com.example.shoptourr.domain.repository.PushRepository

class FakePushRepository(
    private val registerError: Throwable? = null,
) : PushRepository {
    var registerCalls: Int = 0
        private set
    var lastDraft: RegisterDeviceDraft? = null
        private set

    override suspend fun registerDevice(draft: RegisterDeviceDraft): Result<PushDevice> {
        registerCalls += 1
        lastDraft = draft
        registerError?.let { return Result.failure(it) }
        return Result.success(
            PushDevice(
                id = "dev-1",
                tokenFingerprint = draft.token.take(8),
                platform = draft.platform,
                appVersion = draft.appVersion,
                deviceName = draft.deviceName,
                createdAt = "2026-08-11T00:00:00Z",
            ),
        )
    }

    override suspend fun unregisterDevice(deviceId: String): Result<Unit> = Result.success(Unit)
}
