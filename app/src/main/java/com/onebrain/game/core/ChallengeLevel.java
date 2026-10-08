package com.onebrain.game.core;

/**
 * One level of the Challenge Path: a short run of puzzles played under the
 * rules of its stage (attempts, time limit, hints). Immutable and generated
 * from the level index, so every player gets the same level N.
 */
public final class ChallengeLevel {

    private final int index;
    private final int stage;
    private final boolean boss;
    private final PuzzleCategory[] categories;
    private final Difficulty[] difficulties;
    private final int attemptsPerPuzzle;
    private final long timeLimitMillis;
    private final boolean hintsAllowed;
    private final long seed;

    ChallengeLevel(int index, int stage, boolean boss, PuzzleCategory[] categories, Difficulty[] difficulties,
                   int attemptsPerPuzzle, long timeLimitMillis, boolean hintsAllowed, long seed) {
        this.index = index;
        this.stage = stage;
        this.boss = boss;
        this.categories = categories.clone();
        this.difficulties = difficulties.clone();
        this.attemptsPerPuzzle = attemptsPerPuzzle;
        this.timeLimitMillis = timeLimitMillis;
        this.hintsAllowed = hintsAllowed;
        this.seed = seed;
    }

    /** 0-based level index along the whole path. */
    public int getIndex() {
        return index;
    }

    /** 1-based number shown to players. */
    public int getNumber() {
        return index + 1;
    }

    /** 0-based stage index. */
    public int getStage() {
        return stage;
    }

    /** Position of the level inside its stage (1..10). */
    public int getNumberInStage() {
        return index % ChallengePath.LEVELS_PER_STAGE + 1;
    }

    /** The last level of every stage is a tougher "boss" level. */
    public boolean isBoss() {
        return boss;
    }

    public int getPuzzleCount() {
        return categories.length;
    }

    public PuzzleCategory getCategory(int puzzle) {
        return categories[puzzle];
    }

    public Difficulty getDifficulty(int puzzle) {
        return difficulties[puzzle];
    }

    /** Hardest difficulty in the level (for display). */
    public Difficulty getPeakDifficulty() {
        Difficulty peak = Difficulty.EASY;
        for (Difficulty d : difficulties) {
            if (d.ordinal() > peak.ordinal()) {
                peak = d;
            }
        }
        return peak;
    }

    public int getAttemptsPerPuzzle() {
        return attemptsPerPuzzle;
    }

    /** 0 = untimed. */
    public long getTimeLimitMillis() {
        return timeLimitMillis;
    }

    public boolean isHintsAllowed() {
        return hintsAllowed;
    }

    public long getSeed() {
        return seed;
    }
}
