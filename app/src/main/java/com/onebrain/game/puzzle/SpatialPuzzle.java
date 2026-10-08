package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.visual.Visual;

public final class SpatialPuzzle extends ChoicePuzzle {

    private final ShapeTransform transform;

    public SpatialPuzzle(Difficulty difficulty, long seed, ShapeTransform transform,
                         Prompt prompt, Visual visual, ChoiceSet choices) {
        super(PuzzleCategory.SPATIAL, difficulty, seed, prompt, visual, choices);
        this.transform = transform;
    }

    public ShapeTransform getTransform() {
        return transform;
    }

    @Override
    public int getParSeconds() {
        return super.getParSeconds() * 2 / 3;
    }
}
