package com.shoptourr.api.v1.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Auth wire contract — maps Welcome / SignUp / SignIn screens.
 */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 80) String displayName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 6, max = 128) String password,
            /** Optional; default {@code ru}. */
            @Size(min = 2, max = 5) String locale
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 1, max = 128) String password,
            /** Optional device label for session list ("iPhone 15"). */
            @Size(max = 120) String deviceName
    ) {}

    public record RefreshTokenRequest(
            @NotBlank String refreshToken
    ) {}

    public record LogoutRequest(
            /** If null — revoke current session only; if present — that refresh token. */
            String refreshToken,
            /** If true — revoke all sessions for user. */
            boolean allSessions
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    public record AuthTokensResponse(
            String accessToken,
            /** Seconds until access expiry. */
            long accessExpiresIn,
            String refreshToken,
            long refreshExpiresIn,
            String tokenType,
            AuthUserDto user
    ) {
        public AuthTokensResponse {
            if (tokenType == null || tokenType.isBlank()) tokenType = "Bearer";
        }
    }

    public record AuthUserDto(
            UUID id,
            String displayName,
            String email,
            String locale,
            Instant createdAt
    ) {}
}
