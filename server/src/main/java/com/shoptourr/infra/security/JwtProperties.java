package com.shoptourr.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "voyage.jwt")
public record JwtProperties(
        String secret,
        Duration accessTtl,
        Duration refreshTtl
) {}
