package com.onebrain.game.puzzle;

/** Puzzles that can offer hints implement this interface. */
public interface Hintable {

    boolean canUseHint();

    /** Consumes a hint, or returns {@code null} when none is available. */
    Hint useHint();
}
