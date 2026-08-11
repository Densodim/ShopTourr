package com.example.shoptourr.domain.model

data class TripDetail(
    val trip: TripSummary,
    val purchases: List<Purchase>,
) {
    val spentTotal: Money
        get() = purchases.fold(Money.zero(trip.budget.currency)) { acc, purchase ->
            if (purchase.amount.currency == acc.currency) acc + purchase.amount else acc
        }
}
