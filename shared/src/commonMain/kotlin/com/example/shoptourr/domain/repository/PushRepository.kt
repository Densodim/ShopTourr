package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.PushDevice
import com.example.shoptourr.domain.model.RegisterDeviceDraft

interface PushRepository {
    suspend fun registerDevice(draft: RegisterDeviceDraft): Result<PushDevice>
    suspend fun unregisterDevice(deviceId: String): Result<Unit>
}
