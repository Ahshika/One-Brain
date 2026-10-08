package com.onebrain.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.onebrain.game.generator.Fingerprint;
import com.onebrain.game.generator.PuzzleFactory;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;

public class DailyPuzzleManagerTest {

    private static DateProvider fixed(LocalDate date) {
        return new DateProvider() {
            @Override
            public LocalDate today() {
                return date;
            }

            @Override
            public ZonedDateTime now() {
                return date.atTime(12, 0).atZone(ZoneId.of("UTC"));
            }
        };
    }

    @Test
    public void samePuzzleForEveryPlayerOnTheSameDay() {
        LocalDate day = LocalDate.of(2026, 10, 8);
        // Two independent "devices".
        DailyPuzzleManager a = new DailyPuzzleManager(new PuzzleFactory(), fixed(day));
        DailyPuzzleManager b = new DailyPuzzleManager(new PuzzleFactory(), fixed(day));
        assertEquals(Fingerprint.of(a.puzzleFor(a.today())), Fingerprint.of(b.puzzleFor(b.today())));
    }

    @Test
    public void differentDaysGiveDifferentPuzzles() {
        DailyPuzzleManager m = new DailyPuzzleManager(new PuzzleFactory(), fixed(LocalDate.of(2026, 10, 8)));
        long today = m.today();
        assertNotEquals(Fingerprint.of(m.puzzleFor(today)), Fingerprint.of(m.puzzleFor(today + 1)));
    }

    @Test
    public void everyCategoryAppearsOnceInEachEightDayBlock() {
        DailyPuzzleManager m = new DailyPuzzleManager(new PuzzleFactory(), fixed(LocalDate.of(2026, 1, 1)));
        for (long block = 2000; block < 2100; block++) {
            Set<PuzzleCategory> seen = EnumSet.noneOf(PuzzleCategory.class);
            for (int i = 0; i < 8; i++) {
                seen.add(m.categoryFor(block * 8 + i));
            }
            assertEquals(8, seen.size());
        }
    }

    @Test
    public void difficultyRampsThroughTheWeek() {
        DailyPuzzleManager m = new DailyPuzzleManager(new PuzzleFactory(), fixed(LocalDate.of(2026, 10, 5)));
        LocalDate monday = LocalDate.of(2026, 10, 5);
        assertEquals(Difficulty.EASY, m.difficultyFor(monday.toEpochDay()));
        assertEquals(Difficulty.MEDIUM, m.difficultyFor(monday.plusDays(2).toEpochDay()));
        assertEquals(Difficulty.HARD, m.difficultyFor(monday.plusDays(4).toEpochDay()));
        assertEquals(Difficulty.EXPERT, m.difficultyFor(monday.plusDays(6).toEpochDay()));
    }

    @Test
    public void dayNumbersStartAtOne() {
        assertEquals(1, DayClock.dayNumber(DayClock.LAUNCH_DATE.toEpochDay()));
        assertEquals(281, DayClock.dayNumber(LocalDate.of(2026, 10, 8).toEpochDay()));
    }
}
