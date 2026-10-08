package com.onebrain.game.session;

import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzlePack;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.Player;
import com.onebrain.game.puzzle.Puzzle;

/** A single level of a puzzle pack; solving it unlocks the next level. */
public final class PackSession extends GameSession {

    private final PuzzleFactory factory;
    private final PuzzlePack pack;
    private final int level;

    public PackSession(PuzzleFactory factory, PuzzlePack pack, int level, SessionProgress restored) {
        super(GameMode.PACK, pack.seedForLevel(level), restored);
        this.factory = factory;
        this.pack = pack;
        this.level = level;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        return factory.create(pack.categoryForLevel(level), pack.difficultyForLevel(level), pack.seedForLevel(level));
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return 3;
    }

    @Override
    public int getTotalPuzzles() {
        return 1;
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        boolean solved = getProgress().getSolved() > 0;
        if (solved) {
            player.getInventory().setPackProgress(pack.getId(), level + 1);
        }
        result.category(pack.categoryForLevel(level))
                .difficulty(pack.difficultyForLevel(level))
                .pack(pack.getId(), level, solved && level + 1 < pack.getLevels());
    }
}
