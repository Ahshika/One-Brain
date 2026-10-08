package com.onebrain.game.score;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.puzzle.Puzzle;

/**
 * Grades an attempt. Score = base(difficulty) × speed × hint penalty × mistake
 * penalty. Stars reward clean, fast solves; coins scale with stars and difficulty.
 */
public final class ScoreManager {

    static final int BASE_POINTS = 100;

    public ScoreResult evaluate(Puzzle puzzle, boolean correct, int mistakes, long timeMs, GameMode mode) {
        return evaluate(puzzle.getDifficulty(), puzzle.getParSeconds(), puzzle.getHintsUsed(),
                correct, mistakes, timeMs, mode);
    }

    public ScoreResult evaluate(Difficulty difficulty, int parSeconds, int hints, boolean correct,
                                int mistakes, long timeMs, GameMode mode) {
        if (!correct) {
            return new ScoreResult(false, 0, 0, 0, timeMs, mistakes, hints);
        }
        double ratio = timeMs / (Math.max(1, parSeconds) * 1000.0);
        double speed;
        if (ratio <= 0.5) {
            speed = 1.5;
        } else if (ratio <= 1.0) {
            speed = 1.5 - (ratio - 0.5);
        } else if (ratio <= 3.0) {
            speed = 1.0 - (ratio - 1.0) * 0.25;
        } else {
            speed = 0.5;
        }
        double hintFactor = Math.max(0.25, 1.0 - 0.25 * hints);
        double mistakeFactor = Math.max(0.3, 1.0 - 0.3 * mistakes);
        int score = (int) Math.round(BASE_POINTS * difficulty.getScoreMultiplier() * speed * hintFactor * mistakeFactor);

        int stars;
        if (mistakes == 0 && hints == 0 && ratio <= 1.0) {
            stars = 3;
        } else if (mistakes <= 1 && hints <= 1 && ratio <= 2.0) {
            stars = 2;
        } else {
            stars = 1;
        }

        int coins = stars * 2 * difficulty.getLevel();
        if (mode == GameMode.DAILY) {
            coins *= 3;
        } else if (mode == GameMode.WEEKLY) {
            coins *= 2;
        }
        return new ScoreResult(true, score, stars, coins, timeMs, mistakes, hints);
    }
}
