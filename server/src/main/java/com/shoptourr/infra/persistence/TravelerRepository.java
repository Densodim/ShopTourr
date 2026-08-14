package com.shoptourr.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TravelerRepository extends JpaRepository<TravelerEntity, UUID> {
    List<TravelerEntity> findByTripIdOrderByCreatedAtAsc(UUID tripId);
}
