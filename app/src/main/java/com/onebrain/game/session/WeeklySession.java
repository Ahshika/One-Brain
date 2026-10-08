package com.onebrain.game.session;

import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.WeeklyChallengeManager;
import com.onebrain.game.player.Player;
import com.onebrain.game.puzzle.Puzzle;

/** Seven escalating puzzles shared by all players for the current week. */
public final class WeeklySession extends GameSession {

    private final WeeklyChallengeManager weekly;
    private final int weekKey;

    public WeeklySession(WeeklyChallengeManager weekly, int weekKey, SessionProgress restored) {
        super(GameMode.WEEKLY, weekKey, restored);
        this.weekly = weekly;
        this.weekKey = weekKey;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        return weekly.puzzleFor(weekKey, index);
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return 2;
    }

    @Override
    public int getTotalPuzzles() {
        return WeeklyChallengeManager.PUZZLES;
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        boolean finishedAll = getProgress().getIndex() >= getTotalPuzzles();
        player.getStats().recordWeekly(weekKey, getProgress().getScore(), finishedAll);
    }
}
