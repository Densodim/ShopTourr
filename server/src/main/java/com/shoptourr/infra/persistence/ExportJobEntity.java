package com.shoptourr.infra.persistence;

import com.shoptourr.api.v1.dto.export.ExportDtos.ExportFormat;
import com.shoptourr.api.v1.dto.export.ExportDtos.ExportJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "export_jobs")
public class ExportJobEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ExportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExportJobStatus status;

    @Column(name = "include_tax_free", nullable = false)
    private boolean includeTaxFree;

    @Column(name = "include_diary", nullable = false)
    private boolean includeDiary;

    @Column(name = "content_type")
    private String contentType;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }
    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }
    public ExportJobStatus getStatus() { return status; }
    public void setStatus(ExportJobStatus status) { this.status = status; }
    public boolean isIncludeTaxFree() { return includeTaxFree; }
    public void setIncludeTaxFree(boolean includeTaxFree) { this.includeTaxFree = includeTaxFree; }
    public boolean isIncludeDiary() { return includeDiary; }
    public void setIncludeDiary(boolean includeDiary) { this.includeDiary = includeDiary; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
