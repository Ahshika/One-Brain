package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.PromptKey;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.SequencePuzzle;
import com.onebrain.game.puzzle.visual.TextVisual;

import java.util.Arrays;
import java.util.Random;

/** "What comes next?" number sequences with rules that grow in subtlety. */
public final class SequencePuzzleGenerator extends BasePuzzleGenerator {

    private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53};

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.SEQUENCE;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        String rule;
        int[] t;
        switch (difficulty) {
            case EASY:
                switch (r.nextInt(3)) {
                    case 0:
                        rule = "ARITHMETIC";
                        t = arithmetic(range(r, 1, 20), range(r, 2, 9), 6);
                        break;
                    case 1:
                        rule = "DESCENDING";
                        t = arithmetic(range(r, 60, 99), -range(r, 3, 9), 6);
                        break;
                    default:
                        rule = "DOUBLING";
                        t = geometric(range(r, 1, 6), 2, 6);
                        break;
                }
                break;
            case MEDIUM:
                switch (r.nextInt(4)) {
                    case 0:
                        rule = "TRIPLING";
                        t = geometric(range(r, 1, 4), 3, 6);
                        break;
                    case 1: {
                        rule = "SQUARES";
                        int n = range(r, 1, 6);
                        t = new int[6];
                        for (int i = 0; i < 6; i++) {
                            t[i] = (n + i) * (n + i);
                        }
                        break;
                    }
                    case 2:
                        rule = "GROWING_STEP";
                        t = secondOrder(range(r, 1, 10), range(r, 1, 3), 1, 6);
                        break;
                    default: {
                        rule = "ALTERNATING";
                        int up = range(r, 4, 9);
                        int down = range(r, 1, up - 1);
                        t = new int[6];
                        t[0] = range(r, 1, 10);
                        for (int i = 1; i < 6; i++) {
                            t[i] = t[i - 1] + (i % 2 == 1 ? up : -down);
                        }
                        break;
                    }
                }
                break;
            case HARD:
                switch (r.nextInt(4)) {
                    case 0: {
                        rule = "FIBONACCI";
                        t = new int[7];
                        t[0] = range(r, 1, 5);
                        t[1] = range(r, 1, 6);
                        for (int i = 2; i < 7; i++) {
                            t[i] = t[i - 1] + t[i - 2];
                        }
                        break;
                    }
                    case 1: {
                        rule = "DOUBLE_PLUS_K";
                        int k = r.nextBoolean() ? range(r, 1, 3) : -range(r, 1, 2);
                        t = new int[7];
                        t[0] = range(r, 3, 6);
                        for (int i = 1; i < 7; i++) {
                            t[i] = t[i - 1] * 2 + k;
                        }
                        break;
                    }
                    case 2: {
                        rule = "SQUARES_PLUS_K";
                        int n = range(r, 2, 5);
                        int k = range(r, 1, 9);
                        t = new int[7];
                        for (int i = 0; i < 7; i++) {
                            t[i] = (n + i) * (n + i) + k;
                        }
                        break;
                    }
                    default: {
                        rule = "INTERLEAVED";
                        int a = range(r, 1, 9);
                        int da = range(r, 2, 5);
                        int b = range(r, 30, 45);
                        int db = range(r, 1, 3);
                        t = new int[8];
                        for (int i = 0; i < 8; i++) {
                            t[i] = i % 2 == 0 ? a + (i / 2) * da : b - (i / 2) * db;
                        }
                        break;
                    }
                }
                break;
            case MASTER:
                switch (r.nextInt(3)) {
                    case 0: {
                        rule = "MULTIPLY_THEN_ADD";
                        int factor = range(r, 2, 3);
                        int add = range(r, 1, 5);
                        t = new int[8];
                        t[0] = range(r, 1, 4);
                        for (int i = 1; i < 8; i++) {
                            t[i] = i % 2 == 1 ? t[i - 1] * factor : t[i - 1] + add;
                        }
                        break;
                    }
                    case 1: {
                        rule = "SQUARE_STEPS";
                        int n = range(r, 1, 3);
                        t = new int[7];
                        t[0] = range(r, 1, 10);
                        for (int i = 1; i < 7; i++) {
                            int k = n + i - 1;
                            t[i] = t[i - 1] + k * k;
                        }
                        break;
                    }
                    default: {
                        rule = "DOUBLING_STEPS";
                        int step = range(r, 1, 3);
                        t = new int[7];
                        t[0] = range(r, 1, 9);
                        for (int i = 1; i < 7; i++) {
                            t[i] = t[i - 1] + step;
                            step *= 2;
                        }
                        break;
                    }
                }
                break;
            default:
                switch (r.nextInt(4)) {
                    case 0: {
                        rule = "CUBES";
                        int n = range(r, 1, 4);
                        t = new int[6];
                        for (int i = 0; i < 6; i++) {
                            int v = n + i;
                            t[i] = v * v * v;
                        }
                        break;
                    }
                    case 1:
                        rule = "SECOND_ORDER";
                        t = secondOrder(range(r, 1, 10), range(r, 1, 4), range(r, 2, 4), 7);
                        break;
                    case 2: {
                        rule = "GROWING_FACTOR";
                        t = new int[6];
                        t[0] = range(r, 1, 3);
                        int startFactor = range(r, 1, 2);
                        for (int i = 1; i < 6; i++) {
                            t[i] = t[i - 1] * (startFactor + i);
                        }
                        break;
                    }
                    default: {
                        rule = "TRIBONACCI";
                        t = new int[8];
                        t[0] = range(r, 1, 2);
                        t[1] = range(r, 1, 3);
                        t[2] = range(r, 2, 4);
                        for (int i = 3; i < 8; i++) {
                            t[i] = t[i - 1] + t[i - 2] + t[i - 3];
                        }
                        break;
                    }
                }
                break;
        }
        // Occasionally use primes on expert for variety.
        if (difficulty == Difficulty.EXPERT && r.nextInt(5) == 0) {
            rule = "PRIMES";
            int offset = range(r, 0, PRIMES.length - 7);
            t = Arrays.copyOfRange(PRIMES, offset, offset + 7);
        }

        int answer = t[t.length - 1];
        int[] shown = Arrays.copyOf(t, t.length - 1);
        int last = shown[shown.length - 1];
        int prev = shown[shown.length - 2];
        int naive = last + (last - prev);

        StringBuilder text = new StringBuilder();
        for (int v : shown) {
            text.append(v).append(", ");
        }
        text.append('?');

        return new SequencePuzzle(difficulty, seed, rule, shown, new Prompt(PromptKey.SEQUENCE_NEXT),
                new TextVisual(text.toString()),
                numericChoices(r, answer, naive, answer + Math.max(1, Math.abs(last - prev) / 2), answer - 1));
    }

    private static int[] arithmetic(int start, int step, int count) {
        int[] t = new int[count];
        for (int i = 0; i < count; i++) {
            t[i] = start + i * step;
        }
        return t;
    }

    private static int[] geometric(int start, int factor, int count) {
        int[] t = new int[count];
        t[0] = start;
        for (int i = 1; i < count; i++) {
            t[i] = t[i - 1] * factor;
        }
        return t;
    }

    /** Differences grow by {@code growth} each step. */
    private static int[] secondOrder(int start, int firstStep, int growth, int count) {
        int[] t = new int[count];
        t[0] = start;
        int step = firstStep;
        for (int i = 1; i < count; i++) {
            t[i] = t[i - 1] + step;
            step += growth;
        }
        return t;
    }
}
