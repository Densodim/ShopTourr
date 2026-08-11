package com.example.shoptourr.domain.model

data class GeoPoint(
    val lat: String,
    val lng: String,
)

data class RouteStop(
    val id: String,
    val title: String,
    val place: String? = null,
    val date: String? = null,
    val amountSpentHere: Money? = null,
    val point: GeoPoint? = null,
    val orderIndex: Int,
)

data class TripRoute(
    val tripId: String,
    val stopCount: Int,
    val distanceMeters: String? = null,
    val stops: List<RouteStop>,
    val path: List<GeoPoint> = emptyList(),
)

data class CategorySpend(
    val category: PurchaseCategory,
    val amount: Money,
    val share: String,
    val purchaseCount: Int,
)

data class DailySpend(
    val date: String,
    val amount: Money,
    val purchaseCount: Int,
)

data class TripStats(
    val tripId: String,
    val totalSpent: Money,
    val budget: Money,
    val dailyAverage: Money,
    val remaining: Money,
    val onBudget: Boolean,
    val paceDeltaDays: Int? = null,
    val topCategory: PurchaseCategory? = null,
    val byCategory: List<CategorySpend>,
    val byDay: List<DailySpend>,
)
