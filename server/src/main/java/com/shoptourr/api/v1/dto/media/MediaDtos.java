package com.shoptourr.api.v1.dto.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Receipt / avatar upload via pre-signed URL.
 * Flow: create intent → PUT bytes to uploadUrl → confirm (optional) → use mediaId on purchase.
 */
public final class MediaDtos {

    private MediaDtos() {}

    public enum MediaPurpose {
        RECEIPT,
        AVATAR,
        DIARY,
        EXPORT
    }

    public enum MediaStatus {
        PENDING_UPLOAD,
        UPLOADED,
        PROCESSING,
        READY,
        FAILED
    }

    public record CreateMediaUploadIntentRequest(
            @NotNull MediaPurpose purpose,
            @NotBlank @Size(max = 128) String contentType,
            @Positive long byteSize,
            /** Optional client checksum (sha256 hex). */
            @Size(min = 64, max = 64) String sha256Hex
    ) {}

    public record MediaUploadIntentResponse(
            UUID mediaId,
            String uploadUrl,
            /** Extra headers client must send on PUT (e.g. Content-Type). */
            Map<String, String> requiredHeaders,
            Instant uploadExpiresAt,
            MediaStatus status
    ) {}

    public record ConfirmMediaUploadRequest(
            /** Optional; server can also detect via storage event. */
            boolean uploaded
    ) {}

    public record MediaAssetDto(
            UUID id,
            MediaPurpose purpose,
            MediaStatus status,
            String contentType,
            long byteSize,
            String downloadUrl,
            String thumbnailUrl,
            Instant createdAt
    ) {}

    /** OCR assist (P2) — result attached when PROCESSING→READY. */
    public record ReceiptOcrResultDto(
            UUID mediaId,
            String suggestedName,
            String suggestedAmount,
            String suggestedPlace,
            String suggestedCategory,
            double confidence
    ) {}
}
