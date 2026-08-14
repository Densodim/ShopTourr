package com.shoptourr.infra.persistence;

import com.shoptourr.api.v1.dto.media.MediaDtos.MediaPurpose;
import com.shoptourr.api.v1.dto.media.MediaDtos.MediaStatus;
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
@Table(name = "media_assets")
public class MediaAssetEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MediaPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private MediaStatus status;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "byte_size", nullable = false)
    private long byteSize;

    @Column(name = "sha256_hex", length = 64)
    private String sha256Hex;

    @Column(name = "upload_token", nullable = false, length = 64)
    private String uploadToken;

    @Column(name = "upload_expires_at", nullable = false)
    private Instant uploadExpiresAt;

    @Lob
    @Column(name = "content")
    private byte[] content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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
    public MediaPurpose getPurpose() { return purpose; }
    public void setPurpose(MediaPurpose purpose) { this.purpose = purpose; }
    public MediaStatus getStatus() { return status; }
    public void setStatus(MediaStatus status) { this.status = status; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getByteSize() { return byteSize; }
    public void setByteSize(long byteSize) { this.byteSize = byteSize; }
    public String getSha256Hex() { return sha256Hex; }
    public void setSha256Hex(String sha256Hex) { this.sha256Hex = sha256Hex; }
    public String getUploadToken() { return uploadToken; }
    public void setUploadToken(String uploadToken) { this.uploadToken = uploadToken; }
    public Instant getUploadExpiresAt() { return uploadExpiresAt; }
    public void setUploadExpiresAt(Instant uploadExpiresAt) { this.uploadExpiresAt = uploadExpiresAt; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
}
