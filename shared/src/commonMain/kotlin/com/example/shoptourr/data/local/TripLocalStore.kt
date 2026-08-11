package com.example.shoptourr.data.local

import com.example.shoptourr.data.remote.dto.common.ExchangeRateDto
import com.example.shoptourr.data.remote.dto.trip.TravelerDto
import com.example.shoptourr.domain.model.ExchangeRate
import com.example.shoptourr.domain.model.Traveler
import com.example.shoptourr.domain.model.TripSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TripLocalExtrasDto(
    val exchangeRate: ExchangeRateDto? = null,
    val travelers: List<TravelerDto> = emptyList(),
)

object TripLocalExtrasCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    fun encode(trip: TripSummary): String? {
        if (trip.exchangeRate == null && trip.travelers.isEmpty()) return null
        return json.encodeToString(
            TripLocalExtrasDto(
                exchangeRate = trip.exchangeRate?.let {
                    ExchangeRateDto(
                        tripCurrency = it.tripCurrency,
                        quoteCurrency = it.quoteCurrency,
                        rate = it.rate,
                        rateDate = it.rateDate,
                        provider = it.provider,
                    )
                },
                travelers = trip.travelers.map {
                    TravelerDto(
                        id = it.id,
                        name = it.name,
                        colorHex = it.colorHex,
                        avatarGlyph = it.avatarGlyph,
                        isOwner = it.isOwner,
                    )
                },
            ),
        )
    }

    fun decode(raw: String?): Pair<ExchangeRate?, List<Traveler>> {
        if (raw.isNullOrBlank()) return null to emptyList()
        val extras = runCatching { json.decodeFromString<TripLocalExtrasDto>(raw) }.getOrNull()
            ?: return null to emptyList()
        val rate = extras.exchangeRate?.let {
            ExchangeRate(
                tripCurrency = it.tripCurrency,
                quoteCurrency = it.quoteCurrency,
                rate = it.rate,
                rateDate = it.rateDate,
                provider = it.provider,
            )
        }
        val travelers = extras.travelers.map {
            Traveler(
                id = it.id,
                name = it.name,
                colorHex = it.colorHex,
                avatarGlyph = it.avatarGlyph,
                isOwner = it.isOwner,
            )
        }
        return rate to travelers
    }
}

interface TripLocalStore {
    suspend fun replaceAll(trips: List<TripSummary>)
    suspend fun upsert(trip: TripSummary)
    suspend fun remove(tripId: String)
    fun observeAll(): Flow<List<TripSummary>>
    fun all(): List<TripSummary>
}

class InMemoryTripLocalStore : TripLocalStore {
    private val trips = MutableStateFlow<List<TripSummary>>(emptyList())

    override suspend fun replaceAll(trips: List<TripSummary>) {
        this.trips.value = trips
    }

    override suspend fun upsert(trip: TripSummary) {
        val without = trips.value.filterNot { it.id == trip.id }
        trips.value = without + trip
    }

    override suspend fun remove(tripId: String) {
        trips.value = trips.value.filterNot { it.id == tripId }
    }

    override fun observeAll(): Flow<List<TripSummary>> = trips.asStateFlow()

    override fun all(): List<TripSummary> = trips.value
}
