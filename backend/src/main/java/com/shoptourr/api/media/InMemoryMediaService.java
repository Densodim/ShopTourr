package com.shoptourr.api.media;

import com.shoptourr.api.v1.dto.media.MediaDtos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryMediaService {
    private final Map<UUID, MediaDtos.MediaAssetDto> assets = new ConcurrentHashMap<>();

    public MediaDtos.MediaUploadIntentResponse createIntent(MediaDtos.CreateMediaUploadIntentRequest request) {
        UUID mediaId = UUID.randomUUID();
        Instant expires = Instant.now().plusSeconds(900);
        assets.put(
                mediaId,
                new MediaDtos.MediaAssetDto(
                        mediaId,
                        request.purpose(),
                        MediaDtos.MediaStatus.PENDING_UPLOAD,
                        request.contentType(),
                        request.byteSize(),
                        null,
                        null,
                        Instant.now()
                )
        );
        return new MediaDtos.MediaUploadIntentResponse(
                mediaId,
                "https://upload.local/dev/" + mediaId,
                Map.of("Content-Type", request.contentType()),
                expires,
                MediaDtos.MediaStatus.PENDING_UPLOAD
        );
    }

    public MediaDtos.MediaAssetDto confirm(UUID mediaId, MediaDtos.ConfirmMediaUploadRequest request) {
        MediaDtos.MediaAssetDto current = require(mediaId);
        MediaDtos.MediaAssetDto ready = new MediaDtos.MediaAssetDto(
                current.id(),
                current.purpose(),
                MediaDtos.MediaStatus.READY,
                current.contentType(),
                current.byteSize(),
                "https://cdn.local/dev/" + mediaId,
                "https://cdn.local/dev/" + mediaId + "/thumb",
                Instant.now()
        );
        assets.put(mediaId, ready);
        return ready;
    }

    public MediaDtos.MediaAssetDto get(UUID mediaId) {
        return require(mediaId);
    }

    public MediaDtos.ReceiptOcrResultDto ocr(UUID mediaId) {
        MediaDtos.MediaAssetDto asset = require(mediaId);
        if (asset.status() != MediaDtos.MediaStatus.READY
                && asset.status() != MediaDtos.MediaStatus.PROCESSING
                && asset.status() != MediaDtos.MediaStatus.UPLOADED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "MEDIA_NOT_READY");
        }
        return new MediaDtos.ReceiptOcrResultDto(
                mediaId,
                "Pasteis de Belem",
                "4.50",
                "Belem",
                "FOOD",
                0.86
        );
    }

    private MediaDtos.MediaAssetDto require(UUID mediaId) {
        MediaDtos.MediaAssetDto asset = assets.get(mediaId);
        if (asset == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "media not found");
        }
        return asset;
    }
}
