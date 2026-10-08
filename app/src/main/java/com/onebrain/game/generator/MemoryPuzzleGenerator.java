package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.MemoryPuzzle;
import com.onebrain.game.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Flash a pattern of tiles; the player reproduces it. */
public final class MemoryPuzzleGenerator extends BasePuzzleGenerator {

    private static final int[] SIZE = {3, 4, 5, 6, 7};
    private static final int[] TARGETS = {3, 5, 7, 10, 13};
    private static final long[] SHOW_MILLIS = {2500L, 3000L, 3200L, 3500L, 3800L};

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.MEMORY;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        int level = difficulty.ordinal();
        int n = SIZE[level];
        List<Integer> cells = new ArrayList<>();
        for (int i = 0; i < n * n; i++) {
            cells.add(i);
        }
        Collections.shuffle(cells, r);
        Set<Integer> targets = new HashSet<>(cells.subList(0, TARGETS[level]));
        return new MemoryPuzzle(difficulty, seed, n, n, targets, SHOW_MILLIS[level]);
    }
}
