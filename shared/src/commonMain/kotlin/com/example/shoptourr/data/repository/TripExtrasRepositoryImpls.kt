package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.AlertsLocalStore
import com.example.shoptourr.data.local.DiaryLocalStore
import com.example.shoptourr.data.local.TaxFreeLocalStore
import com.example.shoptourr.data.remote.AlertsApi
import com.example.shoptourr.data.remote.DiaryApi
import com.example.shoptourr.data.remote.TaxFreeApi
import com.example.shoptourr.data.remote.dto.alert.AlertSeverity as ApiAlertSeverity
import com.example.shoptourr.data.remote.dto.alert.AlertType as ApiAlertType
import com.example.shoptourr.data.remote.dto.alert.BudgetAlertDto
import com.example.shoptourr.data.remote.dto.diary.CreateDiaryEntryRequest
import com.example.shoptourr.data.remote.dto.diary.DiaryDayGroupDto
import com.example.shoptourr.data.remote.dto.diary.DiaryEntryDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory as ApiPurchaseCategory
import com.example.shoptourr.data.remote.dto.taxfree.TaxFreeEligibleItemDto
import com.example.shoptourr.data.remote.dto.taxfree.TaxFreeRulesDto
import com.example.shoptourr.data.remote.dto.taxfree.TaxFreeSummaryDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.AlertSeverity
import com.example.shoptourr.domain.model.AlertType
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.TaxFreeEligibleItem
import com.example.shoptourr.domain.model.TaxFreeRules
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.repository.AlertsRepository
import com.example.shoptourr.domain.repository.DiaryRepository
import com.example.shoptourr.domain.repository.TaxFreeRepository
import kotlinx.coroutines.flow.Flow

class DiaryRepositoryImpl(
    private val api: DiaryApi,
    private val localStore: DiaryLocalStore,
) : DiaryRepository {
    override fun observeDiary(tripId: String): Flow<List<DiaryDayGroup>> =
        localStore.observe(tripId)

    override suspend fun refresh(tripId: String): Result<Unit> =
        runCatching {
            val days = api.fetchDiary(tripId).days.map { it.toDomain() }
            localStore.replaceDays(tripId, days)
        }.mapHttpAppError()

    override suspend fun create(tripId: String, draft: CreateDiaryDraft): Result<DiaryEntry> =
        runCatching {
            val entry = api.create(
                tripId,
                CreateDiaryEntryRequest(
                    entryDate = draft.entryDate,
                    mood = draft.mood,
                    text = draft.text,
                ),
            ).toDomain()
            localStore.upsertEntry(entry)
            entry
        }.mapHttpAppError()

    override suspend fun delete(tripId: String, entryId: String): Result<Unit> =
        runCatching {
            api.delete(tripId, entryId)
            localStore.removeEntry(tripId, entryId)
        }.mapHttpAppError()
}

class TaxFreeRepositoryImpl(
    private val api: TaxFreeApi,
    private val localStore: TaxFreeLocalStore,
) : TaxFreeRepository {
    override fun observeSummary(tripId: String): Flow<TaxFreeSummary?> =
        localStore.observe(tripId)

    override suspend fun refresh(tripId: String): Result<TaxFreeSummary> =
        runCatching {
            val summary = api.fetchSummary(tripId).toDomain()
            localStore.save(summary)
            summary
        }.mapHttpAppError()
}

class AlertsRepositoryImpl(
    private val api: AlertsApi,
    private val localStore: AlertsLocalStore,
) : AlertsRepository {
    override fun observeAlerts(tripId: String): Flow<List<BudgetAlert>> =
        localStore.observe(tripId)

    override suspend fun refresh(tripId: String): Result<Unit> =
        runCatching {
            val alerts = api.fetchAlerts(tripId).alerts.map { it.toDomain() }
            localStore.replaceAll(tripId, alerts)
        }.mapHttpAppError()
}

private fun DiaryDayGroupDto.toDomain(): DiaryDayGroup =
    DiaryDayGroup(date = date, labelKey = labelKey, entries = entries.map { it.toDomain() })

private fun DiaryEntryDto.toDomain(): DiaryEntry =
    DiaryEntry(
        id = id,
        tripId = tripId,
        entryDate = entryDate,
        mood = mood,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

private fun TaxFreeSummaryDto.toDomain(): TaxFreeSummary =
    TaxFreeSummary(
        tripId = tripId,
        rules = rules.toDomain(),
        eligibleCount = eligibleCount,
        eligibleTotal = Money.parse(eligibleTotal.amount, eligibleTotal.currency),
        estimatedRefundTotal = Money.parse(estimatedRefundTotal.amount, estimatedRefundTotal.currency),
        remainingToMinimum = remainingToMinimum?.let { Money.parse(it.amount, it.currency) },
        items = items.map { it.toDomain() },
    )

private fun TaxFreeRulesDto.toDomain(): TaxFreeRules =
    TaxFreeRules(
        currency = currency,
        minimumPurchase = Money.parse(minimumPurchase.amount, minimumPurchase.currency),
        estimatedRefundRate = estimatedRefundRate,
        regionLabel = regionLabel,
    )

private fun TaxFreeEligibleItemDto.toDomain(): TaxFreeEligibleItem =
    TaxFreeEligibleItem(
        purchaseId = purchaseId,
        name = name,
        amount = Money.parse(amount.amount, amount.currency),
        estimatedRefund = Money.parse(estimatedRefund.amount, estimatedRefund.currency),
        meetsMinimum = meetsMinimum,
    )

private fun BudgetAlertDto.toDomain(): BudgetAlert =
    BudgetAlert(
        id = id,
        type = type.toDomain(),
        severity = severity.toDomain(),
        titleKey = titleKey,
        bodyKey = bodyKey,
        params = params,
        dailyRemaining = dailyRemaining?.let { Money.parse(it.amount, it.currency) },
        category = category?.toDomain(),
        createdAt = createdAt,
        read = read,
    )

private fun ApiAlertType.toDomain(): AlertType = when (this) {
    ApiAlertType.PACE_HIGH -> AlertType.PACE_HIGH
    ApiAlertType.CATEGORY_OVERSPENT -> AlertType.CATEGORY_OVERSPENT
    ApiAlertType.BUDGET_ALMOST_GONE -> AlertType.BUDGET_ALMOST_GONE
    ApiAlertType.BUDGET_EXCEEDED -> AlertType.BUDGET_EXCEEDED
    ApiAlertType.DAILY_ALLOWANCE -> AlertType.DAILY_ALLOWANCE
}

private fun ApiAlertSeverity.toDomain(): AlertSeverity = when (this) {
    ApiAlertSeverity.INFO -> AlertSeverity.INFO
    ApiAlertSeverity.WARNING -> AlertSeverity.WARNING
    ApiAlertSeverity.CRITICAL -> AlertSeverity.CRITICAL
}

private fun ApiPurchaseCategory.toDomain(): PurchaseCategory = when (this) {
    ApiPurchaseCategory.FOOD -> PurchaseCategory.FOOD
    ApiPurchaseCategory.TRANSPORT -> PurchaseCategory.TRANSPORT
    ApiPurchaseCategory.SOUVENIRS -> PurchaseCategory.SOUVENIRS
    ApiPurchaseCategory.HOTEL -> PurchaseCategory.HOTEL
    ApiPurchaseCategory.CULTURE -> PurchaseCategory.CULTURE
    ApiPurchaseCategory.OTHER -> PurchaseCategory.OTHER
}
