package com.onebrain.game.puzzle;

/** Puzzles with a "study" phase before the player may answer. */
public interface Memorizable {

    long getMemorizeMillis();

    Prompt getRecallPrompt();
}
