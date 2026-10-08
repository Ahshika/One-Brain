package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.VisualPuzzle;
import com.onebrain.game.puzzle.visual.GridVisual;
import com.onebrain.game.util.ColorUtil;

import java.util.Arrays;
import java.util.Random;

/** Odd-one-out grids: bigger boards and subtler differences as difficulty rises. */
public final class VisualPuzzleGenerator extends BasePuzzleGenerator {

    private static final String[][][] SYMBOL_PAIRS = {
            // EASY
            {{"★", "☆"}, {"●", "○"}, {"■", "□"}, {"▲", "△"}},
            // MEDIUM
            {{"b", "d"}, {"p", "q"}, {"M", "W"}, {"6", "9"}},
            // HARD
            {{"O", "Q"}, {"E", "F"}, {"C", "G"}, {"P", "R"}},
            // EXPERT
            {{"8", "B"}, {"5", "S"}, {"Z", "2"}, {"V", "Y"}},
            // MASTER
            {{"O", "Q"}, {"8", "B"}, {"E", "F"}, {"P", "R"}, {"5", "S"}}
    };
    private static final float[] VALUE_DELTA = {0.22f, 0.14f, 0.09f, 0.06f, 0.045f};
    private static final int[] SIZE = {3, 4, 5, 6, 7};

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.VISUAL;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        int level = difficulty.ordinal();
        int n = SIZE[level];
        int odd = r.nextInt(n * n);
        if (r.nextInt(3) == 0) {
            String[] pair = SYMBOL_PAIRS[level][r.nextInt(SYMBOL_PAIRS[level].length)];
            boolean flip = r.nextBoolean();
            String base = flip ? pair[1] : pair[0];
            String different = flip ? pair[0] : pair[1];
            String[] labels = new String[n * n];
            Arrays.fill(labels, base);
            labels[odd] = different;
            return new VisualPuzzle(difficulty, seed, VisualPuzzle.Kind.SYMBOL,
                    GridVisual.ofLabels(n, n, labels), odd);
        }
        float hue = r.nextFloat() * 360f;
        float saturation = 0.45f + r.nextFloat() * 0.3f;
        float value = 0.62f + r.nextFloat() * 0.2f;
        float delta = VALUE_DELTA[level];
        float oddValue = value + delta <= 0.98f ? value + delta : value - delta;
        int[] colors = new int[n * n];
        Arrays.fill(colors, ColorUtil.hsv(hue, saturation, value));
        colors[odd] = ColorUtil.hsv(hue, saturation, oddValue);
        return new VisualPuzzle(difficulty, seed, VisualPuzzle.Kind.COLOR, new GridVisual(n, n, null, colors), odd);
    }
}
