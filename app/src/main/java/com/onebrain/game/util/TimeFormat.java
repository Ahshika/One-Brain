package com.onebrain.game.util;

import java.util.Locale;

public final class TimeFormat {

    private TimeFormat() {
    }

    /** "0:42" or "12:05". */
    public static String clock(long millis) {
        long totalSeconds = Math.max(0L, millis) / 1000L;
        return String.format(Locale.getDefault(), "%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    /** "05:12:33" countdown. */
    public static String countdown(long millis) {
        long s = Math.max(0L, millis) / 1000L;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    /** "4.2s" for short durations, clock format otherwise. */
    public static String precise(long millis) {
        if (millis < 60_000L) {
            return String.format(Locale.getDefault(), "%.1fs", millis / 1000f);
        }
        return clock(millis);
    }
}
