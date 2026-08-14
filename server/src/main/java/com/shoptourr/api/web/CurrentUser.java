package com.shoptourr.api.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class CurrentUser {

    private CurrentUser() {}

    public static UUID id(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("missing jwt principal");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
