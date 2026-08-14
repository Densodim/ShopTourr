package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.media.MediaDtos.ConfirmMediaUploadRequest;
import com.shoptourr.api.v1.dto.media.MediaDtos.CreateMediaUploadIntentRequest;
import com.shoptourr.api.v1.dto.media.MediaDtos.MediaAssetDto;
import com.shoptourr.api.v1.dto.media.MediaDtos.MediaUploadIntentResponse;
import com.shoptourr.api.v1.dto.media.MediaDtos.ReceiptOcrResultDto;
import com.shoptourr.application.IdempotencyService;
import com.shoptourr.application.MediaService;
import com.shoptourr.domain.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/media", version = "1")
public class MediaController {

    private final MediaService media;
    private final IdempotencyService idempotency;

    public MediaController(MediaService media, IdempotencyService idempotency) {
        this.media = media;
        this.idempotency = idempotency;
    }

    @PostMapping("/upload-intents")
    ResponseEntity<MediaUploadIntentResponse> createIntent(
            @Valid @RequestBody CreateMediaUploadIntentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication
    ) {
        UUID userId = CurrentUser.id(authentication);
        return idempotency.run(
                userId,
                idempotencyKey,
                "POST /api/media/upload-intents",
                request,
                HttpStatus.CREATED.value(),
                MediaUploadIntentResponse.class,
                () -> media.createIntent(userId, request)
        );
    }

    @PutMapping("/{mediaId}/content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void upload(
            @PathVariable UUID mediaId,
            @RequestParam String uploadToken,
            @RequestBody byte[] body
    ) {
        media.storeContent(mediaId, uploadToken, body);
    }

    @PostMapping("/{mediaId}/confirm")
    MediaAssetDto confirm(
            @PathVariable UUID mediaId,
            @RequestBody(required = false) ConfirmMediaUploadRequest request,
            Authentication authentication
    ) {
        ConfirmMediaUploadRequest body = request == null ? new ConfirmMediaUploadRequest(true) : request;
        return media.confirm(CurrentUser.id(authentication), mediaId, body);
    }

    @GetMapping("/{mediaId}")
    MediaAssetDto get(@PathVariable UUID mediaId, Authentication authentication) {
        return media.get(CurrentUser.id(authentication), mediaId);
    }

    @GetMapping("/{mediaId}/ocr")
    ReceiptOcrResultDto ocr(@PathVariable UUID mediaId) {
        throw ApiException.mediaNotReady("OCR is not available yet");
    }
}
