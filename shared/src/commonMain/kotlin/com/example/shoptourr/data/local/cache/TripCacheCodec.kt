package com.example.shoptourr.data.local.cache

import com.example.shoptourr.domain.model.AlertSeverity
import com.example.shoptourr.domain.model.AlertType
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.CategorySpend
import com.example.shoptourr.domain.model.DailySpend
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.model.GeoPoint
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.RouteStop
import com.example.shoptourr.domain.model.TaxFreeEligibleItem
import com.example.shoptourr.domain.model.TaxFreeRules
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object TripCacheCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun encodeTaxFree(summary: TaxFreeSummary): String = json.encodeToString(summary.toCache())
    fun decodeTaxFree(raw: String): TaxFreeSummary = json.decodeFromString<TaxFreeSummaryCache>(raw).toDomain()

    fun encodeAlerts(alerts: List<BudgetAlert>): String = json.encodeToString(alerts.map { it.toCache() })
    fun decodeAlerts(raw: String): List<BudgetAlert> =
        json.decodeFromString<List<BudgetAlertCache>>(raw).map { it.toDomain() }

    fun encodeRoute(route: TripRoute): String = json.encodeToString(route.toCache())
    fun decodeRoute(raw: String): TripRoute = json.decodeFromString<TripRouteCache>(raw).toDomain()

    fun encodeStats(stats: TripStats): String = json.encodeToString(stats.toCache())
    fun decodeStats(raw: String): TripStats = json.decodeFromString<TripStatsCache>(raw).toDomain()

    fun encodeExport(job: ExportJob): String = json.encodeToString(job.toCache())
    fun decodeExport(raw: String): ExportJob = json.decodeFromString<ExportJobCache>(raw).toDomain()
}

@Serializable
internal data class MoneyCache(val amount: String, val currency: String)

@Serializable
internal data class TaxFreeRulesCache(
    val currency: String,
    val minimumPurchase: MoneyCache,
    val estimatedRefundRate: String,
    val regionLabel: String,
)

@Serializable
internal data class TaxFreeEligibleItemCache(
    val purchaseId: String,
    val name: String,
    val amount: MoneyCache,
    val estimatedRefund: MoneyCache,
    val meetsMinimum: Boolean,
)

@Serializable
internal data class TaxFreeSummaryCache(
    val tripId: String,
    val rules: TaxFreeRulesCache,
    val eligibleCount: Int,
    val eligibleTotal: MoneyCache,
    val estimatedRefundTotal: MoneyCache,
    val remainingToMinimum: MoneyCache? = null,
    val items: List<TaxFreeEligibleItemCache>,
)

@Serializable
internal data class BudgetAlertCache(
    val id: String,
    val type: String,
    val severity: String,
    val titleKey: String,
    val bodyKey: String,
    val params: Map<String, String> = emptyMap(),
    val dailyRemaining: MoneyCache? = null,
    val category: String? = null,
    val createdAt: String,
    val read: Boolean,
)

@Serializable
internal data class GeoPointCache(val lat: String, val lng: String)

@Serializable
internal data class RouteStopCache(
    val id: String,
    val title: String,
    val place: String? = null,
    val date: String? = null,
    val amountSpentHere: MoneyCache? = null,
    val point: GeoPointCache? = null,
    val orderIndex: Int,
)

@Serializable
internal data class TripRouteCache(
    val tripId: String,
    val stopCount: Int,
    val distanceMeters: String? = null,
    val stops: List<RouteStopCache>,
    val path: List<GeoPointCache> = emptyList(),
)

@Serializable
internal data class CategorySpendCache(
    val category: String,
    val amount: MoneyCache,
    val share: String,
    val purchaseCount: Int,
)

@Serializable
internal data class DailySpendCache(
    val date: String,
    val amount: MoneyCache,
    val purchaseCount: Int,
)

@Serializable
internal data class TripStatsCache(
    val tripId: String,
    val totalSpent: MoneyCache,
    val budget: MoneyCache,
    val dailyAverage: MoneyCache,
    val remaining: MoneyCache,
    val onBudget: Boolean,
    val paceDeltaDays: Int? = null,
    val topCategory: String? = null,
    val byCategory: List<CategorySpendCache>,
    val byDay: List<DailySpendCache>,
)

@Serializable
internal data class ExportJobCache(
    val id: String,
    val tripId: String,
    val format: String,
    val status: String,
    val downloadUrl: String? = null,
    val expiresAt: String? = null,
    val errorCode: String? = null,
    val createdAt: String,
    val finishedAt: String? = null,
)

private fun Money.toCache() = MoneyCache(toDecimalString(), currency)
private fun MoneyCache.toDomain() = Money.parse(amount, currency)

private fun TaxFreeSummary.toCache() = TaxFreeSummaryCache(
    tripId = tripId,
    rules = TaxFreeRulesCache(
        currency = rules.currency,
        minimumPurchase = rules.minimumPurchase.toCache(),
        estimatedRefundRate = rules.estimatedRefundRate,
        regionLabel = rules.regionLabel,
    ),
    eligibleCount = eligibleCount,
    eligibleTotal = eligibleTotal.toCache(),
    estimatedRefundTotal = estimatedRefundTotal.toCache(),
    remainingToMinimum = remainingToMinimum?.toCache(),
    items = items.map {
        TaxFreeEligibleItemCache(
            purchaseId = it.purchaseId,
            name = it.name,
            amount = it.amount.toCache(),
            estimatedRefund = it.estimatedRefund.toCache(),
            meetsMinimum = it.meetsMinimum,
        )
    },
)

