package com.example.shoptourr.data.remote.dto.media

enum class MediaPurpose { RECEIPT, AVATAR, DIARY, EXPORT }

enum class MediaStatus { PENDING_UPLOAD, UPLOADED, PROCESSING, READY, FAILED }

data class CreateMediaUploadIntentRequest(
    val purpose: MediaPurpose,
    val contentType: String,
    val byteSize: Long,
    val sha256Hex: String? = null,
)

data class MediaUploadIntentResponse(
    val mediaId: String,
    val uploadUrl: String,
    val requiredHeaders: Map<String, String> = emptyMap(),
    val uploadExpiresAt: String,
    val status: MediaStatus,
)

data class ConfirmMediaUploadRequest(
    val uploaded: Boolean = true,
)

data class MediaAssetDto(
    val id: String,
    val purpose: MediaPurpose,
    val status: MediaStatus,
    val contentType: String,
    val byteSize: Long,
    val downloadUrl: String? = null,
    val thumbnailUrl: String? = null,
    val createdAt: String,
)

data class ReceiptOcrResultDto(
    val mediaId: String,
    val suggestedName: String? = null,
    val suggestedAmount: String? = null,
    val suggestedPlace: String? = null,
    val suggestedCategory: String? = null,
    val confidence: Double = 0.0,
)
