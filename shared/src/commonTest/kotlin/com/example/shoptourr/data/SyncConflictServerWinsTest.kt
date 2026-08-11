package com.example.shoptourr.data

import com.example.shoptourr.data.local.InMemoryDiaryLocalStore
import com.example.shoptourr.data.local.InMemoryPurchaseLocalStore
import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.local.InMemoryWishlistLocalStore
import com.example.shoptourr.data.remote.DiaryApi
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.WishlistApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.common.VatBreakdownDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory
import com.example.shoptourr.data.remote.dto.purchase.PurchaseDto
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutboxEntry
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.data.sync.SyncPayloadCodec
import com.example.shoptourr.data.sync.UpdatePurchasePayload
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory as DomainCategory
import com.example.shoptourr.domain.model.VatCalculator
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SyncConflictServerWinsTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    @Test
    fun `409 on update purchase refreshes server entity and clears outbox`() = runTest {
        var patchCalls = 0
        var getCalls = 0
        val engine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Patch -> {
                    patchCalls += 1
                    respond("", status = HttpStatusCode.Conflict)
                }
                HttpMethod.Get -> {
                    getCalls += 1
                    val body = PurchaseDto(
                        id = "p1",
                        tripId = "lisbon",
                        name = "ServerWins",
                        category = PurchaseCategory.FOOD,
                        amount = MoneyDto("9.00", "EUR"),
                        vat = VatBreakdownDto("7.32", "1.68", "9.00", "23", true),
                        taxRefundEligible = false,
                        purchaseDate = "2026-04-15",
                        yourShare = MoneyDto("9.00", "EUR"),
                        createdAt = "2026-04-15T10:00:00Z",
                        updatedAt = "2026-04-15T12:00:00Z",
                    )
                    respond(
                        content = ByteReadChannel(json.encodeToString(body)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                else -> error("unexpected ${request.method}")
            }
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { "token" },
        )
        val local = InMemoryPurchaseLocalStore()
        local.upsert(
            Purchase(
                id = "p1",
                tripId = "lisbon",
                name = "LocalEdit",
                category = DomainCategory.FOOD,
                amount = Money.parse("5.00", "EUR"),
                vat = VatCalculator.breakdown(Money.parse("5.00", "EUR"), "23", true),
                taxRefundEligible = false,
                place = null,
                purchaseDate = "2026-04-15",
                purchaseTime = null,
                pendingSync = true,
            ),
        )
        val outbox = InMemorySyncOutbox()
        outbox.enqueue(
            SyncOutboxEntry(
                id = "o1",
                type = SyncMutationType.UPDATE_PURCHASE,
                payloadJson = SyncPayloadCodec.encodeUpdatePurchase(
                    UpdatePurchasePayload(
                        purchaseId = "p1",
                        tripId = "lisbon",
                        name = "LocalEdit",
                        category = "FOOD",
                        amount = "5.00",
                        currency = "EUR",
                        vatIncluded = true,
                        vatRatePercent = "23",
                        taxRefundEligible = false,
                        place = null,
                        purchaseDate = "2026-04-15",
                        purchaseTime = null,
                    ),
                ),
                idempotencyKey = "k1",
                createdAtEpochMs = 1L,
            ),
        )
        val processor = SyncOutboxProcessor(
            outbox = outbox,
            purchaseApi = PurchaseApi(client, "https://api.test"),
            purchaseLocalStore = local,
            tripApi = TripApi(client, "https://api.test"),
            tripLocalStore = InMemoryTripLocalStore(),
            wishlistApi = WishlistApi(client, "https://api.test"),
            wishlistLocalStore = InMemoryWishlistLocalStore(),
            diaryApi = DiaryApi(client, "https://api.test"),
            diaryLocalStore = InMemoryDiaryLocalStore(),
            clock = { 10L },
        )

        val result = processor.drainOnce()
        assertEquals(1, result.successCount)
        assertEquals(0, result.failureCount)
        assertEquals(1, patchCalls)
        assertEquals(1, getCalls)
        assertEquals("ServerWins", local.getById("p1")!!.name)
        assertEquals(false, local.getById("p1")!!.pendingSync)
        assertTrue(outbox.pending().isEmpty())
    }
}
