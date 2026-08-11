package com.example.shoptourr.data.local.cache

import com.example.shoptourr.domain.model.AlertSeverity
import com.example.shoptourr.domain.model.AlertType
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.TaxFreeRules
import com.example.shoptourr.domain.model.TaxFreeSummary
import kotlin.test.Test
import kotlin.test.assertEquals

class TripCacheCodecTest {

    @Test
    fun `tax free roundtrip`() {
        val summary = TaxFreeSummary(
            tripId = "t1",
            rules = TaxFreeRules(
                currency = "EUR",
                minimumPurchase = Money.parse("50.00", "EUR"),
                estimatedRefundRate = "0.13",
                regionLabel = "EU",
            ),
            eligibleCount = 0,
            eligibleTotal = Money.parse("0.00", "EUR"),
            estimatedRefundTotal = Money.parse("0.00", "EUR"),
            items = emptyList(),
        )
        assertEquals(summary, TripCacheCodec.decodeTaxFree(TripCacheCodec.encodeTaxFree(summary)))
    }

    @Test
    fun `alerts and export roundtrip`() {
        val alerts = listOf(
            BudgetAlert(
                id = "a1",
                type = AlertType.PACE_HIGH,
                severity = AlertSeverity.WARNING,
                titleKey = "pace",
                bodyKey = "body",
                createdAt = "2026-08-11T00:00:00Z",
                read = false,
            ),
        )
        assertEquals(alerts, TripCacheCodec.decodeAlerts(TripCacheCodec.encodeAlerts(alerts)))

        val job = ExportJob(
            id = "e1",
            tripId = "t1",
            format = ExportFormat.PDF,
            status = ExportJobStatus.READY,
            downloadUrl = "https://cdn/x",
            createdAt = "2026-08-11T00:00:00Z",
        )
        assertEquals(job, TripCacheCodec.decodeExport(TripCacheCodec.encodeExport(job)))
    }
}
