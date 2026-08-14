package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, UUID> {
    Optional<MediaAssetEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<MediaAssetEntity> findByIdAndUploadToken(UUID id, String uploadToken);
}
