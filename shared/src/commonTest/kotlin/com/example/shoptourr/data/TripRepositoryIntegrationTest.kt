package com.example.shoptourr.data

import com.example.shoptourr.data.local.InMemoryTripLocalStore
import com.example.shoptourr.data.remote.HomeApi
import com.example.shoptourr.data.remote.TripApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.repository.TripRepositoryImpl
import com.example.shoptourr.data.sync.InMemorySyncOutbox
import com.example.shoptourr.domain.model.TripStatus as DomainTripStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.shoptourr.api.common.MoneyDto
import com.example.shoptourr.api.home.HomeResponse
import com.example.shoptourr.api.trip.TripStatus
import com.example.shoptourr.api.trip.TripSummaryDto
import com.example.shoptourr.api.user.ThemePreference
import com.example.shoptourr.api.user.UserDto
import com.example.shoptourr.api.user.UserStatsDto

class TripRepositoryIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    @Test
    fun `refreshHome caches trips and observeHome emits snapshot`() = runTest {
        val engine = MockEngine {
            val body = HomeResponse(
                user = UserDto(
                    id = "u1",
                    displayName = "Mila",
                    email = "mila@voyage.app",
                    locale = "ru",
                    preferredCurrency = "RUB",
                    theme = ThemePreference.DARK,
                    pushNotificationsEnabled = true,
                    memberSince = "2026-01-01T00:00:00Z",
                    stats = UserStatsDto(3, 3, 0),
                ),
                currentTrip = trip("lisbon", TripStatus.ACTIVE, "Lisbon"),
                upcoming = listOf(trip("oslo", TripStatus.UPCOMING, "Oslo")),
                archive = listOf(trip("tokyo", TripStatus.PAST, "Tokyo")),
                allTimeSpent = MoneyDto("2180.00", "EUR"),
                unreadAlertCount = 1,
            )
            respond(
                content = ByteReadChannel(json.encodeToString(body)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
        )
        val local = InMemoryTripLocalStore()
        val outbox = InMemorySyncOutbox()
        val repo = TripRepositoryImpl(
            homeApi = HomeApi(client, "https://api.test"),
            tripApi = TripApi(client, "https://api.test"),
            localStore = local,
            outbox = outbox,
            idGenerator = { "local-trip" },
            clock = { 1L },
        )

        repo.refreshTrips().getOrThrow()
        val snapshot = repo.observeHome().first()

        assertEquals("Lisbon", snapshot.currentTripCity)
        assertEquals(1, snapshot.upcomingCount)
        assertEquals(1, snapshot.archiveCount)
        assertEquals(DomainTripStatus.ACTIVE, local.all().first { it.city == "Lisbon" }.status)
    }

    private fun trip(id: String, status: TripStatus, city: String) = TripSummaryDto(
        id = id,
        city = city,
        country = "X",
        status = status,
        startDate = "2026-01-01",
        endDate = "2026-01-08",
        budget = MoneyDto("100.00", "EUR"),
        spent = MoneyDto("10.00", "EUR"),
        purchaseCount = 1,
        dayCount = 7,
    )
}
