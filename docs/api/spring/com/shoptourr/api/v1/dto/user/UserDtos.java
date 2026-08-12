package com.shoptourr.api.v1.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Profile + Settings screens.
 */
public final class UserDtos {

    private UserDtos() {}

    public enum ThemePreference {
        SYSTEM, LIGHT, DARK
    }

    /** Monetization tier (P3). FREE is default. */
    public enum PremiumPlan {
        FREE, PLUS, PRO
    }

    public record UserDto(
            UUID id,
            String displayName,
            String email,
            String avatarUrl,
            String locale,
            /** ISO-4217 preferred display / quote currency (mock: RUB). */
            String preferredCurrency,
            ThemePreference theme,
            boolean pushNotificationsEnabled,
            Instant memberSince,
            PremiumPlan premiumPlan,
            UserStatsDto stats
    ) {}

    public record ActivatePremiumRequest(
            @NotNull PremiumPlan plan
    ) {}

    public record UserStatsDto(
            int tripsCount,
            int countriesCount,
            int wishlistCount
    ) {}

    public record UpdateProfileRequest(
            @NotBlank @Size(min = 2, max = 80) String displayName,
            /** Existing media asset id after avatar upload, or null to clear. */
            UUID avatarMediaId
    ) {}

    public record UserPreferencesDto(
            @NotBlank @Size(min = 2, max = 5) String locale,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") String preferredCurrency,
            ThemePreference theme,
            boolean pushNotificationsEnabled,
            boolean darkMode
    ) {}

    public record UpdatePreferencesRequest(
            @Size(min = 2, max = 5) String locale,
            @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") String preferredCurrency,
            ThemePreference theme,
            Boolean pushNotificationsEnabled,
            Boolean darkMode
    ) {}

    public record FeatureFlagsDto(
            boolean exportPdf,
            boolean ocrAssist,
            boolean nativeMaps
    ) {}

    /** GET /me/app-config — force update thresholds + feature flags. */
    public record ClientRemoteConfigDto(
            int minAndroidBuild,
            int minIosBuild,
            Integer softMinAndroidBuild,
            Integer softMinIosBuild,
            FeatureFlagsDto flags,
            String storeUrlAndroid,
            String storeUrlIos
    ) {}
}
