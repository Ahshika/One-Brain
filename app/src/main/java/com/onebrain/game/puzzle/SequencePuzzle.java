package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.visual.Visual;

public final class SequencePuzzle extends ChoicePuzzle {

    private final String rule;
    private final int[] shownTerms;

    public SequencePuzzle(Difficulty difficulty, long seed, String rule, int[] shownTerms,
                          Prompt prompt, Visual visual, ChoiceSet choices) {
        super(PuzzleCategory.SEQUENCE, difficulty, seed, prompt, visual, choices);
        this.rule = rule;
        this.shownTerms = shownTerms.clone();
    }

    public String getRule() {
        return rule;
    }

    public int[] getShownTerms() {
        return shownTerms.clone();
    }
}
