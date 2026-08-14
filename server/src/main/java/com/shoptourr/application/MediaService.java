package com.shoptourr.application;

import com.shoptourr.api.v1.dto.media.MediaDtos.ConfirmMediaUploadRequest;
import com.shoptourr.api.v1.dto.media.MediaDtos.CreateMediaUploadIntentRequest;
import com.shoptourr.api.v1.dto.media.MediaDtos.MediaAssetDto;
import com.shoptourr.api.v1.dto.media.MediaDtos.MediaStatus;
import com.shoptourr.api.v1.dto.media.MediaDtos.MediaUploadIntentResponse;
import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.MediaAssetEntity;
import com.shoptourr.infra.persistence.MediaAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaService {

    static final long MAX_BYTES = 12L * 1024 * 1024;

    private final MediaAssetRepository assets;
    private final Clock clock;

    public MediaService(MediaAssetRepository assets, Clock clock) {
        this.assets = assets;
        this.clock = clock;
    }

    @Transactional
    public MediaUploadIntentResponse createIntent(UUID userId, CreateMediaUploadIntentRequest request) {
        if (request.byteSize() > MAX_BYTES) {
            throw ApiException.validation("file exceeds 12MB limit");
        }
        Instant now = Instant.now(clock);
        MediaAssetEntity entity = new MediaAssetEntity();
        entity.setUserId(userId);
        entity.setPurpose(request.purpose());
        entity.setStatus(MediaStatus.PENDING_UPLOAD);
        entity.setContentType(request.contentType());
        entity.setByteSize(request.byteSize());
        entity.setSha256Hex(request.sha256Hex());
        entity.setUploadToken(UUID.randomUUID().toString().replace("-", ""));
        entity.setUploadExpiresAt(now.plus(Duration.ofHours(1)));
        assets.save(entity);
        return new MediaUploadIntentResponse(
                entity.getId(),
                uploadUrl(entity),
                Map.of("Content-Type", entity.getContentType()),
                entity.getUploadExpiresAt(),
                entity.getStatus()
        );
    }

    @Transactional
    public void storeContent(UUID mediaId, String uploadToken, byte[] body) {
        MediaAssetEntity entity = assets.findByIdAndUploadToken(mediaId, uploadToken)
                .orElseThrow(() -> ApiException.notFound("upload intent not found"));
        if (Instant.now(clock).isAfter(entity.getUploadExpiresAt())) {
            throw ApiException.validation("upload URL expired");
        }
        if (body == null || body.length == 0) {
            throw ApiException.validation("empty upload");
        }
        if (body.length > MAX_BYTES) {
            throw ApiException.validation("file exceeds 12MB limit");
        }
        entity.setContent(body);
        entity.setByteSize(body.length);
        entity.setStatus(MediaStatus.UPLOADED);
    }

    @Transactional
    public MediaAssetDto confirm(UUID userId, UUID mediaId, ConfirmMediaUploadRequest request) {
        MediaAssetEntity entity = requireOwned(userId, mediaId);
        if (!request.uploaded()) {
            entity.setStatus(MediaStatus.FAILED);
            return toDto(entity);
        }
        if (entity.getContent() == null || entity.getContent().length == 0) {
            throw ApiException.mediaNotReady("upload bytes before confirm");
        }
        entity.setStatus(MediaStatus.READY);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public MediaAssetDto get(UUID userId, UUID mediaId) {
        return toDto(requireOwned(userId, mediaId));
    }

    private MediaAssetEntity requireOwned(UUID userId, UUID mediaId) {
        return assets.findByIdAndUserId(mediaId, userId)
                .orElseThrow(() -> ApiException.notFound("media not found"));
    }

    private MediaAssetDto toDto(MediaAssetEntity entity) {
        String download = entity.getStatus() == MediaStatus.READY ? downloadUrl(entity.getId()) : null;
        return new MediaAssetDto(
                entity.getId(),
                entity.getPurpose(),
                entity.getStatus(),
                entity.getContentType(),
                entity.getByteSize(),
                download,
                null,
                entity.getCreatedAt()
        );
    }

    private static String uploadUrl(MediaAssetEntity entity) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/media/{id}/content")
                .queryParam("uploadToken", entity.getUploadToken())
                .buildAndExpand(entity.getId())
                .toUriString();
    }

    private static String downloadUrl(UUID mediaId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/media/{id}")
                .buildAndExpand(mediaId)
                .toUriString();
    }
}
