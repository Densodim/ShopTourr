package com.shoptourr.application;

import com.shoptourr.api.v1.dto.home.HomeDtos.HomeResponse;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripSummaryDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UserDto;
import com.shoptourr.infra.persistence.TripEntity;
import com.shoptourr.infra.persistence.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HomeService {

    private final UserService users;
    private final TripService trips;
    private final TripRepository tripRepository;
    private final TripMapper mapper;

    public HomeService(UserService users, TripService trips, TripRepository tripRepository, TripMapper mapper) {
        this.users = users;
        this.trips = trips;
        this.tripRepository = tripRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public HomeResponse home(UUID userId) {
        UserDto user = users.me(userId);
        List<TripSummaryDto> upcoming = new ArrayList<>();
        List<TripSummaryDto> archive = new ArrayList<>();
        TripSummaryDto current = null;
        BigDecimal allTime = BigDecimal.ZERO;
        String quote = user.preferredCurrency();
        for (TripEntity trip : tripRepository.findByUserIdAndDeletedAtIsNullOrderByStartDateDesc(userId)) {
            TripSummaryDto summary = trips.toSummary(trip);
            BigDecimal spentInQuote = trip.getFxRate() == null
                    ? summary.spent().amount()
                    : summary.spent().amount().multiply(trip.getFxRate());
            allTime = allTime.add(spentInQuote);
            switch (summary.status()) {
                case ACTIVE -> {
                    if (current == null) {
                        current = summary;
                    }
                }
                case UPCOMING -> upcoming.add(summary);
                case PAST, ARCHIVED -> archive.add(summary);
            }
        }
        return new HomeResponse(
                user,
                current,
                upcoming,
                archive,
                mapper.money(allTime, quote),
                0
        );
    }
}
