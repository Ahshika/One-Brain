package com.onebrain.game.core;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public final class SystemDateProvider implements DateProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now();
    }
}
