package com.onebrain.game.core;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;

/**
 * Calendar helpers. Days are identified by their epoch day in the player's local
 * calendar, so everybody gets puzzle #N on the same calendar date (like Wordle).
 */
public final class DayClock {

    public static final LocalDate LAUNCH_DATE = LocalDate.of(2026, 1, 1);

    private DayClock() {
    }

    /** 1-based puzzle number shown to players ("One Brain #283"). */
    public static int dayNumber(long epochDay) {
        return (int) (epochDay - LAUNCH_DATE.toEpochDay()) + 1;
    }

    public static int weekKey(LocalDate date) {
        return date.get(IsoFields.WEEK_BASED_YEAR) * 100 + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public static long millisUntilTomorrow(ZonedDateTime now) {
        ZonedDateTime next = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Math.max(0L, Duration.between(now, next).toMillis());
    }

    public static long millisUntilNextWeek(ZonedDateTime now) {
        LocalDate nextMonday = now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        return Math.max(0L, Duration.between(now, nextMonday.atStartOfDay(now.getZone())).toMillis());
    }
}
