package com.shoptourr.application;

import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.IdempotencyKeyEntity;
import com.shoptourr.infra.persistence.IdempotencyKeyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository keys;
    private final JsonMapper jsonMapper;

    public IdempotencyService(IdempotencyKeyRepository keys, JsonMapper jsonMapper) {
        this.keys = keys;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public <T> ResponseEntity<T> run(
            UUID userId,
            String idempotencyKey,
            String route,
            Object requestBody,
            int successStatus,
            Class<T> responseType,
            Supplier<T> action
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw ApiException.validation("Idempotency-Key is required");
        }
        String hash = hashBody(requestBody);
        Optional<IdempotencyKeyEntity> existing = keys.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKeyEntity row = existing.get();
            if (!row.getRequestHash().equals(hash) || !row.getRoute().equals(route)) {
                throw ApiException.idempotencyConflict("Idempotency-Key reused with a different request");
            }
            T replayed = jsonMapper.readValue(row.getResponseBody(), responseType);
            return ResponseEntity.status(row.getStatusCode()).body(replayed);
        }
        T result = action.get();
        IdempotencyKeyEntity row = new IdempotencyKeyEntity();
        row.setUserId(userId);
        row.setIdempotencyKey(idempotencyKey);
        row.setRoute(route);
        row.setRequestHash(hash);
        row.setStatusCode(successStatus);
        row.setResponseBody(jsonMapper.writeValueAsString(result));
        keys.save(row);
        return ResponseEntity.status(successStatus).body(result);
    }

    private String hashBody(Object requestBody) {
        byte[] payload = jsonMapper.writeValueAsBytes(requestBody);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("cannot hash idempotency body", e);
        }
    }
}
