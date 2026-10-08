package com.onebrain.game.puzzle;

import com.onebrain.game.puzzle.visual.GridVisual;

/** One answer option: either text (number, symbol, emoji) or a small grid. */
public final class Option {

    private final String text;
    private final GridVisual grid;

    private Option(String text, GridVisual grid) {
        this.text = text;
        this.grid = grid;
    }

    public static Option text(String text) {
        return new Option(text, null);
    }

    public static Option grid(GridVisual grid) {
        return new Option(null, grid);
    }

    public boolean isGrid() {
        return grid != null;
    }

    public String getText() {
        return text;
    }

    public GridVisual getGrid() {
        return grid;
    }

    public boolean sameContent(Option other) {
        if (other == null) {
            return false;
        }
        if (isGrid()) {
            return grid.sameContent(other.grid);
        }
        return text != null && text.equals(other.text);
    }
}
