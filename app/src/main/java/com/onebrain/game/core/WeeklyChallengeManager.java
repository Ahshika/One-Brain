package com.onebrain.game.core;

import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.util.SeedUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Weekly Challenge: 7 puzzles, identical for everyone during an ISO week,
 * escalating from Easy to Expert.
 */
public final class WeeklyChallengeManager {

    public static final int PUZZLES = 7;
    private static final long WEEKLY_SALT = 0x3EE1D1L;
    private static final Difficulty[] LADDER = {
            Difficulty.EASY, Difficulty.EASY, Difficulty.MEDIUM, Difficulty.MEDIUM,
            Difficulty.HARD, Difficulty.HARD, Difficulty.EXPERT
    };

    private final PuzzleFactory factory;
    private final DateProvider dates;

    public WeeklyChallengeManager(PuzzleFactory factory, DateProvider dates) {
        this.factory = factory;
        this.dates = dates;
    }

    public int currentWeek() {
        return DayClock.weekKey(dates.today());
    }

    public long millisUntilNextWeek() {
        return DayClock.millisUntilNextWeek(dates.now());
    }

    public Puzzle puzzleFor(int weekKey, int index) {
        List<PuzzleCategory> categories = new ArrayList<>(Arrays.asList(PuzzleCategory.values()));
        Collections.shuffle(categories, new Random(SeedUtil.mix(WEEKLY_SALT, weekKey)));
        PuzzleCategory category = categories.get(index % categories.size());
        Difficulty difficulty = LADDER[Math.min(index, LADDER.length - 1)];
        return factory.create(category, difficulty, SeedUtil.mix(SeedUtil.mix(WEEKLY_SALT, weekKey), index));
    }
}
