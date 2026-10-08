package com.onebrain.game.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;

import org.junit.Test;

public class ScoreManagerTest {

    private final ScoreManager scores = new ScoreManager();

    @Test
    public void fastCleanSolveEarnsThreeStars() {
        ScoreResult r = scores.evaluate(Difficulty.MEDIUM, 45, 0, true, 0, 10_000, GameMode.PRACTICE);
        assertEquals(3, r.getStars());
        assertTrue(r.getScore() > 100);
    }

    @Test
    public void hintsAndMistakesLowerScoreAndStars() {
        ScoreResult clean = scores.evaluate(Difficulty.HARD, 60, 0, true, 0, 30_000, GameMode.PRACTICE);
        ScoreResult messy = scores.evaluate(Difficulty.HARD, 60, 2, true, 2, 30_000, GameMode.PRACTICE);
        assertTrue(messy.getScore() < clean.getScore());
        assertEquals(1, messy.getStars());
    }

    @Test
    public void slowerIsWorseButNeverZero() {
        ScoreResult fast = scores.evaluate(Difficulty.EASY, 30, 0, true, 0, 5_000, GameMode.PRACTICE);
        ScoreResult slow = scores.evaluate(Difficulty.EASY, 30, 0, true, 0, 600_000, GameMode.PRACTICE);
        assertTrue(fast.getScore() > slow.getScore());
        assertTrue(slow.getScore() > 0);
    }

    @Test
    public void wrongAnswerScoresNothing() {
        ScoreResult r = scores.evaluate(Difficulty.EXPERT, 90, 0, false, 3, 20_000, GameMode.DAILY);
        assertEquals(0, r.getScore());
        assertEquals(0, r.getStars());
        assertEquals(0, r.getCoins());
    }

    @Test
    public void dailyPaysMoreCoins() {
        ScoreResult daily = scores.evaluate(Difficulty.EASY, 30, 0, true, 0, 5_000, GameMode.DAILY);
        ScoreResult practice = scores.evaluate(Difficulty.EASY, 30, 0, true, 0, 5_000, GameMode.PRACTICE);
        assertEquals(practice.getCoins() * 3, daily.getCoins());
    }
}
