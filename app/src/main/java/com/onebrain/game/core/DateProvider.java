package com.onebrain.game.core;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/** Abstraction over "now" so day-based logic (daily puzzle, streaks) is testable. */
public interface DateProvider {

    LocalDate today();

    ZonedDateTime now();
}
