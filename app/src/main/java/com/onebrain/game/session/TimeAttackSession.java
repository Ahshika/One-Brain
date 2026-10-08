package com.onebrain.game.session;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.Player;
import com.onebrain.game.puzzle.Puzzle;

import java.util.Collections;
import java.util.Random;

/**
 * 90 seconds, as many puzzles as possible. Wrong answers cost 5 seconds.
 * Memory puzzles are excluded because their study phase would eat the clock.
 */
public final class TimeAttackSession extends GameSession {

    public static final long DURATION_MS = 90_000L;
    public static final long PENALTY_MS = 5_000L;
    static final int SOLVES_PER_LEVEL = 3;

    private final PuzzleFactory factory;

    public TimeAttackSession(PuzzleFactory factory, long seed, SessionProgress restored) {
        super(GameMode.TIME_ATTACK, seed, restored);
        this.factory = factory;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        Random r = new Random(seed);
        PuzzleCategory c = factory.randomCategory(r, Collections.singleton(PuzzleCategory.MEMORY));
        Difficulty d = Difficulty.ramp(getProgress().getSolved(), SOLVES_PER_LEVEL);
        return factory.create(c, d, r.nextLong());
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return 1;
    }

    @Override
    public long getTimeLimitMillis() {
        return DURATION_MS;
    }

    @Override
    public long getWrongPenaltyMillis() {
        return PENALTY_MS;
    }

    @Override
    public boolean allowsHints() {
        return false;
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        player.getStats().submitRun(GameMode.TIME_ATTACK, getProgress().getSolved());
    }
}
