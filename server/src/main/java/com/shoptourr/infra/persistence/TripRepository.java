package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {
    List<TripEntity> findByUserIdAndDeletedAtIsNullOrderByStartDateDesc(UUID userId);

    Optional<TripEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
