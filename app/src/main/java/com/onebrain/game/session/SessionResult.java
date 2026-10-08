package com.onebrain.game.session;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Final, immutable summary of a session shown on the results screen. */
public final class SessionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final GameMode mode;
    private final PuzzleCategory category;
    private final Difficulty difficulty;
    private final boolean success;
    private final int played;
    private final int solved;
    private final int score;
    private final long timeMs;
    private final int stars;
    private final int coins;
    private final boolean newBest;
    private final int bestScore;
    private final int dayNumber;
    private final int streak;
    private final String packId;
    private final int packLevel;
    private final boolean packHasNext;
    private final int challengeLevel;
    private final int challengeStage;
    private final boolean stageCleared;
    private final int clearBonus;
    private final ArrayList<String> achievementIds;

    private SessionResult(Builder b) {
        mode = b.mode;
        category = b.category;
        difficulty = b.difficulty;
        success = b.success;
        played = b.played;
        solved = b.solved;
        score = b.score;
        timeMs = b.timeMs;
        stars = b.stars;
        coins = b.coins;
        newBest = b.newBest;
        bestScore = b.bestScore;
        dayNumber = b.dayNumber;
        streak = b.streak;
        packId = b.packId;
        packLevel = b.packLevel;
        packHasNext = b.packHasNext;
        challengeLevel = b.challengeLevel;
        challengeStage = b.challengeStage;
        stageCleared = b.stageCleared;
        clearBonus = b.clearBonus;
        achievementIds = new ArrayList<>(b.achievementIds);
    }

    /** 0-based Challenge Path level, or -1. */
    public int getChallengeLevel() {
        return challengeLevel;
    }

    public int getChallengeStage() {
        return challengeStage;
    }

    /** True when this clear finished a stage (boss level) for the first time. */
    public boolean isStageCleared() {
        return stageCleared;
    }

    /** First-clear coin reward included in {@link #getCoins()}. */
    public int getClearBonus() {
        return clearBonus;
    }

    public GameMode getMode() {
        return mode;
    }

    /** Category of a single-puzzle session (daily/pack), else {@code null}. */
    public PuzzleCategory getCategory() {
        return category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getPlayed() {
        return played;
    }

    public int getSolved() {
        return solved;
    }

    public int getScore() {
        return score;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public int getStars() {
        return stars;
    }

    public int getCoins() {
        return coins;
    }

    public boolean isNewBest() {
        return newBest;
    }

    public int getBestScore() {
        return bestScore;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public int getStreak() {
        return streak;
    }

    public String getPackId() {
        return packId;
    }

    public int getPackLevel() {
        return packLevel;
    }

    public boolean packHasNext() {
        return packHasNext;
    }

    public List<String> getAchievementIds() {
        return Collections.unmodifiableList(achievementIds);
    }

    public int getAccuracyPercent() {
        return played == 0 ? 0 : Math.round(solved * 100f / played);
    }

    public static final class Builder {
        private final GameMode mode;
        private PuzzleCategory category;
        private Difficulty difficulty;
        private boolean success;
        private int played;
        private int solved;
        private int score;
        private long timeMs;
        private int stars;
        private int coins;
        private boolean newBest;
        private int bestScore;
        private int dayNumber;
        private int streak;
        private String packId;
        private int packLevel;
        private boolean packHasNext;
        private int challengeLevel = -1;
        private int challengeStage;
        private boolean stageCleared;
        private int clearBonus;
        private final List<String> achievementIds = new ArrayList<>();

        public Builder(GameMode mode) {
            this.mode = mode;
        }

        public Builder category(PuzzleCategory c) {
            category = c;
            return this;
        }

        public Builder difficulty(Difficulty d) {
            difficulty = d;
            return this;
        }

        public Builder success(boolean v) {
            success = v;
            return this;
        }

        public Builder counts(int playedCount, int solvedCount) {
            played = playedCount;
            solved = solvedCount;
            return this;
        }

        public Builder score(int v) {
            score = v;
            return this;
        }

        public Builder time(long v) {
            timeMs = v;
            return this;
        }

        public Builder stars(int v) {
            stars = v;
            return this;
        }

        public Builder coins(int v) {
            coins = v;
            return this;
        }

        public Builder best(boolean isNewBest, int best) {
            newBest = isNewBest;
            bestScore = best;
            return this;
        }

        public Builder daily(int number, int currentStreak) {
            dayNumber = number;
            streak = currentStreak;
            return this;
        }

        public Builder pack(String id, int level, boolean hasNext) {
            packId = id;
            packLevel = level;
            packHasNext = hasNext;
            return this;
        }

        public Builder challenge(int level, int stage, boolean passed, boolean clearedStage, int bonus) {
            challengeLevel = level;
            challengeStage = stage;
            success = passed;
            stageCleared = clearedStage;
            clearBonus = bonus;
            return this;
        }

        public Builder addAchievement(String id) {
            achievementIds.add(id);
            return this;
        }

        public SessionResult build() {
            return new SessionResult(this);
        }
    }
}
