package com.shoptourr.application;

import com.shoptourr.api.v1.dto.user.UserDtos.ClientRemoteConfigDto;
import com.shoptourr.api.v1.dto.user.UserDtos.FeatureFlagsDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UpdatePreferencesRequest;
import com.shoptourr.api.v1.dto.user.UserDtos.UpdateProfileRequest;
import com.shoptourr.api.v1.dto.user.UserDtos.UserDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UserPreferencesDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UserStatsDto;
import com.shoptourr.api.config.ClientConfigProperties;
import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.TripRepository;
import com.shoptourr.infra.persistence.UserEntity;
import com.shoptourr.infra.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository users;
    private final TripRepository trips;
    private final ClientConfigProperties clientConfig;

    public UserService(UserRepository users, TripRepository trips, ClientConfigProperties clientConfig) {
        this.users = users;
        this.trips = trips;
        this.clientConfig = clientConfig;
    }

    @Transactional(readOnly = true)
    public UserDto me(UUID userId) {
        return toDto(require(userId));
    }

    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileRequest request) {
        UserEntity user = require(userId);
        user.setDisplayName(request.displayName());
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public UserPreferencesDto preferences(UUID userId) {
        UserEntity user = require(userId);
        return new UserPreferencesDto(
                user.getLocale(),
                user.getPreferredCurrency(),
                user.getTheme(),
                user.isPushNotificationsEnabled(),
                user.isDarkMode()
        );
    }

    @Transactional
    public UserPreferencesDto updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        UserEntity user = require(userId);
        if (request.locale() != null) {
            user.setLocale(request.locale());
        }
        if (request.preferredCurrency() != null) {
            user.setPreferredCurrency(request.preferredCurrency());
        }
        if (request.theme() != null) {
            user.setTheme(request.theme());
            user.setDarkMode(request.theme() == com.shoptourr.api.v1.dto.user.UserDtos.ThemePreference.DARK);
        }
        if (request.pushNotificationsEnabled() != null) {
            user.setPushNotificationsEnabled(request.pushNotificationsEnabled());
        }
        if (request.darkMode() != null) {
            user.setDarkMode(request.darkMode());
        }
        return preferences(userId);
    }

    public ClientRemoteConfigDto appConfig() {
        ClientConfigProperties.Flags flags = clientConfig.flags();
        return new ClientRemoteConfigDto(
                clientConfig.minAndroidBuild(),
                clientConfig.minIosBuild(),
                clientConfig.softMinAndroidBuild(),
                clientConfig.softMinIosBuild(),
                new FeatureFlagsDto(flags.exportPdf(), flags.ocrAssist(), flags.nativeMaps()),
                clientConfig.storeUrlAndroid(),
                clientConfig.storeUrlIos()
        );
    }

    UserEntity require(UUID userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("user not found"));
    }

    UserDto toDto(UserEntity user) {
        int tripsCount = trips.findByUserIdAndDeletedAtIsNullOrderByStartDateDesc(user.getId()).size();
        return new UserDto(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getLocale(),
                user.getPreferredCurrency(),
                user.getTheme(),
                user.isPushNotificationsEnabled(),
                user.getCreatedAt(),
                user.getPremiumPlan(),
                new UserStatsDto(tripsCount, 0, 0)
        );
    }
}
