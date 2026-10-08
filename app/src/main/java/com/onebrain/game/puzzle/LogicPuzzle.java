package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.visual.Visual;

public final class LogicPuzzle extends ChoicePuzzle {

    public enum Kind {
        /** Each symbol hides a number. */
        SYMBOL_ALGEBRA,
        /** Deduce a ranking from comparisons. */
        ORDERING
    }

    private final Kind kind;

    public LogicPuzzle(Difficulty difficulty, long seed, Kind kind, Prompt prompt, Visual visual, ChoiceSet choices) {
        super(PuzzleCategory.LOGIC, difficulty, seed, prompt, visual, choices);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public int getParSeconds() {
        // Reading statements takes time; logic gets 50% more par.
        return super.getParSeconds() * 3 / 2;
    }
}
