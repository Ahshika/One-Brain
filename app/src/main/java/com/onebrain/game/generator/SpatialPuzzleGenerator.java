package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.PromptKey;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.ShapeTransform;
import com.onebrain.game.puzzle.SpatialPuzzle;
import com.onebrain.game.puzzle.visual.GridVisual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Mental rotation and reflection of an asymmetric polyomino. */
public final class SpatialPuzzleGenerator extends BasePuzzleGenerator {

    private static final int[] GRID = {3, 4, 4, 5, 5};
    private static final int[] CELLS = {4, 5, 6, 7, 8};

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.SPATIAL;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        int level = difficulty.ordinal();
        int n = GRID[level];
        boolean[] shape = asymmetricShape(r, n, CELLS[level]);

        ShapeTransform target;
        switch (difficulty) {
            case EASY:
                target = ShapeTransform.ROTATE_90;
                break;
            case MEDIUM:
                target = pickOne(r, ShapeTransform.ROTATE_90, ShapeTransform.ROTATE_270, ShapeTransform.MIRROR_H);
                break;
            case HARD:
                target = pickOne(r, ShapeTransform.ROTATE_180, ShapeTransform.MIRROR_V, ShapeTransform.ROTATE_270);
                break;
            case EXPERT:
                target = ShapeTransform.ROTATE_90_MIRROR_H;
                break;
            default:
                target = pickOne(r, ShapeTransform.ROTATE_90_MIRROR_H, ShapeTransform.ROTATE_90_MIRROR_V);
                break;
        }

        GridVisual correct = GridVisual.ofMask(n, n, target.apply(shape, n));
        List<ShapeTransform> others = new ArrayList<>(Arrays.asList(ShapeTransform.values()));
        others.remove(target);
        Collections.shuffle(others, r);
        List<GridVisual> distractors = new ArrayList<>();
        for (ShapeTransform t : others) {
            if (distractors.size() == 3) {
                break;
            }
            distractors.add(GridVisual.ofMask(n, n, t.apply(shape, n)));
        }
        return new SpatialPuzzle(difficulty, seed, target, new Prompt(promptFor(target)),
                GridVisual.ofMask(n, n, shape), gridChoices(r, correct, distractors));
    }

    private static PromptKey promptFor(ShapeTransform t) {
        switch (t) {
            case ROTATE_90:
                return PromptKey.SPATIAL_ROTATE_CW;
            case ROTATE_270:
                return PromptKey.SPATIAL_ROTATE_CCW;
            case ROTATE_180:
                return PromptKey.SPATIAL_ROTATE_180;
            case MIRROR_H:
                return PromptKey.SPATIAL_MIRROR_H;
            case MIRROR_V:
                return PromptKey.SPATIAL_MIRROR_V;
            case ROTATE_90_MIRROR_V:
                return PromptKey.SPATIAL_ROTATE_MIRROR_V;
            default:
                return PromptKey.SPATIAL_ROTATE_MIRROR;
        }
    }

    private static ShapeTransform pickOne(Random r, ShapeTransform... options) {
        return options[r.nextInt(options.length)];
    }

    /** Grows a connected shape until all 8 symmetries are distinct (so every option differs). */
    static boolean[] asymmetricShape(Random r, int n, int cells) {
        boolean[] shape = null;
        for (int attempt = 0; attempt < 200; attempt++) {
            shape = grow(r, n, cells);
            if (allTransformsDistinct(shape, n)) {
                return shape;
            }
        }
        // Fallback: an "L" tetromino has 8 distinct orientations.
        shape = new boolean[n * n];
        shape[0] = true;
        shape[n] = true;
        shape[2 * n] = true;
        shape[2 * n + 1] = true;
        return shape;
    }

    private static boolean[] grow(Random r, int n, int cells) {
        boolean[] shape = new boolean[n * n];
        List<Integer> filled = new ArrayList<>();
        int start = r.nextInt(n * n);
        shape[start] = true;
        filled.add(start);
        int guard = 0;
        while (filled.size() < cells && guard++ < 500) {
            int from = filled.get(r.nextInt(filled.size()));
            int row = from / n;
            int col = from % n;
            int dir = r.nextInt(4);
            int nr = row + (dir == 0 ? -1 : dir == 1 ? 1 : 0);
            int nc = col + (dir == 2 ? -1 : dir == 3 ? 1 : 0);
            if (nr < 0 || nc < 0 || nr >= n || nc >= n) {
                continue;
            }
            int idx = nr * n + nc;
            if (!shape[idx]) {
                shape[idx] = true;
                filled.add(idx);
            }
        }
        return shape;
    }

    static boolean allTransformsDistinct(boolean[] shape, int n) {
        List<boolean[]> seen = new ArrayList<>();
        for (ShapeTransform t : ShapeTransform.values()) {
            boolean[] out = t.apply(shape, n);
            for (boolean[] other : seen) {
                if (Arrays.equals(other, out)) {
                    return false;
                }
            }
            seen.add(out);
        }
        return true;
    }
}
