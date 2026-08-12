package com.shoptourr.api.v1.dto.media

import java.time.Instant
import java.util.UUID

data class MediaAssetDto(
    val id: UUID,
    val purpose: MediaPurpose,
    val status: MediaStatus,
    val contentType: String,
    val byteSize: Long,
    val downloadUrl: String?,
    val thumbnailUrl: String?,
    val createdAt: Instant,
)
