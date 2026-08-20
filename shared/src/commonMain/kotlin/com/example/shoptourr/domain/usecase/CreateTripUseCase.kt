package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateTripDraft
import com.example.shoptourr.domain.model.TripSummary
import com.example.shoptourr.domain.repository.TripRepository
import com.example.shoptourr.domain.validation.FieldRules

class CreateTripUseCase(
    private val tripRepository: TripRepository,
    private val drainSyncOutbox: DrainSyncOutboxUseCase? = null,
) {
    suspend operator fun invoke(draft: CreateTripDraft): Result<TripSummary> {
        val city = draft.city.trim()
        val country = draft.country.trim()
        if (!FieldRules.isPlaceName(city)) return Result.failure(AppError.Validation("city"))
        if (!FieldRules.isPlaceName(country)) return Result.failure(AppError.Validation("country"))
        if (!FieldRules.isIsoDate(draft.startDate)) return Result.failure(AppError.Validation("startDate"))
        if (!FieldRules.isIsoDate(draft.endDate)) return Result.failure(AppError.Validation("endDate"))
        if (draft.budget.minorUnits <= 0) return Result.failure(AppError.Validation("budget"))
        if (!FieldRules.isIso4217(draft.budget.currency)) {
            return Result.failure(AppError.Validation("budget"))
        }
        if (draft.endDate < draft.startDate) return Result.failure(AppError.Validation("dates"))
        val countryCode = draft.countryCode?.trim()?.takeIf { it.isNotEmpty() }
        if (countryCode != null && !FieldRules.isCountryCode(countryCode)) {
            return Result.failure(AppError.Validation("countryCode"))
        }
        val quoteCurrency = draft.quoteCurrency?.trim()?.takeIf { it.isNotEmpty() }
        if (quoteCurrency != null && !FieldRules.isSupportedCurrency(quoteCurrency)) {
            return Result.failure(AppError.Validation("quoteCurrency"))
        }
        draft.travelers.forEach { traveler ->
            if (!FieldRules.isTravelerName(traveler.name.trim())) {
                return Result.failure(AppError.Validation("travelers"))
            }
            if (!FieldRules.isHexColor(traveler.colorHex)) {
                return Result.failure(AppError.Validation("travelers"))
            }
        }
        return tripRepository.createTrip(
            draft.copy(
                city = city,
                country = country,
                countryCode = countryCode,
                quoteCurrency = quoteCurrency,
            )
        ).also { result ->
            if (result.isSuccess) {
                drainSyncOutbox?.invoke()
            }
        }
    }
}
