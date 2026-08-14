package com.shoptourr.application;

import com.shoptourr.api.v1.dto.auth.AuthDtos.AuthTokensResponse;
import com.shoptourr.api.v1.dto.auth.AuthDtos.AuthUserDto;
import com.shoptourr.api.v1.dto.auth.AuthDtos.LoginRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.LogoutRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.RefreshTokenRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.RegisterRequest;
import com.shoptourr.api.v1.dto.user.UserDtos.PremiumPlan;
import com.shoptourr.api.v1.dto.user.UserDtos.ThemePreference;
import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.RefreshSessionEntity;
import com.shoptourr.infra.persistence.RefreshSessionRepository;
import com.shoptourr.infra.persistence.UserEntity;
import com.shoptourr.infra.persistence.UserRepository;
import com.shoptourr.infra.security.JwtProperties;
import com.shoptourr.infra.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository users;
    private final RefreshSessionRepository sessions;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthService(
            UserRepository users,
            RefreshSessionRepository sessions,
            PasswordEncoder passwords,
            TokenService tokens,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.users = users;
        this.sessions = sessions;
        this.passwords = passwords;
        this.tokens = tokens;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional
    public AuthTokensResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("email already registered");
        }
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwords.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setLocale(request.locale() == null || request.locale().isBlank() ? "ru" : request.locale());
        user.setPreferredCurrency("RUB");
        user.setTheme(ThemePreference.SYSTEM);
        user.setPushNotificationsEnabled(true);
        user.setDarkMode(false);
        user.setPremiumPlan(PremiumPlan.FREE);
        users.save(user);
        return issue(user, null);
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserEntity user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.unauthorized("invalid credentials"));
        if (!passwords.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("invalid credentials");
        }
        return issue(user, request.deviceName());
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshTokenRequest request) {
        RefreshSessionEntity session = sessions.findByTokenHash(tokens.hashRefreshToken(request.refreshToken()))
                .orElseThrow(() -> ApiException.unauthorized("invalid refresh token"));
        if (session.getExpiresAt().isBefore(clock.instant())) {
            sessions.delete(session);
            throw ApiException.unauthorized("refresh token expired");
        }
        UserEntity user = users.findById(session.getUserId())
                .orElseThrow(() -> ApiException.unauthorized("invalid refresh token"));
        sessions.delete(session);
        return issue(user, session.getDeviceName());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        if (request.allSessions() && request.refreshToken() != null) {
            sessions.findByTokenHash(tokens.hashRefreshToken(request.refreshToken()))
                    .ifPresent(session -> sessions.deleteByUserId(session.getUserId()));
            return;
        }
        if (request.refreshToken() != null && !request.refreshToken().isBlank()) {
            sessions.findByTokenHash(tokens.hashRefreshToken(request.refreshToken()))
                    .ifPresent(sessions::delete);
        }
    }

    public void forgotPassword(String email) {
        // Always 204 — do not leak whether the account exists.
    }

    private AuthTokensResponse issue(UserEntity user, String deviceName) {
        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setUserId(user.getId());
        String refresh = tokens.newRefreshToken();
        session.setTokenHash(tokens.hashRefreshToken(refresh));
        session.setDeviceName(deviceName);
        session.setExpiresAt(clock.instant().plus(jwtProperties.refreshTtl()));
        sessions.save(session);
        TokenService.IssuedTokens issued = tokens.issue(user.getId(), user.getEmail(), session.getId());
        return new AuthTokensResponse(
                issued.accessToken(),
                issued.accessExpiresIn(),
                refresh,
                issued.refreshExpiresIn(),
                "Bearer",
                new AuthUserDto(
                        user.getId(),
                        user.getDisplayName(),
                        user.getEmail(),
                        user.getLocale(),
                        user.getCreatedAt()
                )
        );
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
