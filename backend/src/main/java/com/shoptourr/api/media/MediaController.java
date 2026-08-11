package com.shoptourr.api.media;

import com.shoptourr.api.v1.dto.media.MediaDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final InMemoryMediaService mediaService;

    public MediaController(InMemoryMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload-intents")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaDtos.MediaUploadIntentResponse createIntent(
            @Valid @RequestBody MediaDtos.CreateMediaUploadIntentRequest request
    ) {
        return mediaService.createIntent(request);
    }

    @PostMapping("/{mediaId}/confirm")
    public MediaDtos.MediaAssetDto confirm(
            @PathVariable UUID mediaId,
            @RequestBody(required = false) MediaDtos.ConfirmMediaUploadRequest request
    ) {
        return mediaService.confirm(
                mediaId,
                request == null ? new MediaDtos.ConfirmMediaUploadRequest(true) : request
        );
    }

    @GetMapping("/{mediaId}")
    public MediaDtos.MediaAssetDto get(@PathVariable UUID mediaId) {
        return mediaService.get(mediaId);
    }

    @GetMapping("/{mediaId}/ocr")
    public MediaDtos.ReceiptOcrResultDto ocr(@PathVariable UUID mediaId) {
        return mediaService.ocr(mediaId);
    }
}
