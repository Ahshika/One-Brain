package com.onebrain.game.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;

import org.json.JSONObject;
import org.junit.Test;

public class PlayerStatsTest {

    @Test
    public void consecutiveDailySolvesBuildAStreak() {
        PlayerStats s = new PlayerStats();
        Inventory inv = new Inventory();
        for (long day = 100; day < 105; day++) {
            s.refreshStreak(day, inv);
            s.registerDaily(day, true, 3);
        }
        assertEquals(5, s.getCurrentStreak());
        assertEquals(5, s.getLongestStreak());
        assertEquals(5, s.getDailySolved());
    }

    @Test
    public void missingADayBreaksTheStreak() {
        PlayerStats s = new PlayerStats();
        Inventory inv = new Inventory();
        s.registerDaily(100, true, 3);
        s.registerDaily(101, true, 3);
        assertEquals(0, s.getDisplayStreak(103, inv));
        s.refreshStreak(103, inv);
        s.registerDaily(103, true, 2);
        assertEquals(1, s.getCurrentStreak());
        assertEquals(2, s.getLongestStreak());
    }

    @Test
    public void streakFreezeBridgesAMissedDay() {
        PlayerStats s = new PlayerStats();
        Inventory inv = new Inventory();
        inv.addStreakFreeze();
        s.registerDaily(100, true, 3);
        assertEquals(1, s.getDisplayStreak(102, inv));
        s.refreshStreak(102, inv);
        s.registerDaily(102, true, 3);
        assertEquals(2, s.getCurrentStreak());
        assertEquals(0, inv.getStreakFreezes());
    }

    @Test
    public void failingTheDailyResetsTheStreakAndCountsOnce() {
        PlayerStats s = new PlayerStats();
        s.registerDaily(100, true, 3);
        s.registerDaily(101, false, 0);
        s.registerDaily(101, true, 3); // replay attempt ignored
        assertEquals(0, s.getCurrentStreak());
        assertEquals(2, s.getDailyPlayed());
        assertEquals(Integer.valueOf(0), s.getDailyResult(101));
        assertTrue(s.hasPlayedDaily(101));
    }

    @Test
    public void flawlessRunResetsOnMistake() {
        PlayerStats s = new PlayerStats();
        for (int i = 0; i < 4; i++) {
            s.recordPuzzle(PuzzleCategory.MATH, Difficulty.EASY, true, 0, 5000, 3, 100);
        }
        s.recordPuzzle(PuzzleCategory.MATH, Difficulty.EASY, true, 1, 5000, 2, 70);
        assertEquals(0, s.getFlawlessRun());
        assertEquals(4, s.getBestFlawlessRun());
        assertEquals(5, s.getTotalSolved());
        assertEquals(5000, s.getAverageSolveTimeMs());
    }

    @Test
    public void jsonRoundTripPreservesEverything() throws Exception {
        Player p = new Player();
        PlayerStats s = p.getStats();
        s.recordPuzzle(PuzzleCategory.LOGIC, Difficulty.HARD, true, 0, 8000, 3, 300);
        s.recordPuzzle(PuzzleCategory.WORD, Difficulty.EASY, false, 2, 20000, 0, 0);
        s.registerDaily(500, true, 3);
        s.submitBestScore(GameMode.ENDLESS, 1234);
        s.saveDailyProgress(501, 4200, 1);
        p.getWallet().add(55);
        p.getInventory().setAdsRemoved(true);
        p.getInventory().addPack("math_master");
        p.getInventory().setPackProgress("starter", 3);

        Player copy = new Player();
        copy.fromJson(new JSONObject(p.toJson().toString()));

        assertEquals(p.toJson().toString(), copy.toJson().toString());
        assertEquals(1, copy.getStats().getCurrentStreak());
        assertEquals(1234, copy.getStats().getBestScore(GameMode.ENDLESS));
        assertEquals(4200, copy.getStats().getDailyProgressElapsed(501));
        assertEquals(Wallet.STARTING_COINS + 55, copy.getWallet().getCoins());
        assertTrue(copy.getInventory().isAdsRemoved());
        assertTrue(copy.getInventory().ownsPack("math_master"));
        assertEquals(3, copy.getInventory().getPackProgress("starter"));
        assertEquals(1, copy.getStats().getCategoryStats(PuzzleCategory.LOGIC).getSolved(Difficulty.HARD));
    }

    @Test
    public void resetKeepsPurchases() {
        Player p = new Player();
        p.getInventory().setPremium(true);
        p.getInventory().setPackProgress("starter", 5);
        p.getStats().recordPuzzle(PuzzleCategory.MATH, Difficulty.EASY, true, 0, 1000, 3, 100);
        p.resetProgress();
        assertTrue(p.getInventory().isPremium());
        assertEquals(0, p.getInventory().getPackProgress("starter"));
        assertEquals(0, p.getStats().getTotalSolved());
        assertFalse(p.getStats().hasPlayedDaily(1));
    }
}
