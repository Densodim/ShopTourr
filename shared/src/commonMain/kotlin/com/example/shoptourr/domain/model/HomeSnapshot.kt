package com.example.shoptourr.domain.model

// HomeSnapshot lives next to trip models for presentation mapping.
data class HomeSnapshot(
    val userName: String,
    val currentTripCity: String?,
    val upcomingCount: Int,
    val archiveCount: Int,
    val currentTripId: String? = null,
    /**
     * The whole trip behind [currentTripId]. Home needs its budget, spend and day
     * number to say anything useful; the flat `city`/`id` pair above is kept so
     * existing call sites and stored snapshots stay valid.
     */
    val currentTrip: TripSummary? = null,
    /** Soonest first — Home lists them, it no longer just counts them. */
    val upcoming: List<TripSummary> = emptyList(),
    /** Most recent first. */
    val archive: List<TripSummary> = emptyList(),
)
