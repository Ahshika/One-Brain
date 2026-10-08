package com.onebrain.game.core;

import com.onebrain.game.player.CategoryStats;
import com.onebrain.game.player.PlayerStats;

/**
 * Adaptive difficulty. A player unlocks the next level in a category after
 * solving enough puzzles at the current level with good accuracy, so difficulty
 * rises gradually as skill grows.
 */
public final class DifficultyManager {

    /** Challenge Path level a free player must unlock to practice Expert / Master. */
    public static final int EXPERT_UNLOCK_LEVEL = 50;
    public static final int MASTER_UNLOCK_LEVEL = 80;

    /** Expert and Master practice unlock through Premium or Challenge Path progress. */
    public boolean isUnlockedForPractice(Difficulty d, boolean premium, int highestPathLevel) {
        if (premium || d.ordinal() < Difficulty.EXPERT.ordinal()) {
            return true;
        }
        int needed = d == Difficulty.EXPERT ? EXPERT_UNLOCK_LEVEL : MASTER_UNLOCK_LEVEL;
        return highestPathLevel >= needed;
    }

    static final int SOLVED_TO_ADVANCE = 4;
    static final float ACCURACY_TO_ADVANCE = 0.7f;

    public Difficulty recommend(CategoryStats stats) {
        Difficulty result = Difficulty.EASY;
        for (Difficulty d : Difficulty.values()) {
            if (d == Difficulty.EXPERT) {
                break;
            }
            if (stats.getSolved(d) >= SOLVED_TO_ADVANCE && stats.getAccuracy(d) >= ACCURACY_TO_ADVANCE) {
                result = d.next();
            } else {
                break;
            }
        }
        return result;
    }

    public Difficulty recommend(PlayerStats stats, PuzzleCategory category) {
        return recommend(stats.getCategoryStats(category));
    }
}
