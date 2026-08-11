package com.shoptourr.api.auth;

import com.shoptourr.api.v1.dto.auth.AuthDtos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryAuthService {
    private final Map<String, AuthDtos.AuthUserDto> usersByEmail = new ConcurrentHashMap<>();
    private final Map<String, String> passwordByEmail = new ConcurrentHashMap<>();

    public AuthDtos.AuthTokensResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usersByEmail.containsKey(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        }
        AuthDtos.AuthUserDto user = new AuthDtos.AuthUserDto(
                UUID.randomUUID(),
                request.displayName().trim(),
                email,
                request.locale() == null || request.locale().isBlank() ? "ru" : request.locale(),
                Instant.now()
        );
        usersByEmail.put(email, user);
        passwordByEmail.put(email, request.password());
        return tokensFor(user);
    }

    public AuthDtos.AuthTokensResponse login(AuthDtos.LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AuthDtos.AuthUserDto user = usersByEmail.get(email);
        String password = passwordByEmail.get(email);
        if (user == null || password == null || !password.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        return tokensFor(user);
    }

    public AuthDtos.AuthTokensResponse refresh(AuthDtos.RefreshTokenRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token");
        }
        // Dev stub: refresh token encodes user id after "refresh:"
        String raw = request.refreshToken();
        if (!raw.startsWith("refresh:")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token");
        }
        UUID userId = UUID.fromString(raw.substring("refresh:".length()));
        AuthDtos.AuthUserDto user = usersByEmail.values().stream()
                .filter(u -> u.id().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unknown user"));
        return tokensFor(user);
    }

    public void logout(AuthDtos.LogoutRequest request) {
        // Stateless stub — client drops tokens.
    }

    private AuthDtos.AuthTokensResponse tokensFor(AuthDtos.AuthUserDto user) {
        return new AuthDtos.AuthTokensResponse(
                "access:" + user.id(),
                900,
                "refresh:" + user.id(),
                2_592_000,
                "Bearer",
                user
        );
    }
}
