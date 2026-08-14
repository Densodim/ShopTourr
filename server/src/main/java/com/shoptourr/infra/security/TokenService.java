package com.shoptourr.infra.security;

import com.shoptourr.infra.persistence.RefreshSessionRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenService {

    public static final String SESSION_CLAIM = "sid";

    private final JwtProperties properties;
    private final JwtEncoder encoder;
    private final Clock clock;
    private final RefreshSessionRepository sessions;
    private final SecureRandom random = new SecureRandom();

    public TokenService(
            JwtProperties properties,
            JwtEncoder encoder,
            Clock clock,
            RefreshSessionRepository sessions
    ) {
        this.properties = properties;
        this.encoder = encoder;
        this.clock = clock;
        this.sessions = sessions;
    }

    public IssuedTokens issue(UUID userId, String email, UUID sessionId) {
        Instant now = clock.instant();
        Instant accessExp = now.plus(properties.accessTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim(SESSION_CLAIM, sessionId.toString())
                .issuedAt(now)
                .expiresAt(accessExp)
                .build();
        String access = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();
        return new IssuedTokens(
                access,
                properties.accessTtl().toSeconds(),
                properties.refreshTtl().toSeconds()
        );
    }

    public String newRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashRefreshToken(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static SecretKey hmacKey(String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(raw, 0, padded, 0, raw.length);
            raw = padded;
        }
        return new SecretKeySpec(raw, "HmacSHA256");
    }

    public static JwtEncoder encoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public static JwtDecoder decoder(SecretKey key, RefreshSessionRepository sessions) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> sessionValidator = jwt -> {
            String sid = jwt.getClaimAsString(SESSION_CLAIM);
            if (sid == null || sid.isBlank()) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "missing sid", null));
            }
            UUID sessionId;
            try {
                sessionId = UUID.fromString(sid);
            } catch (IllegalArgumentException ex) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "bad sid", null));
            }
            if (!sessions.existsById(sessionId)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "session revoked", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                sessionValidator
        ));
        return decoder;
    }

    public record IssuedTokens(String accessToken, long accessExpiresIn, long refreshExpiresIn) {}
}
