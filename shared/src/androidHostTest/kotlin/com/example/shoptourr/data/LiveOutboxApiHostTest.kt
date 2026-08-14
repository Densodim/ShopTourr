package com.example.shoptourr.data

import com.example.shoptourr.data.local.InMemoryDiaryLocalStore
import com.example.shoptourr.data.local.InMemoryPurchaseLocalStore
import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.local.InMemoryWishlistLocalStore
import com.example.shoptourr.data.remote.AuthApi
import com.example.shoptourr.data.remote.DiaryApi
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.data.remote.PurchaseApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.WishlistApi
import com.example.shoptourr.data.remote.createPlatformHttpEngine
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.repository.AuthRepositoryImpl
import com.example.shoptourr.data.repository.PurchaseRepositoryImpl
import com.example.shoptourr.data.repository.SyncRepositoryImpl
import com.example.shoptourr.data.repository.TripRepositoryImpl
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.data.sync.SyncOutboxProcessor
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.usecase.CreatePurchaseUseCase
import com.example.shoptourr.domain.usecase.CreateTripUseCase
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assume

/**
 * Talks to a running ShopTourBoot on loopback.
 *
 *     cd ShopTourBoot && ./scripts/run.sh
 *     ./gradlew :shared:testAndroidHostTest --tests com.example.shoptourr.data.LiveOutboxApiHostTest
 *
 * Skipped when `/api/_ping` is unreachable so CI stays green without the API.
 */
class LiveOutboxApiHostTest {

    private val baseUrl = System.getenv("VOYAGE_LIVE_API_URL")?.trimEnd('/')
        ?: AppConfig.JVM_LOCAL_API

    @Test
    fun `outbox drains create trip and purchase to live api`() = runBlocking {
        Assume.assumeTrue(
            "ShopTourBoot is not running at $baseUrl",
            ping(baseUrl),
        )

        val tokens = MemoryTokenStore()
        val client = createVoyageHttpClient(
            baseUrl = baseUrl,
            engine = createPlatformHttpEngine(),
            tokenProvider = { tokens.accessToken() },
            refreshTokenProvider = { tokens.refreshToken() },
        )
        try {
            val auth = AuthRepositoryImpl(AuthApi(client, baseUrl), tokens)
            val email = "live-${UUID.randomUUID()}@example.com"
            auth.register(
                displayName = "Live Outbox",
                email = email,
                password = "secret12",
                locale = "ru",
            ).getOrThrow()

            val trips = InMemoryTripLocalStore()
            val purchases = InMemoryPurchaseLocalStore()
            val outbox = InMemorySyncOutbox()
            val processor = SyncOutboxProcessor(
                outbox = outbox,
                purchaseApi = PurchaseApi(client, baseUrl),
                purchaseLocalStore = purchases,
                tripApi = TripApi(client, baseUrl),
                tripLocalStore = trips,
                wishlistApi = WishlistApi(client, baseUrl),
                wishlistLocalStore = InMemoryWishlistLocalStore(),
                diaryApi = DiaryApi(client, baseUrl),
                diaryLocalStore = InMemoryDiaryLocalStore(),
                clock = { System.currentTimeMillis() },
            )
            val drain = DrainSyncOutboxUseCase(SyncRepositoryImpl(processor))
            val tripRepo = TripRepositoryImpl(
                homeApi = HomeApi(client, baseUrl),
                tripApi = TripApi(client, baseUrl),
                localStore = trips,
                outbox = outbox,
                idGenerator = { "local-trip-${UUID.randomUUID()}" },
                clock = { System.currentTimeMillis() },
            )
            val purchaseRepo = PurchaseRepositoryImpl(
                api = PurchaseApi(client, baseUrl),
                localStore = purchases,
                outbox = outbox,
                idGenerator = { "local-buy-${UUID.randomUUID()}" },
                clock = { System.currentTimeMillis() },
            )

            val localTrip = CreateTripUseCase(tripRepo)(
                CreateTripDraft(
                    city = "Lisbon",
                    country = "Portugal",
                    countryCode = "PT",
                    startDate = "2026-08-10",
                    endDate = "2026-08-20",
                    budget = Money.parse("1500.00", "EUR"),
                ),
            ).getOrThrow()
            assertTrue(localTrip.id.startsWith("local-trip-"))
            assertEquals(1, outbox.pending().size)

            val tripDrain = drain().getOrThrow()
            assertEquals(0, tripDrain.failureCount, "trip drain failed, pending=${outbox.pending()}")
            assertEquals(1, tripDrain.successCount)
            assertTrue(outbox.pending().isEmpty())

            val syncedTrip = trips.all().single()
            assertTrue(syncedTrip.id.matches(UUID_RE), syncedTrip.id)
            val remoteTrip = TripApi(client, baseUrl).fetchTrip(syncedTrip.id)
            assertEquals("Lisbon", remoteTrip.city)
            assertEquals("PT", remoteTrip.countryCode)

            val localPurchase = CreatePurchaseUseCase(purchaseRepo)(
                tripId = syncedTrip.id,
                draft = PurchaseDraft(
                    name = "Pastel de nata",
                    category = PurchaseCategory.FOOD,
                    amount = Money.parse("1.20", "EUR"),
                    vatIncluded = true,
                    vatRatePercent = "23",
                    place = "Belem",
                    purchaseDate = "2026-08-12",
                    purchaseTime = "10:24:00",
                ),
            ).getOrThrow()
            assertTrue(localPurchase.id.startsWith("local-buy-"))
            assertEquals(1, outbox.pending().size)

            val purchaseDrain = drain().getOrThrow()
            assertEquals(0, purchaseDrain.failureCount, "purchase drain failed, pending=${outbox.pending()}")
            assertEquals(1, purchaseDrain.successCount)
            assertTrue(outbox.pending().isEmpty())

            val syncedPurchase = purchases.observeByTrip(syncedTrip.id).first().single()
            assertTrue(syncedPurchase.id.matches(UUID_RE), syncedPurchase.id)
            assertEquals(false, syncedPurchase.pendingSync)
            val remotePurchase = PurchaseApi(client, baseUrl).fetchPurchase(syncedTrip.id, syncedPurchase.id)
            assertEquals("Pastel de nata", remotePurchase.name)
            assertEquals("1.20", remotePurchase.amount.amount)
        } finally {
            client.close()
        }
    }

    private suspend fun ping(baseUrl: String): Boolean {
        val probe = HttpClient(createPlatformHttpEngine())
        return try {
            probe.get("${baseUrl.trimEnd('/')}/_ping").status.isSuccess()
        } catch (_: Exception) {
            false
        } finally {
            probe.close()
        }
    }

    private class MemoryTokenStore : TokenStore {
        private var access: String? = null
        private var refresh: String? = null
        override fun accessToken(): String? = access
        override fun refreshToken(): String? = refresh
        override fun saveTokens(accessToken: String, refreshToken: String) {
            access = accessToken
            refresh = refreshToken
        }
        override fun clear() {
            access = null
            refresh = null
        }
    }

    private companion object {
        val UUID_RE = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    }
}
