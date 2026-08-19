package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.common.VatBreakdownDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory
import com.example.shoptourr.data.remote.dto.purchase.PurchaseDto
import com.example.shoptourr.data.remote.dto.purchase.UpdatePurchaseRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PurchaseIfMatchTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    @Test
    fun `patch sends a quoted If-Match when the local updatedAt is known`() = runTest {
        var seen: String? = null
        val body = PurchaseDto(
            id = "p1",
            tripId = "lisbon",
            name = "Tea",
            category = PurchaseCategory.FOOD,
            amount = MoneyDto("4.50", "EUR"),
            vat = VatBreakdownDto("3.66", "0.84", "4.50", "23", true),
            taxRefundEligible = false,
            purchaseDate = "2026-08-13",
            yourShare = MoneyDto("4.50", "EUR"),
            createdAt = "2026-08-13T12:00:00Z",
            updatedAt = "2026-08-13T12:00:00Z",
        )
        val engine = MockEngine { request ->
            seen = request.headers["If-Match"]
            respond(
                content = ByteReadChannel(json.encodeToString(body)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val api = PurchaseApi(
            createVoyageHttpClient("https://api.test", engine, { "t" }),
            "https://api.test",
        )
        api.updatePurchase(
            tripId = "lisbon",
            purchaseId = "p1",
            request = UpdatePurchaseRequest(name = "Tea"),
            ifMatch = "2026-08-13T12:00:00Z",
        )
        assertEquals("\"2026-08-13T12:00:00Z\"", seen)
    }
}
