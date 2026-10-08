package com.onebrain.game.core;

/**
 * Difficulty ladder: Easy → Medium → Hard → Expert → Master. Each level carries
 * its own score multiplier and "par" time used to grade solving speed. Master is
 * only served by the Challenge Path's late stages (and unlocked practice).
 */
public enum Difficulty {
    EASY(1, 1.0f, 30),
    MEDIUM(2, 1.6f, 45),
    HARD(3, 2.4f, 60),
    EXPERT(4, 3.5f, 90),
    MASTER(5, 5.0f, 120);

    private final int level;
    private final float scoreMultiplier;
    private final int parSeconds;

    Difficulty(int level, float scoreMultiplier, int parSeconds) {
        this.level = level;
        this.scoreMultiplier = scoreMultiplier;
        this.parSeconds = parSeconds;
    }

    public int getLevel() {
        return level;
    }

    public float getScoreMultiplier() {
        return scoreMultiplier;
    }

    public int getParSeconds() {
        return parSeconds;
    }

    public Difficulty next() {
        return this == MASTER ? MASTER : values()[ordinal() + 1];
    }

    public static Difficulty fromIndex(int index) {
        Difficulty[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, index))];
    }

    /** Ramps difficulty up by one level every {@code perStep} correct answers. */
    public static Difficulty ramp(int correctSoFar, int perStep) {
        return fromIndex(correctSoFar / Math.max(1, perStep));
    }
}
