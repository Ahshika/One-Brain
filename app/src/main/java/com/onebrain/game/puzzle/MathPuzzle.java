package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.visual.Visual;

public final class MathPuzzle extends ChoicePuzzle {

    public enum Kind {
        MISSING_OPERAND,
        ORDER_OF_OPERATIONS,
        LINEAR_EQUATION,
        SYSTEM
    }

    private final Kind kind;

    public MathPuzzle(Difficulty difficulty, long seed, Kind kind, Prompt prompt, Visual visual, ChoiceSet choices) {
        super(PuzzleCategory.MATH, difficulty, seed, prompt, visual, choices);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public int getParSeconds() {
        // Equations need a little longer than plain arithmetic.
        boolean algebra = kind == Kind.LINEAR_EQUATION || kind == Kind.SYSTEM;
        return algebra ? super.getParSeconds() + 15 : super.getParSeconds();
    }
}
