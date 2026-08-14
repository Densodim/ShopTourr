package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntryEntity, UUID> {
    List<DiaryEntryEntity> findByTripIdOrderByEntryDateDescCreatedAtDesc(UUID tripId);

    Optional<DiaryEntryEntity> findByIdAndTripId(UUID id, UUID tripId);
}
