package com.onebrain.game.session;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.Player;
import com.onebrain.game.puzzle.Puzzle;

import java.util.Random;

/** Random puzzles until three are missed; difficulty climbs every 4 solves. */
public final class EndlessSession extends GameSession {

    static final int LIVES = 3;
    static final int SOLVES_PER_LEVEL = 4;

    private final PuzzleFactory factory;

    public EndlessSession(PuzzleFactory factory, long seed, SessionProgress restored) {
        super(GameMode.ENDLESS, seed, restored);
        this.factory = factory;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        Random r = new Random(seed);
        Difficulty d = Difficulty.ramp(getProgress().getSolved(), SOLVES_PER_LEVEL);
        return factory.create(factory.randomCategory(r, null), d, r.nextLong());
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return 1;
    }

    @Override
    public int getMaxLives() {
        return LIVES;
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        player.getStats().submitRun(GameMode.ENDLESS, getProgress().getSolved());
    }
}
