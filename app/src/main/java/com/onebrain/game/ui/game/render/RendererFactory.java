package com.onebrain.game.ui.game.render;

import com.onebrain.game.puzzle.Puzzle;

/** Picks the renderer for a puzzle's input type. */
public final class RendererFactory {

    private RendererFactory() {
    }

    public static PuzzleRenderer create(Puzzle puzzle) {
        switch (puzzle.getInputType()) {
            case CHOICE:
                return new ChoiceRenderer();
            case GRID_SELECT:
                return new MemoryRenderer();
            case GRID_TAP:
                return new OddOneOutRenderer();
            case LETTER_TILES:
                return new WordRenderer();
            default:
                throw new IllegalArgumentException("No renderer for " + puzzle.getInputType());
        }
    }
}
