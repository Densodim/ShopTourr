package com.example.shoptourr.domain.model

data class TripDetail(
    val trip: TripSummary,
    val purchases: List<Purchase>,
) {
    val spentTotal: Money
        get() = purchases.fold(Money.zero(trip.budget.currency)) { acc, purchase ->
            if (purchase.amount.currency == acc.currency) acc + purchase.amount else acc
        }

    /** What is left of the budget; negative once the trip goes over. */
    fun remaining(): Money =
        Money(trip.budget.minorUnits - spentTotal.minorUnits, trip.budget.currency)

    fun isOverBudget(): Boolean = spentTotal.minorUnits > trip.budget.minorUnits

    /**
     * Share of the budget already spent, 0..100. A trip with no budget yet reports 0
     * rather than dividing by zero, and overspending saturates at 100 so the bar
     * cannot render past its track.
     */
    fun spendPercent(): Int {
        if (trip.budget.minorUnits <= 0L) return 0
        val raw = spentTotal.minorUnits * 100 / trip.budget.minorUnits
        return raw.coerceIn(0L, 100L).toInt()
    }

    fun vatTotal(): Money = purchases.sumMoney(trip.budget.currency) { it.vat.vat }

    fun taxRefundTotal(): Money = purchases
        .filter { it.taxRefundEligible }
        .sumMoney(trip.budget.currency) { it.vat.vat }

    /** Categories with at least one purchase, heaviest spend first. */
    fun categoriesUsed(): List<PurchaseCategory> = purchases
        .groupBy { it.category }
        .entries
        .sortedByDescending { (_, items) -> items.sumOf { it.amount.minorUnits } }
        .map { it.key }

    /** Purchases bucketed by day, most recent day first, each with its own total. */
    fun dayGroups(filter: PurchaseCategory? = null): List<TripDayGroup> = purchases
        .filter { filter == null || it.category == filter }
        .groupBy { it.purchaseDate }
        .entries
        .sortedByDescending { it.key }
        .map { (date, items) ->
            TripDayGroup(
                date = date,
                total = items.sumMoney(trip.budget.currency) { it.amount },
                items = items.sortedByDescending { it.purchaseTime.orEmpty() },
            )
        }

    private fun List<Purchase>.sumMoney(
        currency: String,
        select: (Purchase) -> Money,
    ): Money = fold(Money.zero(currency)) { acc, purchase ->
        val value = select(purchase)
        if (value.currency == acc.currency) acc + value else acc
    }
}

/** One day of a trip's spending, as the trip screen lists it. */
data class TripDayGroup(
    val date: String,
    val total: Money,
    val items: List<Purchase>,
)
