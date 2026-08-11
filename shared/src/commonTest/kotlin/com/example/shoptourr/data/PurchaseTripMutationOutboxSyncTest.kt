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
import com.example.shoptourr.data.remote.dto.trip.TripDto
import com.example.shoptourr.data.remote.dto.trip.TripStatus
import com.example.shoptourr.data.repository.PurchaseRepositoryImpl
import com.example.shoptourr.data.repository.SyncRepositoryImpl
import com.example.shoptourr.data.repository.TripRepositoryImpl
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory as DomainCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.TripStatus as DomainTripStatus
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.model.UpdateTripDraft
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
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

class PurchaseTripMutationOutboxSyncTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    @Test
    fun `update and delete purchase drain through outbox`() = runTest {
        var patched = 0
        var deleted = 0
        val engine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Patch -> {
                    patched += 1
                    val body = purchaseDto(id = "p1", name = "Updated")
                    respond(
                        content = ByteReadChannel(json.encodeToString(body)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                HttpMethod.Delete -> {
                    deleted += 1
                    respond("", status = HttpStatusCode.NoContent)
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
            com.example.shoptourr.domain.model.Purchase(
                id = "p1",
                tripId = "lisbon",
                name = "Old",
                category = DomainCategory.FOOD,
                amount = Money.parse("4.50", "EUR"),
                vat = com.example.shoptourr.domain.model.VatCalculator.breakdown(
                    Money.parse("4.50", "EUR"), "23", true,
                ),
                taxRefundEligible = false,
                place = null,
                purchaseDate = "2026-04-15",
                purchaseTime = null,
                pendingSync = false,
            ),
        )
        val outbox = InMemorySyncOutbox()
        val repo = PurchaseRepositoryImpl(
            api = PurchaseApi(client, "https://api.test"),
            localStore = local,
            outbox = outbox,
            idGenerator = { "unused" },
            clock = { 1_700_000_000_000L },
        )
        val processor = processor(client, outbox, local)
        val drain = DrainSyncOutboxUseCase(SyncRepositoryImpl(processor))

        repo.update(
            tripId = "lisbon",
            purchaseId = "p1",
            draft = PurchaseDraft(
                name = "Updated",
                category = DomainCategory.FOOD,
                amount = Money.parse("5.00", "EUR"),
                vatIncluded = true,
                vatRatePercent = "23",
                place = null,
            ),
        ).getOrThrow()
        drain()
        assertEquals(1, patched)
        assertEquals("Updated", local.getById("p1")!!.name)
        assertEquals(false, local.getById("p1")!!.pendingSync)

        repo.delete("lisbon", "p1").getOrThrow()
        drain()
        assertEquals(1, deleted)
        assertTrue(local.observeByTrip("lisbon").first().isEmpty())
        assertTrue(outbox.pending().isEmpty())
    }

    @Test
    fun `update and delete trip drain through outbox`() = runTest {
        var patched = 0
        var deleted = 0
        val engine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Patch -> {
                    patched += 1
                    respond(
                        content = ByteReadChannel(
                            json.encodeToString(
                                TripDto(
                                    id = "t1",
                                    city = "Porto",
                                    country = "Portugal",
                                    status = TripStatus.ACTIVE,
                                    startDate = "2026-04-01",
                                    endDate = "2026-04-10",
                                    budget = MoneyDto("1000.00", "EUR"),
                                    spent = MoneyDto("10.00", "EUR"),
                                    remaining = MoneyDto("990.00", "EUR"),
                                    purchaseCount = 1,
                                    dayCount = 10,
                                    defaultVatRatePercent = "23",
                                    createdAt = "2026-03-01T00:00:00Z",
                                    updatedAt = "2026-04-01T00:00:00Z",
                                ),
                            ),
                        ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                HttpMethod.Delete -> {
                    deleted += 1
                    respond("", status = HttpStatusCode.NoContent)
                }
                else -> error("unexpected ${request.method}")
            }
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { "token" },
        )
        val trips = InMemoryTripLocalStore()
        trips.upsert(
            TripSummary(
                id = "t1",
                city = "Lisbon",
                country = "Portugal",
                status = DomainTripStatus.ACTIVE,
                startDate = "2026-04-01",
                endDate = "2026-04-10",
                budget = Money.parse("1000.00", "EUR"),
                spent = Money.parse("10.00", "EUR"),
                purchaseCount = 1,
            ),
        )
        val outbox = InMemorySyncOutbox()
        val repo = TripRepositoryImpl(
            homeApi = com.example.shoptourr.data.remote.HomeApi(client, "https://api.test"),
            tripApi = TripApi(client, "https://api.test"),
            localStore = trips,
            outbox = outbox,
            idGenerator = { "unused" },
            clock = { 1_700_000_000_000L },
        )
        val processor = SyncOutboxProcessor(
            outbox = outbox,
            purchaseApi = PurchaseApi(client, "https://api.test"),
            purchaseLocalStore = InMemoryPurchaseLocalStore(),
            tripApi = TripApi(client, "https://api.test"),
            tripLocalStore = trips,
            wishlistApi = WishlistApi(client, "https://api.test"),
            wishlistLocalStore = InMemoryWishlistLocalStore(),
            diaryApi = DiaryApi(client, "https://api.test"),
            diaryLocalStore = InMemoryDiaryLocalStore(),
            clock = { 1_700_000_000_100L },
        )
        val drain = DrainSyncOutboxUseCase(SyncRepositoryImpl(processor))

        repo.updateTrip("t1", UpdateTripDraft(city = "Porto")).getOrThrow()
        drain()
        assertEquals(1, patched)
        assertEquals("Porto", trips.all().single().city)

        repo.deleteTrip("t1").getOrThrow()
        drain()
        assertEquals(1, deleted)
        assertTrue(trips.all().isEmpty())
        assertTrue(outbox.pending().isEmpty())
    }

    private fun processor(
        client: io.ktor.client.HttpClient,
        outbox: InMemorySyncOutbox,
        local: InMemoryPurchaseLocalStore,
    ) = SyncOutboxProcessor(
        outbox = outbox,
        purchaseApi = PurchaseApi(client, "https://api.test"),
        purchaseLocalStore = local,
        tripApi = TripApi(client, "https://api.test"),
        tripLocalStore = InMemoryTripLocalStore(),
        wishlistApi = WishlistApi(client, "https://api.test"),
        wishlistLocalStore = InMemoryWishlistLocalStore(),
        diaryApi = DiaryApi(client, "https://api.test"),
        diaryLocalStore = InMemoryDiaryLocalStore(),
        clock = { 1_700_000_000_100L },
    )

    private fun purchaseDto(id: String, name: String) = PurchaseDto(
        id = id,
        tripId = "lisbon",
        name = name,
        category = PurchaseCategory.FOOD,
        amount = MoneyDto("5.00", "EUR"),
        vat = VatBreakdownDto("4.07", "0.93", "5.00", "23", true),
        taxRefundEligible = false,
        purchaseDate = "2026-04-15",
        yourShare = MoneyDto("5.00", "EUR"),
        createdAt = "2026-04-15T10:24:00Z",
        updatedAt = "2026-04-15T11:00:00Z",
    )
}
