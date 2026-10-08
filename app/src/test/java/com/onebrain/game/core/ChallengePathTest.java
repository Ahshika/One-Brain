package com.onebrain.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.generator.Fingerprint;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.Player;
import com.onebrain.game.score.ScoreResult;
import com.onebrain.game.session.ChallengeSession;
import com.onebrain.game.session.SessionResult;

import org.junit.Test;

public class ChallengePathTest {

    private final ChallengePath path = new ChallengePath();
    private final PuzzleFactory factory = new PuzzleFactory();

    private static ScoreResult ok() {
        return new ScoreResult(true, 100, 3, 6, 4000, 0, 0);
    }

    private static ScoreResult fail() {
        return new ScoreResult(false, 0, 0, 0, 4000, 1, 0);
    }

    @Test
    public void levelsAreDeterministicForEveryone() {
        for (int i = 0; i < 150; i++) {
            ChallengeSession a = new ChallengeSession(factory, path, i, null);
            ChallengeSession b = new ChallengeSession(new PuzzleFactory(), new ChallengePath(), i, null);
            assertEquals(Fingerprint.of(a.getCurrentPuzzle()), Fingerprint.of(b.getCurrentPuzzle()));
        }
    }

    @Test
    public void challengeNeverGetsEasierStageToStage() {
        int prevPeak = -1;
        int prevPuzzles = 0;
        int prevAttempts = Integer.MAX_VALUE;
        for (int stage = 0; stage < ChallengePath.NAMED_STAGES; stage++) {
            ChallengeLevel mid = path.level(path.firstLevelOf(stage) + 5);
            assertTrue("peak difficulty", mid.getPeakDifficulty().ordinal() >= prevPeak);
            assertTrue("puzzle count", mid.getPuzzleCount() >= prevPuzzles);
            assertTrue("attempts", mid.getAttemptsPerPuzzle() <= prevAttempts);
            prevPeak = mid.getPeakDifficulty().ordinal();
            prevPuzzles = mid.getPuzzleCount();
            prevAttempts = mid.getAttemptsPerPuzzle();
        }
        assertEquals(Difficulty.EASY, path.level(0).getPeakDifficulty());
        assertEquals(Difficulty.MASTER, path.level(95).getPeakDifficulty());
    }

    @Test
    public void infinityStagesKeepShrinkingTheClock() {
        long previous = Long.MAX_VALUE;
        int standardStages = 0;
        for (int stage = ChallengePath.NAMED_STAGES; stage < 40; stage++) {
            if (path.theme(stage).getModifier() != ChallengePath.StageModifier.STANDARD) {
                continue;
            }
            long limit = path.level(path.firstLevelOf(stage)).getTimeLimitMillis();
            assertTrue(limit <= previous);
            previous = limit;
            standardStages++;
        }
        assertTrue(standardStages >= 3);
        assertTrue(path.level(5000).getTimeLimitMillis() >= 75_000L);
        assertFalse(path.level(150).isHintsAllowed());
    }

    @Test
    public void gameCreatesItsOwnVariedStagesAfterTheNamedOnes() {
        java.util.Set<ChallengePath.StageModifier> modifiers = java.util.EnumSet.noneOf(ChallengePath.StageModifier.class);
        int focused = 0;
        for (int stage = ChallengePath.NAMED_STAGES; stage < 60; stage++) {
            ChallengePath.StageTheme theme = path.theme(stage);
            // Same theme for everyone.
            assertEquals(theme.getModifier(), new ChallengePath().theme(stage).getModifier());
            assertEquals(theme.getFocus(), new ChallengePath().theme(stage).getFocus());
            modifiers.add(theme.getModifier());
            if (!theme.getFocus().isEmpty()) {
                focused++;
                for (int i = 0; i < ChallengePath.LEVELS_PER_STAGE; i++) {
                    ChallengeLevel level = path.level(path.firstLevelOf(stage) + i);
                    for (int j = 0; j < level.getPuzzleCount(); j++) {
                        assertTrue(theme.getFocus().contains(level.getCategory(j)));
                    }
                }
            }
            int expected = 5 + (theme.getModifier() == ChallengePath.StageModifier.MARATHON ? 1 : 0);
            assertEquals(expected, path.level(path.firstLevelOf(stage)).getPuzzleCount());
        }
        assertEquals(3, modifiers.size());
        assertTrue(focused > 10);
        assertTrue(path.theme(0).getFocus().isEmpty());
    }

    @Test
    public void bossLevelsAreLastInStageAndBigger() {
        ChallengeLevel boss = path.level(19);
        ChallengeLevel normal = path.level(18);
        assertTrue(boss.isBoss());
        assertFalse(normal.isBoss());
        assertEquals(normal.getPuzzleCount() + 1, boss.getPuzzleCount());
    }

    @Test
    public void clearingUnlocksNextAndPaysOnce() {
        Player player = new Player();
        int coins = player.getWallet().getCoins();
        ChallengeSession s = new ChallengeSession(factory, path, 0, null);
        while (!s.isOver()) {
            s.onPuzzleCompleted(ok());
        }
        SessionResult r = s.complete(player).build();
        assertTrue(r.isSuccess());
        assertEquals(1, player.getStats().getChallenge().getHighestUnlocked());
        assertEquals(3, player.getStats().getChallenge().getStars(0));
        assertTrue(r.getClearBonus() > 0);
        assertEquals(coins + r.getClearBonus(), player.getWallet().getCoins());

        ChallengeSession again = new ChallengeSession(factory, path, 0, null);
        while (!again.isOver()) {
            again.onPuzzleCompleted(ok());
        }
        assertEquals(0, again.complete(player).build().getClearBonus());
    }

    @Test
    public void oneFailedPuzzleFailsTheLevel() {
        Player player = new Player();
        ChallengeSession s = new ChallengeSession(factory, path, 30, null);
        s.onPuzzleCompleted(ok());
        assertFalse(s.isOver());
        s.onPuzzleCompleted(fail());
        assertTrue(s.isOver());
        SessionResult r = s.complete(player).build();
        assertFalse(r.isSuccess());
        assertEquals(0, player.getStats().getChallenge().getClearedCount());
    }

    @Test
    public void practiceUnlocksFollowPathProgress() {
        DifficultyManager dm = new DifficultyManager();
        assertTrue(dm.isUnlockedForPractice(Difficulty.HARD, false, 0));
        assertFalse(dm.isUnlockedForPractice(Difficulty.EXPERT, false, 10));
        assertTrue(dm.isUnlockedForPractice(Difficulty.EXPERT, false, DifficultyManager.EXPERT_UNLOCK_LEVEL));
        assertFalse(dm.isUnlockedForPractice(Difficulty.MASTER, false, DifficultyManager.EXPERT_UNLOCK_LEVEL));
        assertTrue(dm.isUnlockedForPractice(Difficulty.MASTER, true, 0));
    }
}
