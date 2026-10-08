package com.onebrain.game.session;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.DifficultyManager;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.Player;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.puzzle.Puzzle;

import java.util.Random;

/**
 * Relaxed training set of 5 puzzles. With no fixed difficulty, each puzzle
 * adapts to the player's current level in its category.
 */
public final class PracticeSession extends GameSession {

    public static final int LENGTH = 5;

    private final PuzzleFactory factory;
    private final DifficultyManager difficultyManager;
    private final PlayerStats stats;
    private final PuzzleCategory category;
    private final Difficulty difficulty;

    public PracticeSession(PuzzleFactory factory, DifficultyManager difficultyManager, PlayerStats stats,
                           PuzzleCategory category, Difficulty difficulty, long seed, SessionProgress restored) {
        super(GameMode.PRACTICE, seed, restored);
        this.factory = factory;
        this.difficultyManager = difficultyManager;
        this.stats = stats;
        this.category = category;
        this.difficulty = difficulty;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        Random r = new Random(seed);
        PuzzleCategory c = category != null ? category : factory.randomCategory(r, null);
        Difficulty d = difficulty != null ? difficulty : difficultyManager.recommend(stats, c);
        return factory.create(c, d, r.nextLong());
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return 2;
    }

    @Override
    public int getTotalPuzzles() {
        return LENGTH;
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        result.category(category).difficulty(difficulty);
    }
}
