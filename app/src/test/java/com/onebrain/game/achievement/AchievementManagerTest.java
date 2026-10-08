package com.onebrain.game.achievement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.player.Wallet;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchievementManagerTest {

    @Test
    public void unlocksOnceAndPaysReward() {
        AchievementManager m = new AchievementManager(Collections.emptySet());
        PlayerStats stats = new PlayerStats();
        Wallet wallet = new Wallet();
        stats.recordPuzzle(PuzzleCategory.MATH, Difficulty.EASY, true, 0, 8_000, 3, 100);

        List<Achievement> unlocked = m.evaluate(stats, wallet);
        Set<String> ids = new HashSet<>();
        for (Achievement a : unlocked) {
            ids.add(a.getId());
        }
        assertTrue(ids.contains("first_solve"));
        assertTrue(ids.contains("speed_10"));
        assertEquals(Wallet.STARTING_COINS + 20 + 100, wallet.getCoins());
        assertTrue(m.evaluate(stats, wallet).isEmpty());
    }

    @Test
    public void requiredAchievementsExist() {
        AchievementManager m = new AchievementManager(null);
        for (String id : new String[]{"daily_7", "solved_30", "flawless_10", "speed_10"}) {
            assertTrue(id, m.find(id) != null);
        }
        assertEquals(7, m.find("daily_7").getTarget());
        assertEquals(30, m.find("solved_30").getTarget());
        assertEquals(10, m.find("flawless_10").getTarget());
    }

    @Test
    public void flawlessTenRequiresConsecutiveCleanSolves() {
        AchievementManager m = new AchievementManager(null);
        PlayerStats stats = new PlayerStats();
        Wallet wallet = new Wallet();
        for (int i = 0; i < 9; i++) {
            stats.recordPuzzle(PuzzleCategory.LOGIC, Difficulty.EASY, true, 0, 20_000, 3, 100);
        }
        stats.recordPuzzle(PuzzleCategory.LOGIC, Difficulty.EASY, true, 1, 20_000, 2, 70);
        m.evaluate(stats, wallet);
        assertTrue(!m.isUnlocked("flawless_10"));
        for (int i = 0; i < 10; i++) {
            stats.recordPuzzle(PuzzleCategory.LOGIC, Difficulty.EASY, true, 0, 20_000, 3, 100);
        }
        m.evaluate(stats, wallet);
        assertTrue(m.isUnlocked("flawless_10"));
    }
}
