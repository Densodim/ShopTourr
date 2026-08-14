package com.shoptourr.api.v1.dto.push;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Push device registration — budget alerts (P2).
 */
public final class PushDtos {

    private PushDtos() {}

    public enum PushPlatform {
        ANDROID,
        IOS
    }

    public record RegisterDeviceRequest(
            @NotBlank @Size(max = 512) String token,
            @NotNull PushPlatform platform,
            @Size(max = 64) String appVersion,
            @Size(max = 120) String deviceName
    ) {}

    public record DeviceDto(
            UUID id,
            String tokenFingerprint,
            PushPlatform platform,
            String appVersion,
            String deviceName,
            Instant createdAt,
            Instant lastSeenAt
    ) {}
}
