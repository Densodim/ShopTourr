package com.example.shoptourr.data

import com.example.shoptourr.data.local.InMemoryPurchaseLocalStore
import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.repository.PurchaseRepositoryImpl
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncMutationType
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory as DomainCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.common.VatBreakdownDto
import com.example.shoptourr.data.remote.dto.purchase.PurchaseCategory
import com.example.shoptourr.data.remote.dto.purchase.PurchaseDto

class PurchaseOutboxSyncTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    @Test
    fun `create purchase is local-first then outbox drains to api`() = runTest {
        var posted = 0
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertTrue(request.url.encodedPath.contains("/purchases"))
            posted += 1
            val body = PurchaseDto(
                id = "server-1",
                tripId = "lisbon",
                name = "Pasteis",
                category = PurchaseCategory.FOOD,
                amount = MoneyDto("4.50", "EUR"),
                vat = VatBreakdownDto("3.66", "0.84", "4.50", "23", true),
                taxRefundEligible = false,
                place = "Belem",
                purchaseDate = "2026-04-15",
                purchaseTime = "10:24",
                yourShare = MoneyDto("4.50", "EUR"),
                createdAt = "2026-04-15T10:24:00Z",
                updatedAt = "2026-04-15T10:24:00Z",
            )
            respond(
                content = ByteReadChannel(json.encodeToString(body)),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { "token" },
        )
        val local = InMemoryPurchaseLocalStore()
        val outbox = InMemorySyncOutbox()
        val repo = PurchaseRepositoryImpl(
            api = PurchaseApi(client, "https://api.test"),
            localStore = local,
            outbox = outbox,
            idGenerator = { "local-1" },
            clock = { 1_700_000_000_000L },
        )
        val created = CreatePurchaseUseCase(repo)(
            tripId = "lisbon",
            draft = PurchaseDraft(
                name = "Pasteis",
                category = DomainCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = "Belem",
            ),
        ).getOrThrow()

        assertEquals("local-1", created.id)
        assertEquals(1, local.observeByTrip("lisbon").first().size)
        assertEquals(1, outbox.pending().size)
        assertEquals(SyncMutationType.CREATE_PURCHASE, outbox.pending().single().type)

        val processor = SyncOutboxProcessor(
            outbox = outbox,
            purchaseApi = PurchaseApi(client, "https://api.test"),
            purchaseLocalStore = local,
            tripApi = TripApi(client, "https://api.test"),
            tripLocalStore = InMemoryTripLocalStore(),
            clock = { 1_700_000_000_100L },
        )
        val drained = processor.drainOnce()

        assertEquals(1, drained.successCount)
        assertEquals(0, drained.failureCount)
        assertEquals(1, posted)
        assertTrue(outbox.pending().isEmpty())
        assertEquals("server-1", local.observeByTrip("lisbon").first().single().id)
    }
}
