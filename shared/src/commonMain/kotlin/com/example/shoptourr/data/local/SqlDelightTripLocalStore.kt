package com.example.shoptourr.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shoptourr.db.TripEntity
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.epochMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightTripLocalStore(
    private val db: VoyageDatabase,
) : TripLocalStore {

    override suspend fun replaceAll(trips: List<TripSummary>) = withContext(Dispatchers.IO) {
        db.transaction {
            db.tripEntityQueries.deleteAll()
            trips.forEach { upsertInternal(it) }
        }
    }

    override suspend fun upsert(trip: TripSummary) = withContext(Dispatchers.IO) {
        upsertInternal(trip)
    }

    override suspend fun remove(tripId: String) {
        withContext(Dispatchers.IO) {
            db.tripEntityQueries.softDelete(deletedAt = epochMillis(), id = tripId)
        }
    }

    private fun upsertInternal(trip: TripSummary) {
        db.tripEntityQueries.upsert(
            id = trip.id,
            city = trip.city,
            country = trip.country,
            country_code = null,
            flag_emoji = trip.flagEmoji,
            status = trip.status.name,
            start_date = trip.startDate,
            end_date = trip.endDate,
            dates_label = trip.datesLabel,
            budget_amount = trip.budget.toDecimalString(),
            budget_currency = trip.budget.currency,
            spent_amount = trip.spent.toDecimalString(),
            spent_currency = trip.spent.currency,
            purchase_count = trip.purchaseCount.toLong(),
            day_count = (trip.dayCount ?: 0).toLong(),
            current_day_number = trip.currentDayNumber?.toLong(),
            default_vat_rate_percent = "0",
            exchange_rate_json = TripLocalExtrasCodec.encode(trip),
            updated_at_epoch_ms = epochMillis(),
            deleted_at_epoch_ms = null,
        )
    }

    override fun observeAll(): Flow<List<TripSummary>> =
        db.tripEntityQueries.selectAllActive()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override fun all(): List<TripSummary> =
        db.tripEntityQueries.selectAllActive().executeAsList().map { it.toDomain() }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.tripEntityQueries.deleteAll()
        }
    }

    private fun TripEntity.toDomain(): TripSummary {
        val (rate, travelers) = TripLocalExtrasCodec.decode(exchange_rate_json)
        return TripSummary(
            id = id,
            city = city,
            country = country,
            status = TripStatus.valueOf(status),
            startDate = start_date,
            endDate = end_date,
            budget = Money.parse(budget_amount, budget_currency),
            spent = Money.parse(spent_amount, spent_currency),
            purchaseCount = purchase_count.toInt(),
            flagEmoji = flag_emoji,
            datesLabel = dates_label,
            currentDayNumber = current_day_number?.toInt(),
            dayCount = day_count.toInt(),
            exchangeRate = rate,
            travelers = travelers,
        )
    }
}
