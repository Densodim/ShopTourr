package com.shoptourr.domain;

import com.shoptourr.api.v1.dto.trip.TripDtos.TripStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class TripCalendar {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

    private TripCalendar() {}

    public static TripStatus resolve(LocalDate start, LocalDate end, LocalDate today, TripStatus stored) {
        if (stored == TripStatus.ARCHIVED) {
            return TripStatus.ARCHIVED;
        }
        if (today.isBefore(start)) {
            return TripStatus.UPCOMING;
        }
        if (today.isAfter(end)) {
            return TripStatus.PAST;
        }
        return TripStatus.ACTIVE;
    }

    public static int dayCount(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    public static Integer currentDayNumber(LocalDate start, LocalDate end, LocalDate today, TripStatus status) {
        if (status != TripStatus.ACTIVE) {
            return null;
        }
        int day = (int) ChronoUnit.DAYS.between(start, today) + 1;
        int max = dayCount(start, end);
        return Math.min(Math.max(day, 1), max);
    }

    public static String datesLabel(LocalDate start, LocalDate end) {
        if (start.getMonth() == end.getMonth() && start.getYear() == end.getYear()) {
            return start.getDayOfMonth() + "–" + end.getDayOfMonth() + " "
                    + MONTH.format(end).toUpperCase(Locale.ENGLISH);
        }
        return start.getDayOfMonth() + " " + MONTH.format(start).toUpperCase(Locale.ENGLISH)
                + " – " + end.getDayOfMonth() + " " + MONTH.format(end).toUpperCase(Locale.ENGLISH);
    }
}
