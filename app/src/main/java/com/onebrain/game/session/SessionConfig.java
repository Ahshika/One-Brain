package com.onebrain.game.session;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.util.SeedUtil;

import java.io.Serializable;

/**
 * Everything needed to (re)create a session. It is small and Serializable so it
 * can travel in an Intent and survive process death via SavedStateHandle; since
 * puzzles are generated from seeds, the config alone rebuilds the same puzzles.
 */
public final class SessionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final GameMode mode;
    private final PuzzleCategory category;
    private final Difficulty difficulty;
    private final String packId;
    private final int packLevel;
    private final long seed;

    private SessionConfig(GameMode mode, PuzzleCategory category, Difficulty difficulty,
                          String packId, int packLevel, long seed) {
        this.mode = mode;
        this.category = category;
        this.difficulty = difficulty;
        this.packId = packId;
        this.packLevel = packLevel;
        this.seed = seed;
    }

    /** @param epochDay the calendar day of the puzzle (stored as the seed). */
    public static SessionConfig daily(long epochDay) {
        return new SessionConfig(GameMode.DAILY, null, null, null, 0, epochDay);
    }

    /** @param category {@code null} for mixed; @param difficulty {@code null} for adaptive. */
    public static SessionConfig practice(PuzzleCategory category, Difficulty difficulty) {
        return new SessionConfig(GameMode.PRACTICE, category, difficulty, null, 0, SeedUtil.randomSeed());
    }

    public static SessionConfig endless() {
        return new SessionConfig(GameMode.ENDLESS, null, null, null, 0, SeedUtil.randomSeed());
    }

    public static SessionConfig timeAttack() {
        return new SessionConfig(GameMode.TIME_ATTACK, null, null, null, 0, SeedUtil.randomSeed());
    }

    public static SessionConfig weekly(int weekKey) {
        return new SessionConfig(GameMode.WEEKLY, null, null, null, 0, weekKey);
    }

    public static SessionConfig pack(String packId, int level) {
        return new SessionConfig(GameMode.PACK, null, null, packId, level, 0L);
    }

    /** @param levelIndex 0-based Challenge Path level (stored in packLevel). */
    public static SessionConfig challenge(int levelIndex) {
        return new SessionConfig(GameMode.CHALLENGE, null, null, null, levelIndex, 0L);
    }

    /** Same settings, fresh puzzles (used by "Play again"). Daily/weekly/pack/challenge keep their seed. */
    public SessionConfig replay() {
        boolean fixedSeed = mode == GameMode.DAILY || mode == GameMode.WEEKLY || mode == GameMode.PACK
                || mode == GameMode.CHALLENGE;
        return new SessionConfig(mode, category, difficulty, packId, packLevel,
                fixedSeed ? seed : SeedUtil.randomSeed());
    }

    public GameMode getMode() {
        return mode;
    }

    public PuzzleCategory getCategory() {
        return category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getPackId() {
        return packId;
    }

    public int getPackLevel() {
        return packLevel;
    }

    public long getSeed() {
        return seed;
    }
}
