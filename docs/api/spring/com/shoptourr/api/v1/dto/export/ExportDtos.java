package com.shoptourr.api.v1.dto.export;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** Export PDF / CSV — always async job. */
public final class ExportDtos {

    private ExportDtos() {}

    public enum ExportFormat {
        PDF, CSV
    }

    public enum ExportJobStatus {
        QUEUED, RUNNING, READY, FAILED, EXPIRED
    }

    public record CreateExportRequest(
            @NotNull ExportFormat format,
            /** Include Tax Free worksheet section (PDF). */
            boolean includeTaxFree,
            boolean includeDiary
    ) {}

    public record ExportJobDto(
            UUID id,
            UUID tripId,
            ExportFormat format,
            ExportJobStatus status,
            String downloadUrl,
            Instant expiresAt,
            String errorCode,
            Instant createdAt,
            Instant finishedAt
    ) {}
}
