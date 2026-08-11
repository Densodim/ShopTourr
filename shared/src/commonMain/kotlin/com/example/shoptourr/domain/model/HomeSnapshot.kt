package com.example.shoptourr.domain.model

// HomeSnapshot lives next to trip models for presentation mapping.
data class HomeSnapshot(
    val userName: String,
    val currentTripCity: String?,
    val upcomingCount: Int,
    val archiveCount: Int,
    val currentTripId: String? = null,
)
