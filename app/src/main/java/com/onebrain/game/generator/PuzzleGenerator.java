package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Puzzle;

/**
 * Strategy for producing puzzles of one category. Implementations MUST be pure
 * functions of (difficulty, seed): the same inputs always yield the same puzzle.
 * That property is what makes the Daily Puzzle identical for every player.
 */
public interface PuzzleGenerator {

    PuzzleCategory getCategory();

    Puzzle generate(Difficulty difficulty, long seed);
}
