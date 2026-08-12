package com.shoptourr.api.v1.dto.media

import java.time.Instant
import java.util.UUID

data class MediaUploadIntentResponse(
    val mediaId: UUID,
    val uploadUrl: String,
    /** Extra headers client must send on PUT (e.g. Content-Type). */
    val requiredHeaders: Map<String, String>?,
    val uploadExpiresAt: Instant,
    val status: MediaStatus,
)
