package com.example.shoptourr.domain.model

enum class MediaPurpose { RECEIPT, AVATAR, DIARY, EXPORT }

enum class MediaStatus { PENDING_UPLOAD, UPLOADED, PROCESSING, READY, FAILED }

data class MediaUploadIntent(
    val mediaId: String,
    val uploadUrl: String,
    val requiredHeaders: Map<String, String> = emptyMap(),
    val uploadExpiresAt: String,
    val status: MediaStatus,
)

data class MediaAsset(
    val id: String,
    val purpose: MediaPurpose,
    val status: MediaStatus,
    val contentType: String,
    val byteSize: Long,
    val downloadUrl: String? = null,
    val thumbnailUrl: String? = null,
    val createdAt: String,
)

data class ReceiptOcrResult(
    val mediaId: String,
    val suggestedName: String? = null,
    val suggestedAmount: String? = null,
    val suggestedPlace: String? = null,
    val suggestedCategory: PurchaseCategory? = null,
    val confidence: Double = 0.0,
)

data class ReceiptUploadDraft(
    val contentType: String,
    val bytes: ByteArray,
    val sha256Hex: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ReceiptUploadDraft
        return contentType == other.contentType &&
            bytes.contentEquals(other.bytes) &&
            sha256Hex == other.sha256Hex
    }

    override fun hashCode(): Int {
        var result = contentType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (sha256Hex?.hashCode() ?: 0)
        return result
    }
}
