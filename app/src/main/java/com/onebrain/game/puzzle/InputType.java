package com.onebrain.game.puzzle;

/** How the player answers; the UI picks a renderer from this. */
public enum InputType {
    /** Pick one of several options. */
    CHOICE,
    /** Select several cells on a grid (memory). */
    GRID_SELECT,
    /** Tap a single cell on a grid (odd one out). */
    GRID_TAP,
    /** Arrange letter tiles (anagram). */
    LETTER_TILES
}
