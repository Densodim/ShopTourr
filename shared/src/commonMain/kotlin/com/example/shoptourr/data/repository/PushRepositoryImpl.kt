package com.example.shoptourr.data.repository

import com.example.shoptourr.data.remote.PushApi
import com.example.shoptourr.data.remote.dto.push.PushPlatformDto
import com.example.shoptourr.data.remote.dto.push.RegisterDeviceRequest
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.PushDevice
import com.example.shoptourr.domain.model.PushPlatform
import com.example.shoptourr.domain.model.RegisterDeviceDraft
import com.example.shoptourr.domain.repository.PushRepository

class PushRepositoryImpl(
    private val api: PushApi,
) : PushRepository {
    override suspend fun registerDevice(draft: RegisterDeviceDraft): Result<PushDevice> =
        runCatching {
            api.registerDevice(
                RegisterDeviceRequest(
                    token = draft.token,
                    platform = when (draft.platform) {
                        PushPlatform.ANDROID -> PushPlatformDto.ANDROID
                        PushPlatform.IOS -> PushPlatformDto.IOS
                    },
                    appVersion = draft.appVersion,
                    deviceName = draft.deviceName,
                ),
            ).let { dto ->
                PushDevice(
                    id = dto.id,
                    tokenFingerprint = dto.tokenFingerprint,
                    platform = when (dto.platform) {
                        PushPlatformDto.ANDROID -> PushPlatform.ANDROID
                        PushPlatformDto.IOS -> PushPlatform.IOS
                    },
                    appVersion = dto.appVersion,
                    deviceName = dto.deviceName,
                    createdAt = dto.createdAt,
                    lastSeenAt = dto.lastSeenAt,
                )
            }
        }.mapHttpAppError()

    override suspend fun unregisterDevice(deviceId: String): Result<Unit> =
        runCatching { api.unregisterDevice(deviceId) }.mapHttpAppError()
}
