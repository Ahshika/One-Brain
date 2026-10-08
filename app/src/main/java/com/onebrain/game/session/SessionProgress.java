package com.onebrain.game.session;

import com.onebrain.game.score.ScoreResult;

import java.io.Serializable;

/** Mutable running totals of a session; Serializable for SavedStateHandle. */
public final class SessionProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    private int index;
    private int solved;
    private int failed;
    private int mistakes;
    private int score;
    private int coins;
    private int starsSum;
    private int hints;
    private long timeMs;
    private long penaltyMs;

    void addPuzzle(ScoreResult result) {
        if (result.isCorrect()) {
            solved++;
        } else {
            failed++;
        }
        score += result.getScore();
        coins += result.getCoins();
        starsSum += result.getStars();
        hints += result.getHintsUsed();
        timeMs += result.getTimeMs();
        index++;
    }

    void addMistake(long penalty) {
        mistakes++;
        penaltyMs += penalty;
    }

    public int getIndex() {
        return index;
    }

    public int getSolved() {
        return solved;
    }

    public int getFailed() {
        return failed;
    }

    public int getPlayed() {
        return solved + failed;
    }

    public int getMistakes() {
        return mistakes;
    }

    public int getScore() {
        return score;
    }

    public int getCoins() {
        return coins;
    }

    public int getStarsSum() {
        return starsSum;
    }

    public int getHints() {
        return hints;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public long getPenaltyMs() {
        return penaltyMs;
    }
}
