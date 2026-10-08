package com.onebrain.game.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.DailyPuzzleManager;
import com.onebrain.game.core.DateProvider;
import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.DifficultyManager;
import com.onebrain.game.core.PackManager;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.core.PuzzlePack;
import com.onebrain.game.core.WeeklyChallengeManager;
import com.onebrain.game.generator.Fingerprint;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.Player;
import com.onebrain.game.score.ScoreResult;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SessionTest {

    private final PuzzleFactory factory = new PuzzleFactory();
    private final DateProvider dates = new DateProvider() {
        @Override
        public LocalDate today() {
            return LocalDate.of(2026, 10, 8);
        }

        @Override
        public ZonedDateTime now() {
            return today().atStartOfDay(ZoneId.of("UTC"));
        }
    };

    private static ScoreResult ok() {
        return new ScoreResult(true, 100, 3, 6, 4000, 0, 0);
    }

    private static ScoreResult fail() {
        return new ScoreResult(false, 0, 0, 0, 4000, 1, 0);
    }

    @Test
    public void endlessEndsAfterThreeMissesAndRampsDifficulty() {
        EndlessSession s = new EndlessSession(factory, 99L, null);
        assertEquals(Difficulty.EASY, s.getCurrentPuzzle().getDifficulty());
        for (int i = 0; i < 4; i++) {
            s.onPuzzleCompleted(ok());
        }
        assertEquals(Difficulty.MEDIUM, s.getCurrentPuzzle().getDifficulty());
        s.onPuzzleCompleted(fail());
        s.onPuzzleCompleted(fail());
        assertFalse(s.isOver());
        assertEquals(1, s.getLivesLeft());
        s.onPuzzleCompleted(fail());
        assertTrue(s.isOver());

        Player player = new Player();
        SessionResult r = s.complete(player).build();
        assertEquals(4, r.getSolved());
        assertEquals(4, player.getStats().getBestEndlessRun());
    }

    @Test
    public void timeAttackRunsOutAndPenalizesMistakes() {
        TimeAttackSession s = new TimeAttackSession(factory, 5L, null);
        assertEquals(TimeAttackSession.DURATION_MS, s.getTimeRemaining(0));
        s.onWrongAttempt();
        assertEquals(TimeAttackSession.DURATION_MS - TimeAttackSession.PENALTY_MS, s.getTimeRemaining(0));
        for (int i = 0; i < 50; i++) {
            assertFalse(s.getCurrentPuzzle().getCategory() == PuzzleCategory.MEMORY);
            s.onPuzzleCompleted(ok());
        }
        assertTrue(s.isOver());
    }

    @Test
    public void practiceHasFixedLength() {
        PracticeSession s = new PracticeSession(factory, new DifficultyManager(), new Player().getStats(),
                PuzzleCategory.SEQUENCE, null, 1L, null);
        for (int i = 0; i < PracticeSession.LENGTH; i++) {
            assertFalse(s.isOver());
            assertEquals(PuzzleCategory.SEQUENCE, s.getCurrentPuzzle().getCategory());
            s.onPuzzleCompleted(ok());
        }
        assertTrue(s.isOver());
    }

    @Test
    public void sessionRestoresSamePuzzleFromSeedAndProgress() {
        EndlessSession a = new EndlessSession(factory, 1234L, null);
        a.onPuzzleCompleted(ok());
        String second = Fingerprint.of(a.getCurrentPuzzle());
        EndlessSession restored = new EndlessSession(factory, 1234L, a.getProgress());
        assertEquals(second, Fingerprint.of(restored.getCurrentPuzzle()));
    }

    @Test
    public void dailySessionUpdatesStreak() {
        DailyPuzzleManager daily = new DailyPuzzleManager(factory, dates);
        Player player = new Player();
        DailySession s = new DailySession(daily, daily.today(), null);
        s.onPuzzleCompleted(ok());
        assertTrue(s.isOver());
        SessionResult r = s.complete(player).build();
        assertEquals(1, r.getStreak());
        assertEquals(281, r.getDayNumber());
        assertTrue(player.getStats().hasPlayedDaily(daily.today()));
    }

    @Test
    public void weeklyIsSharedAndEscalates() {
        WeeklyChallengeManager weekly = new WeeklyChallengeManager(factory, dates);
        int week = weekly.currentWeek();
        assertEquals(Difficulty.EASY, weekly.puzzleFor(week, 0).getDifficulty());
        assertEquals(Difficulty.EXPERT, weekly.puzzleFor(week, 6).getDifficulty());
        assertEquals(Fingerprint.of(weekly.puzzleFor(week, 3)),
                Fingerprint.of(new WeeklyChallengeManager(new PuzzleFactory(), dates).puzzleFor(week, 3)));
        assertNotEquals(Fingerprint.of(weekly.puzzleFor(week, 3)), Fingerprint.of(weekly.puzzleFor(week + 1, 3)));
    }

    @Test
    public void solvingAPackLevelUnlocksTheNext() {
        PackManager packs = new PackManager();
        PuzzlePack starter = packs.find(PackManager.STARTER);
        Player player = new Player();
        PackSession s = new PackSession(factory, starter, 0, null);
        s.onPuzzleCompleted(ok());
        SessionResult r = s.complete(player).build();
        assertTrue(r.packHasNext());
        assertEquals(1, player.getInventory().getPackProgress(PackManager.STARTER));
        assertEquals(Difficulty.EASY, starter.difficultyForLevel(0));
        assertEquals(Difficulty.EXPERT, starter.difficultyForLevel(starter.getLevels() - 1));
    }
}
