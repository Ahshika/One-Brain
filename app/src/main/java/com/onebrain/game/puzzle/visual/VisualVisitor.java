package com.onebrain.game.puzzle.visual;

public interface VisualVisitor<R> {

    R visitText(TextVisual visual);

    R visitLines(LinesVisual visual);

    R visitGrid(GridVisual visual);
}