private fun TaxFreeSummaryCache.toDomain() = TaxFreeSummary(
    tripId = tripId,
    rules = TaxFreeRules(
        currency = rules.currency,
        minimumPurchase = rules.minimumPurchase.toDomain(),
        estimatedRefundRate = rules.estimatedRefundRate,
        regionLabel = rules.regionLabel,
    ),
    eligibleCount = eligibleCount,
    eligibleTotal = eligibleTotal.toDomain(),
    estimatedRefundTotal = estimatedRefundTotal.toDomain(),
    remainingToMinimum = remainingToMinimum?.toDomain(),
    items = items.map {
        TaxFreeEligibleItem(
            purchaseId = it.purchaseId,
            name = it.name,
            amount = it.amount.toDomain(),
            estimatedRefund = it.estimatedRefund.toDomain(),
            meetsMinimum = it.meetsMinimum,
        )
    },
)

private fun BudgetAlert.toCache() = BudgetAlertCache(
    id = id,
    type = type.name,
    severity = severity.name,
    titleKey = titleKey,
    bodyKey = bodyKey,
    params = params,
    dailyRemaining = dailyRemaining?.toCache(),
    category = category?.name,
    createdAt = createdAt,
    read = read,
)

private fun BudgetAlertCache.toDomain() = BudgetAlert(
    id = id,
    type = AlertType.valueOf(type),
    severity = AlertSeverity.valueOf(severity),
    titleKey = titleKey,
    bodyKey = bodyKey,
    params = params,
    dailyRemaining = dailyRemaining?.toDomain(),
    category = category?.let { PurchaseCategory.valueOf(it) },
    createdAt = createdAt,
    read = read,
)

private fun TripRoute.toCache() = TripRouteCache(
    tripId = tripId,
    stopCount = stopCount,
    distanceMeters = distanceMeters,
    stops = stops.map {
        RouteStopCache(
            id = it.id,
            title = it.title,
            place = it.place,
            date = it.date,
            amountSpentHere = it.amountSpentHere?.toCache(),
            point = it.point?.let { p -> GeoPointCache(p.lat, p.lng) },
            orderIndex = it.orderIndex,
        )
    },
    path = path.map { GeoPointCache(it.lat, it.lng) },
)

private fun TripRouteCache.toDomain() = TripRoute(
    tripId = tripId,
    stopCount = stopCount,
    distanceMeters = distanceMeters,
    stops = stops.map {
        RouteStop(
            id = it.id,
            title = it.title,
            place = it.place,
            date = it.date,
            amountSpentHere = it.amountSpentHere?.toDomain(),
            point = it.point?.let { p -> GeoPoint(p.lat, p.lng) },
            orderIndex = it.orderIndex,
        )
    },
    path = path.map { GeoPoint(it.lat, it.lng) },
)

private fun TripStats.toCache() = TripStatsCache(
    tripId = tripId,
    totalSpent = totalSpent.toCache(),
    budget = budget.toCache(),
    dailyAverage = dailyAverage.toCache(),
    remaining = remaining.toCache(),
    onBudget = onBudget,
    paceDeltaDays = paceDeltaDays,
    topCategory = topCategory?.name,
    byCategory = byCategory.map {
        CategorySpendCache(
            category = it.category.name,
            amount = it.amount.toCache(),
            share = it.share,
            purchaseCount = it.purchaseCount,
        )
    },
    byDay = byDay.map {
        DailySpendCache(
            date = it.date,
            amount = it.amount.toCache(),
            purchaseCount = it.purchaseCount,
        )
    },
)

private fun TripStatsCache.toDomain() = TripStats(
    tripId = tripId,
    totalSpent = totalSpent.toDomain(),
    budget = budget.toDomain(),
    dailyAverage = dailyAverage.toDomain(),
    remaining = remaining.toDomain(),
    onBudget = onBudget,
    paceDeltaDays = paceDeltaDays,
    topCategory = topCategory?.let { PurchaseCategory.valueOf(it) },
    byCategory = byCategory.map {
        CategorySpend(
            category = PurchaseCategory.valueOf(it.category),
            amount = it.amount.toDomain(),
            share = it.share,
            purchaseCount = it.purchaseCount,
        )
    },
    byDay = byDay.map {
        DailySpend(
            date = it.date,
            amount = it.amount.toDomain(),
            purchaseCount = it.purchaseCount,
        )
    },
)

private fun ExportJob.toCache() = ExportJobCache(
    id = id,
    tripId = tripId,
    format = format.name,
    status = status.name,
    downloadUrl = downloadUrl,
    expiresAt = expiresAt,
    errorCode = errorCode,
    createdAt = createdAt,
    finishedAt = finishedAt,
)

private fun ExportJobCache.toDomain() = ExportJob(
    id = id,
    tripId = tripId,
    format = ExportFormat.valueOf(format),
    status = ExportJobStatus.valueOf(status),
    downloadUrl = downloadUrl,
    expiresAt = expiresAt,
    errorCode = errorCode,
    createdAt = createdAt,
    finishedAt = finishedAt,
)
