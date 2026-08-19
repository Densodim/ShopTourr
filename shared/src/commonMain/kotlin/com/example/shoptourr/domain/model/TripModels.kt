package com.example.shoptourr.domain.model

data class Traveler(
    val id: String,
    val name: String,
    val colorHex: String,
    val avatarGlyph: String,
    val isOwner: Boolean,
)

data class CreateTravelerDraft(
    val name: String,
    val colorHex: String = "#FFD84D",
    val avatarGlyph: String? = null,
)

data class ExchangeRate(
    val tripCurrency: String,
    val quoteCurrency: String,
    val rate: String,
    val rateDate: String,
    val provider: String? = null,
)

enum class TripInviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }

data class TripInvite(
    val id: String,
    val tripId: String,
    val email: String,
    val status: TripInviteStatus,
    val createdAt: String,
    val expiresAt: String? = null,
)

enum class TripStatus {
    UPCOMING,
    ACTIVE,
    PAST,
    ARCHIVED,
}

data class TripSummary(
    val id: String,
    val city: String,
    val country: String,
    val status: TripStatus,
    val startDate: String,
    val endDate: String,
    val budget: Money,
    val spent: Money,
    val purchaseCount: Int,
    val flagEmoji: String? = null,
    val datesLabel: String? = null,
    val currentDayNumber: Int? = null,
    val dayCount: Int? = null,
    val exchangeRate: ExchangeRate? = null,
    val travelers: List<Traveler> = emptyList(),
) {
    /** What is left of the budget; negative once the trip goes over. */
    val remaining: Money
        get() = Money(budget.minorUnits - spentInBudgetCurrency().minorUnits, budget.currency)

    val isOverBudget: Boolean
        get() = spentInBudgetCurrency().minorUnits > budget.minorUnits

    /**
     * Share of the budget already spent, 0..100. A trip with no budget reports 0
     * rather than dividing by zero, and overspending saturates so a progress track
     * cannot render past its own width.
     */
    fun spendPercent(): Int {
        if (budget.minorUnits <= 0L) return 0
        val raw = spentInBudgetCurrency().minorUnits * 100 / budget.minorUnits
        return raw.coerceIn(0L, 100L).toInt()
    }

    /** Days still to come, today included; null when the trip has no day counter. */
    fun daysLeft(): Int? {
        val total = dayCount ?: return null
        val today = currentDayNumber ?: return null
        return (total - today + 1).coerceAtLeast(0)
    }

    /**
     * What is left to spend per remaining day — the number that actually decides
     * whether today's purchase is affordable. Null when the trip is over, has no
     * day counter, or is already over budget.
     */
    fun dailyAllowance(): Money? {
        val days = daysLeft()?.takeIf { it > 0 } ?: return null
        val left = remaining.minorUnits
        if (left <= 0L) return null
        return Money(left / days, budget.currency)
    }

    /** Average spend per elapsed day, for pacing against [dailyAllowance]. */
    fun averagePerDay(): Money? {
        val elapsed = currentDayNumber?.takeIf { it > 0 } ?: return null
        return Money(spentInBudgetCurrency().minorUnits / elapsed, budget.currency)
    }

    /**
     * The server reports both in the trip currency, but a stale cached row could
     * disagree; treat a mismatch as "nothing spent" rather than doing arithmetic
     * across currencies.
     */
    private fun spentInBudgetCurrency(): Money =
        if (spent.currency == budget.currency) spent else Money.zero(budget.currency)

    companion object {
        fun toHomeSnapshot(userName: String, trips: List<TripSummary>): HomeSnapshot {
            val current = trips.firstOrNull { it.status == TripStatus.ACTIVE }
            val upcoming = trips
                .filter { it.status == TripStatus.UPCOMING }
                .sortedBy { it.startDate }
            val archive = trips
                .filter { it.status == TripStatus.PAST || it.status == TripStatus.ARCHIVED }
                .sortedByDescending { it.startDate }
            return HomeSnapshot(
                userName = userName,
                currentTripCity = current?.city,
                upcomingCount = upcoming.size,
                archiveCount = archive.size,
                currentTripId = current?.id,
                currentTrip = current,
                upcoming = upcoming,
                archive = archive,
            )
        }
    }
}

data class CreateTripDraft(
    val city: String,
    val country: String,
    val startDate: String,
    val endDate: String,
    val budget: Money,
    val countryCode: String? = null,
    val defaultVatRatePercent: String? = null,
    val quoteCurrency: String? = null,
    val travelers: List<CreateTravelerDraft> = emptyList(),
)

data class UpdateTripDraft(
    val city: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val budget: Money? = null,
    val defaultVatRatePercent: String? = null,
    val status: TripStatus? = null,
)
