package com.onebrain.game.score;

import java.io.Serializable;

/** Immutable grading of a single puzzle attempt. */
public final class ScoreResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean correct;
    private final int score;
    private final int stars;
    private final int coins;
    private final long timeMs;
    private final int mistakes;
    private final int hintsUsed;

    public ScoreResult(boolean correct, int score, int stars, int coins, long timeMs, int mistakes, int hintsUsed) {
        this.correct = correct;
        this.score = score;
        this.stars = stars;
        this.coins = coins;
        this.timeMs = timeMs;
        this.mistakes = mistakes;
        this.hintsUsed = hintsUsed;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getScore() {
        return score;
    }

    public int getStars() {
        return stars;
    }

    public int getCoins() {
        return coins;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public int getMistakes() {
        return mistakes;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }
}
