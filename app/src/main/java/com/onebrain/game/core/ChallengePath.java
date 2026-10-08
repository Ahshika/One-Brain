package com.onebrain.game.core;

import com.onebrain.game.util.SeedUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Challenge Path: an open, level-based campaign that never ends and keeps
 * getting harder. Levels are grouped in stages of 10; each stage tightens the
 * rules (harder puzzles, more puzzles per level, fewer attempts, a clock, no
 * hints). After the 10 named stages come endless "Infinity" stages where the
 * clock keeps shrinking.
 */
public final class ChallengePath {

    public static final int LEVELS_PER_STAGE = 10;
    public static final int NAMED_STAGES = 10;

    private static final long PATH_SALT = 0xC4A11E46EL;
    private static final long BOSS_EXTRA_TIME_MS = 30_000L;
    private static final long MIN_TIME_LIMIT_MS = 75_000L;

    /** Rules of one stage. */
    static final class StageRule {
        final Difficulty low;
        final Difficulty high;
        final int puzzles;
        final int attempts;
        final int timeLimitSeconds;
        final boolean hints;

        StageRule(Difficulty low, Difficulty high, int puzzles, int attempts, int timeLimitSeconds, boolean hints) {
            this.low = low;
            this.high = high;
            this.puzzles = puzzles;
            this.attempts = attempts;
            this.timeLimitSeconds = timeLimitSeconds;
            this.hints = hints;
        }
    }

    private static final StageRule[] STAGES = {
            //            low                high               puzzles attempts time hints
            new StageRule(Difficulty.EASY,   Difficulty.EASY,   1, 3, 0,   true),  // 1 Beginner
            new StageRule(Difficulty.EASY,   Difficulty.MEDIUM, 2, 3, 0,   true),  // 2 Apprentice
            new StageRule(Difficulty.MEDIUM, Difficulty.MEDIUM, 2, 2, 0,   true),  // 3 Thinker
            new StageRule(Difficulty.MEDIUM, Difficulty.HARD,   3, 2, 150, true),  // 4 Strategist
            new StageRule(Difficulty.HARD,   Difficulty.HARD,   3, 2, 130, true),  // 5 Challenger
            new StageRule(Difficulty.HARD,   Difficulty.EXPERT, 3, 2, 130, true),  // 6 Expert
            new StageRule(Difficulty.EXPERT, Difficulty.EXPERT, 3, 1, 120, true),  // 7 Mastermind
            new StageRule(Difficulty.EXPERT, Difficulty.MASTER, 4, 1, 150, false), // 8 Genius
            new StageRule(Difficulty.MASTER, Difficulty.MASTER, 4, 1, 140, false), // 9 Legend
            new StageRule(Difficulty.MASTER, Difficulty.MASTER, 5, 1, 160, false), // 10 One Brain
    };

    public int stageOf(int levelIndex) {
        return Math.max(0, levelIndex) / LEVELS_PER_STAGE;
    }

    public int firstLevelOf(int stage) {
        return stage * LEVELS_PER_STAGE;
    }

    public boolean isInfinityStage(int stage) {
        return stage >= NAMED_STAGES;
    }

    /** Special rule of a generated stage. */
    public enum StageModifier {
        STANDARD,
        /** 20% less time. */
        SPRINT,
        /** One extra puzzle per level, a bit more time. */
        MARATHON
    }

    /** Procedurally generated identity of a stage: focus categories and a modifier. */
    public static final class StageTheme {
        private final List<PuzzleCategory> focus;
        private final StageModifier modifier;

        StageTheme(List<PuzzleCategory> focus, StageModifier modifier) {
            this.focus = Collections.unmodifiableList(new ArrayList<>(focus));
            this.modifier = modifier;
        }

        /** Empty = all categories. */
        public List<PuzzleCategory> getFocus() {
            return focus;
        }

        public StageModifier getModifier() {
            return modifier;
        }
    }

