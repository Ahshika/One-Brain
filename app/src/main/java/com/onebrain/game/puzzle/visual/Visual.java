package com.onebrain.game.puzzle.visual;

/**
 * Something shown to the player as part of a puzzle (an equation, a list of
 * statements, a grid). The UI renders visuals through {@link VisualVisitor}, so
 * the model never depends on Android views.
 */
public abstract class Visual {

    public abstract <R> R accept(VisualVisitor<R> visitor);
}
