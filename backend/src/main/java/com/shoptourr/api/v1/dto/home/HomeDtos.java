package com.shoptourr.api.v1.dto.home;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripSummaryDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UserDto;

import java.util.List;

/**
 * Home screen aggregate — one round-trip for first paint.
 * Prefer this over N separate calls on cold start.
 */
public final class HomeDtos {

    private HomeDtos() {}

    public record HomeResponse(
            UserDto user,
            TripSummaryDto currentTrip,
            List<TripSummaryDto> upcoming,
            List<TripSummaryDto> archive,
            /** Sum of spent across active + past (in preferredCurrency). */
            MoneyDto allTimeSpent,
            int unreadAlertCount
    ) {}
}