    private static final long THEME_SALT = 0x57A6E7E3EL;
    private static final long MARATHON_EXTRA_TIME_MS = 40_000L;

    /**
     * The 10 named stages are hand-tuned and mixed. Every stage after that is
     * created by the game itself: a seeded theme picks focus categories (or a
     * mix) and a modifier, so each new stage plays differently while staying
     * identical for all players.
     */
    public StageTheme theme(int stage) {
        if (!isInfinityStage(stage)) {
            return new StageTheme(Collections.emptyList(), StageModifier.STANDARD);
        }
        Random r = new Random(SeedUtil.mix(THEME_SALT, stage));
        List<PuzzleCategory> categories = new ArrayList<>(Arrays.asList(PuzzleCategory.values()));
        Collections.shuffle(categories, r);
        List<PuzzleCategory> focus = r.nextInt(3) == 0
                ? Collections.<PuzzleCategory>emptyList() : categories.subList(0, 2);
        StageModifier modifier = StageModifier.values()[r.nextInt(StageModifier.values().length)];
        return new StageTheme(focus, modifier);
    }

    public ChallengeLevel level(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Level index must be >= 0");
        }
        int stage = stageOf(index);
        int position = index % LEVELS_PER_STAGE;
        boolean boss = position == LEVELS_PER_STAGE - 1;
        StageRule rule = STAGES[Math.min(stage, STAGES.length - 1)];
        StageTheme theme = theme(stage);
        long seed = SeedUtil.mix(PATH_SALT, index);
        Random r = new Random(seed);

        int count = rule.puzzles + (boss ? 1 : 0) + (theme.getModifier() == StageModifier.MARATHON ? 1 : 0);
        Difficulty[] difficulties = new Difficulty[count];
        for (int j = 0; j < count; j++) {
            // Later levels of a stage (and later puzzles of a level) lean towards the harder tier.
            boolean hard = boss || position + j >= 6;
            difficulties[j] = hard ? rule.high : rule.low;
        }

        List<PuzzleCategory> pool = new ArrayList<>(Arrays.asList(PuzzleCategory.values()));
        Collections.shuffle(pool, r);
        PuzzleCategory[] categories = new PuzzleCategory[count];
        List<PuzzleCategory> focus = theme.getFocus();
        int offset = r.nextInt(2);
        for (int j = 0; j < count; j++) {
            categories[j] = focus.isEmpty() ? pool.get(j % pool.size()) : focus.get((j + offset) % focus.size());
        }

        long limit = timeLimitMillis(rule, stage, position, boss);
        if (limit > 0 && theme.getModifier() == StageModifier.SPRINT) {
            limit = Math.max(MIN_TIME_LIMIT_MS, limit * 4 / 5);
        } else if (limit > 0 && theme.getModifier() == StageModifier.MARATHON) {
            limit += MARATHON_EXTRA_TIME_MS;
        }
        return new ChallengeLevel(index, stage, boss, categories, difficulties, rule.attempts, limit, rule.hints, seed);
    }

    private long timeLimitMillis(StageRule rule, int stage, int position, boolean boss) {
        if (rule.timeLimitSeconds == 0) {
            return 0L;
        }
        long base = rule.timeLimitSeconds * 1000L;
        if (isInfinityStage(stage)) {
            // Every infinity stage removes 5 seconds from the clock.
            base -= (stage - NAMED_STAGES + 1) * 5_000L;
        }
        base -= position * 2_000L; // the clock tightens through the stage
        if (boss) {
            base += BOSS_EXTRA_TIME_MS;
        }
        return Math.max(MIN_TIME_LIMIT_MS, base);
    }

    /** Coins awarded for clearing a level for the first time. */
    public int clearReward(ChallengeLevel level) {
        int base = 10 * (level.getStage() + 1);
        return level.isBoss() ? base * 5 : base;
    }
}
