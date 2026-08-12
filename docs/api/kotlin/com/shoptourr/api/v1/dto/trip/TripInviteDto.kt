package com.shoptourr.api.v1.dto.trip

import java.time.Instant
import java.util.UUID

data class TripInviteDto(
    val id: UUID,
    val tripId: UUID,
    val email: String,
    val status: TripInviteStatus,
    val createdAt: Instant,
    val expiresAt: Instant?,
)
