package com.onebrain.game.session;

import com.onebrain.game.core.DailyPuzzleManager;
import com.onebrain.game.core.DayClock;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.player.Player;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.puzzle.Puzzle;

/** Today's puzzle: one puzzle, three attempts, updates the streak. */
public final class DailySession extends GameSession {

    private final DailyPuzzleManager daily;
    private final long epochDay;

    public DailySession(DailyPuzzleManager daily, long epochDay, SessionProgress restored) {
        super(GameMode.DAILY, epochDay, restored);
        this.daily = daily;
        this.epochDay = epochDay;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        return daily.puzzleFor(epochDay);
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return 3;
    }

    @Override
    public int getTotalPuzzles() {
        return 1;
    }

    public long getEpochDay() {
        return epochDay;
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        PlayerStats stats = player.getStats();
        boolean solved = getProgress().getSolved() > 0;
        int stars = getProgress().getStarsSum();
        stats.refreshStreak(epochDay, player.getInventory());
        stats.registerDaily(epochDay, solved, stars);
        result.category(daily.categoryFor(epochDay))
                .difficulty(daily.difficultyFor(epochDay))
                .daily(DayClock.dayNumber(epochDay), stats.getCurrentStreak());
    }
}
