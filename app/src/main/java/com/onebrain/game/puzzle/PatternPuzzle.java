package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.visual.Visual;

public final class PatternPuzzle extends ChoicePuzzle {

    private final String rule;

    public PatternPuzzle(Difficulty difficulty, long seed, String rule, Prompt prompt, Visual visual, ChoiceSet choices) {
        super(PuzzleCategory.PATTERN, difficulty, seed, prompt, visual, choices);
        this.rule = rule;
    }

    /** Internal rule id (for analytics/debugging; never shown before solving). */
    public String getRule() {
        return rule;
    }
}
