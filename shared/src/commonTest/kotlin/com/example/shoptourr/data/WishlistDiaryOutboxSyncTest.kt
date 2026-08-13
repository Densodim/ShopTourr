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
import com.example.shoptourr.data.remote.dto.diary.DiaryEntryDto
import com.example.shoptourr.data.remote.dto.wishlist.WishlistItemDto
import com.example.shoptourr.data.repository.DiaryRepositoryImpl
import com.example.shoptourr.data.repository.SyncRepositoryImpl
import com.example.shoptourr.data.repository.WishlistRepositoryImpl
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.usecase.CreateDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.CreateWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DeleteWishlistItemUseCase
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import com.example.shoptourr.domain.model.WishlistItem
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

class WishlistDiaryOutboxSyncTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    @Test
    fun `create wishlist is local-first then outbox drains to api`() = runTest {
        var posted = 0
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertTrue(request.url.encodedPath.endsWith("/wishlist"))
            assertEquals("local-w1", request.headers["Idempotency-Key"])
            posted += 1
            val body = WishlistItemDto(
                id = "server-w1",
                name = "Pastel",
                city = "Lisbon",
                targetPrice = MoneyDto("1.20", "EUR"),
                iconEmoji = null,
                note = null,
                createdAt = "2026-08-11T12:00:00Z",
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
        val local = InMemoryWishlistLocalStore()
        val outbox = InMemorySyncOutbox()
        val processor = SyncOutboxProcessor(
            outbox = outbox,
            purchaseApi = PurchaseApi(client, "https://api.test"),
            purchaseLocalStore = InMemoryPurchaseLocalStore(),
            tripApi = TripApi(client, "https://api.test"),
            tripLocalStore = InMemoryTripLocalStore(),
            wishlistApi = WishlistApi(client, "https://api.test"),
            wishlistLocalStore = local,
            diaryApi = DiaryApi(client, "https://api.test"),
            diaryLocalStore = InMemoryDiaryLocalStore(),
            clock = { 1_700_000_000_100L },
        )
        val created = CreateWishlistItemUseCase(
            wishlistRepository = WishlistRepositoryImpl(
                api = WishlistApi(client, "https://api.test"),
                localStore = local,
                outbox = outbox,
                idGenerator = { "local-w1" },
                clock = { 1_700_000_000_000L },
            ),
            drainSyncOutbox = DrainSyncOutboxUseCase(SyncRepositoryImpl(processor)),
        )(
            CreateWishlistDraft(
                name = "Pastel",
                city = "Lisbon",
                targetPrice = Money.parse("1.20", "EUR"),
            ),
        ).getOrThrow()

        assertEquals("local-w1", created.id)
        assertEquals(1, posted)
        assertTrue(outbox.pending().isEmpty())
        assertEquals("server-w1", local.observe().first().single().id)
    }

    @Test
    fun `create diary is local-first then outbox drains to api`() = runTest {
        var posted = 0
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertTrue(request.url.encodedPath.contains("/diary"))
            assertEquals("local-d1", request.headers["Idempotency-Key"])
            posted += 1
            val body = DiaryEntryDto(
                id = "server-d1",
                tripId = "lisbon",
                entryDate = "2026-08-11",
                mood = "happy",
                text = "Pasteis day",
                createdAt = "2026-08-11T12:00:00Z",
                updatedAt = "2026-08-11T12:00:00Z",
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
        val local = InMemoryDiaryLocalStore()
        val outbox = InMemorySyncOutbox()
        val processor = SyncOutboxProcessor(
            outbox = outbox,
            purchaseApi = PurchaseApi(client, "https://api.test"),
            purchaseLocalStore = InMemoryPurchaseLocalStore(),
            tripApi = TripApi(client, "https://api.test"),
            tripLocalStore = InMemoryTripLocalStore(),
            wishlistApi = WishlistApi(client, "https://api.test"),
            wishlistLocalStore = InMemoryWishlistLocalStore(),
            diaryApi = DiaryApi(client, "https://api.test"),
            diaryLocalStore = local,
            clock = { 1_700_000_000_100L },
        )
        val created = CreateDiaryEntryUseCase(
            diaryRepository = DiaryRepositoryImpl(
                api = DiaryApi(client, "https://api.test"),
                localStore = local,
                outbox = outbox,
                idGenerator = { "local-d1" },
                clock = { 1_700_000_000_000L },
                today = { "2026-08-11" },
            ),
            drainSyncOutbox = DrainSyncOutboxUseCase(SyncRepositoryImpl(processor)),
        )(
            tripId = "lisbon",
            draft = CreateDiaryDraft(mood = "happy", text = "Pasteis day"),
        ).getOrThrow()

        assertEquals("local-d1", created.id)
        assertEquals(1, posted)
        assertTrue(outbox.pending().isEmpty())
        assertEquals(
            "server-d1",
            local.observe("lisbon").first().single().entries.single().id,
        )
    }

    @Test
    fun `delete wishlist is local-first then outbox drains to api`() = runTest {
        var deleted = 0
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Delete, request.method)
            assertTrue(request.url.encodedPath.endsWith("/wishlist/w1"))
            deleted += 1
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { "token" },
        )
        val local = InMemoryWishlistLocalStore()
        local.upsert(
            WishlistItem(
                id = "w1",
                name = "Pastel",
                city = "Lisbon",
                targetPrice = Money.parse("1.20", "EUR"),
                iconEmoji = null,
                note = null,
                createdAt = "2026-08-11T12:00:00Z",
            ),
        )
        val outbox = InMemorySyncOutbox()
        val processor = SyncOutboxProcessor(
            outbox = outbox,
            purchaseApi = PurchaseApi(client, "https://api.test"),
            purchaseLocalStore = InMemoryPurchaseLocalStore(),
            tripApi = TripApi(client, "https://api.test"),
            tripLocalStore = InMemoryTripLocalStore(),
            wishlistApi = WishlistApi(client, "https://api.test"),
            wishlistLocalStore = local,
            diaryApi = DiaryApi(client, "https://api.test"),
            diaryLocalStore = InMemoryDiaryLocalStore(),
            clock = { 1_700_000_000_100L },
        )
        DeleteWishlistItemUseCase(
            wishlistRepository = WishlistRepositoryImpl(
                api = WishlistApi(client, "https://api.test"),
                localStore = local,
                outbox = outbox,
                idGenerator = { "x" },
                clock = { 1_700_000_000_000L },
            ),
            drainSyncOutbox = DrainSyncOutboxUseCase(SyncRepositoryImpl(processor)),
        )("w1").getOrThrow()

        assertEquals(1, deleted)
        assertTrue(outbox.pending().isEmpty())
        assertTrue(local.observe().first().isEmpty())
    }
}
