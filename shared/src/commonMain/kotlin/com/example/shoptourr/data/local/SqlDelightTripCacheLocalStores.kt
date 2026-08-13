package com.example.shoptourr.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.shoptourr.data.local.cache.TripCacheCodec
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.model.TripRoute
import com.example.shoptourr.domain.model.TripStats
import com.example.shoptourr.epochMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private object TripCacheKind {
    const val TAX_FREE = "TAX_FREE"
    const val ALERTS = "ALERTS"
    const val ROUTE = "ROUTE"
    const val STATS = "STATS"
    const val EXPORT = "EXPORT"
}

class SqlDelightTaxFreeLocalStore(
    private val db: VoyageDatabase,
    private val clock: () -> Long = { epochMillis() },
) : TaxFreeLocalStore {
    override fun observe(tripId: String): Flow<TaxFreeSummary?> =
        db.tripCacheEntityQueries.selectByKindAndTrip(TripCacheKind.TAX_FREE, tripId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.payload_json?.let(TripCacheCodec::decodeTaxFree) }

    override suspend fun save(summary: TaxFreeSummary) {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.upsert(
                kind = TripCacheKind.TAX_FREE,
                trip_id = summary.tripId,
                payload_json = TripCacheCodec.encodeTaxFree(summary),
                updated_at_epoch_ms = clock(),
            )
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.deleteByKind(TripCacheKind.TAX_FREE)
        }
    }
}

class SqlDelightAlertsLocalStore(
    private val db: VoyageDatabase,
    private val clock: () -> Long = { epochMillis() },
) : AlertsLocalStore {
    override fun observe(tripId: String): Flow<List<BudgetAlert>> =
        db.tripCacheEntityQueries.selectByKindAndTrip(TripCacheKind.ALERTS, tripId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.payload_json?.let(TripCacheCodec::decodeAlerts).orEmpty() }

    override suspend fun replaceAll(tripId: String, alerts: List<BudgetAlert>) {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.upsert(
                kind = TripCacheKind.ALERTS,
                trip_id = tripId,
                payload_json = TripCacheCodec.encodeAlerts(alerts),
                updated_at_epoch_ms = clock(),
            )
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.deleteByKind(TripCacheKind.ALERTS)
        }
    }
}

class SqlDelightRouteLocalStore(
    private val db: VoyageDatabase,
    private val clock: () -> Long = { epochMillis() },
) : RouteLocalStore {
    override fun observe(tripId: String): Flow<TripRoute?> =
        db.tripCacheEntityQueries.selectByKindAndTrip(TripCacheKind.ROUTE, tripId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.payload_json?.let(TripCacheCodec::decodeRoute) }

    override suspend fun save(route: TripRoute) {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.upsert(
                kind = TripCacheKind.ROUTE,
                trip_id = route.tripId,
                payload_json = TripCacheCodec.encodeRoute(route),
                updated_at_epoch_ms = clock(),
            )
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.deleteByKind(TripCacheKind.ROUTE)
        }
    }
}

class SqlDelightStatsLocalStore(
    private val db: VoyageDatabase,
    private val clock: () -> Long = { epochMillis() },
) : StatsLocalStore {
    override fun observe(tripId: String): Flow<TripStats?> =
        db.tripCacheEntityQueries.selectByKindAndTrip(TripCacheKind.STATS, tripId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.payload_json?.let(TripCacheCodec::decodeStats) }

    override suspend fun save(stats: TripStats) {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.upsert(
                kind = TripCacheKind.STATS,
                trip_id = stats.tripId,
                payload_json = TripCacheCodec.encodeStats(stats),
                updated_at_epoch_ms = clock(),
            )
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.deleteByKind(TripCacheKind.STATS)
        }
    }
}

class SqlDelightExportLocalStore(
    private val db: VoyageDatabase,
    private val clock: () -> Long = { epochMillis() },
) : ExportLocalStore {
    override fun observe(tripId: String): Flow<ExportJob?> =
        db.tripCacheEntityQueries.selectByKindAndTrip(TripCacheKind.EXPORT, tripId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.payload_json?.let(TripCacheCodec::decodeExport) }

    override suspend fun save(job: ExportJob) {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.upsert(
                kind = TripCacheKind.EXPORT,
                trip_id = job.tripId,
                payload_json = TripCacheCodec.encodeExport(job),
                updated_at_epoch_ms = clock(),
            )
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.tripCacheEntityQueries.deleteByKind(TripCacheKind.EXPORT)
        }
    }
}
