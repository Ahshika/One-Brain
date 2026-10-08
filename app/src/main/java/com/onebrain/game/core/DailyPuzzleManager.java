package com.onebrain.game.core;

import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.util.SeedUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Picks today's puzzle. Category, difficulty and seed derive only from the
 * calendar date, so every player gets the identical puzzle on the same day:
 * <ul>
 *   <li>Category: each block of 8 days contains every category once, shuffled.</li>
 *   <li>Difficulty: ramps through the week (Mon/Tue Easy … Sun Expert).</li>
 * </ul>
 */
public final class DailyPuzzleManager {

    private static final long DAILY_SALT = 0x0B12A1D0DA11L;

    private final PuzzleFactory factory;
    private final DateProvider dates;

    public DailyPuzzleManager(PuzzleFactory factory, DateProvider dates) {
        this.factory = factory;
        this.dates = dates;
    }

    public long today() {
        return dates.today().toEpochDay();
    }

    public int todayNumber() {
        return DayClock.dayNumber(today());
    }

    public long millisUntilNext() {
        return DayClock.millisUntilTomorrow(dates.now());
    }

    public PuzzleCategory categoryFor(long epochDay) {
        long block = Math.floorDiv(epochDay, 8L);
        List<PuzzleCategory> order = new ArrayList<>(Arrays.asList(PuzzleCategory.values()));
        Collections.shuffle(order, new Random(SeedUtil.mix(DAILY_SALT, block)));
        return order.get((int) Math.floorMod(epochDay, 8L));
    }

    public Difficulty difficultyFor(long epochDay) {
        switch (LocalDate.ofEpochDay(epochDay).getDayOfWeek()) {
            case MONDAY:
            case TUESDAY:
                return Difficulty.EASY;
            case WEDNESDAY:
            case THURSDAY:
                return Difficulty.MEDIUM;
            case FRIDAY:
            case SATURDAY:
                return Difficulty.HARD;
            default:
                return Difficulty.EXPERT;
        }
    }

    public long seedFor(long epochDay) {
        return SeedUtil.mix(DAILY_SALT, epochDay);
    }

    public Puzzle puzzleFor(long epochDay) {
        return factory.create(categoryFor(epochDay), difficultyFor(epochDay), seedFor(epochDay));
    }
}
