package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.PatternPuzzle;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.PromptKey;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.visual.GridVisual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 3×3 matrices: symbol Latin squares, or number rows following a hidden rule
 * (c = f(a, b)). Generation rejects grids where another rule would also fit.
 */
public final class PatternPuzzleGenerator extends BasePuzzleGenerator {

    private static final String[][] SYMBOL_SETS = {
            {"●", "▲", "■"},
            {"★", "♦", "♥"},
            {"◆", "○", "✚"},
            {"♣", "▼", "♠"}
    };
    private static final String[] EXTRA_SYMBOLS = {"◇", "△", "□", "☆"};

    enum Rule {
        PLUS {
            @Override
            int apply(int a, int b) {
                return a + b;
            }
        },
        TIMES {
            @Override
            int apply(int a, int b) {
                return a * b;
            }
        },
        DOUBLE_SUM {
            @Override
            int apply(int a, int b) {
                return (a + b) * 2;
            }
        },
        TIMES_MINUS_A {
            @Override
            int apply(int a, int b) {
                return a * b - a;
            }
        },
        SQUARE_PLUS {
            @Override
            int apply(int a, int b) {
                return a * a + b;
            }
        },
        DIFF_OF_SQUARES {
            @Override
            int apply(int a, int b) {
                return (a + b) * (a - b);
            }
        },
        PRODUCT_PLUS_SUM {
            @Override
            int apply(int a, int b) {
                return a * b + a + b;
            }
        },
        MINUS {
            @Override
            int apply(int a, int b) {
                return a - b;
            }
        },
        SUM_OF_SQUARES {
            @Override
            int apply(int a, int b) {
                return a * a + b * b;
            }
        },
        CUBE_MINUS_B {
            @Override
            int apply(int a, int b) {
                return a * a * a - b;
            }
        };

        abstract int apply(int a, int b);
    }

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.PATTERN;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        switch (difficulty) {
            case EASY:
                return r.nextBoolean() ? latin(difficulty, seed, r) : numbers(difficulty, seed, r, Rule.PLUS, 1, 12);
            case MEDIUM:
                return r.nextBoolean()
                        ? numbers(difficulty, seed, r, Rule.TIMES, 2, 9)
                        : numbers(difficulty, seed, r, Rule.DOUBLE_SUM, 1, 9);
            case HARD:
                return r.nextBoolean()
                        ? numbers(difficulty, seed, r, Rule.TIMES_MINUS_A, 2, 9)
                        : numbers(difficulty, seed, r, Rule.SQUARE_PLUS, 2, 9);
            case EXPERT:
                return r.nextBoolean()
                        ? numbers(difficulty, seed, r, Rule.DIFF_OF_SQUARES, 3, 12)
                        : numbers(difficulty, seed, r, Rule.PRODUCT_PLUS_SUM, 2, 9);
            default:
                return r.nextBoolean()
                        ? numbers(difficulty, seed, r, Rule.SUM_OF_SQUARES, 2, 12)
                        : numbers(difficulty, seed, r, Rule.CUBE_MINUS_B, 2, 6);
        }
    }

    private Puzzle latin(Difficulty d, long seed, Random r) {
        int setIndex = r.nextInt(SYMBOL_SETS.length);
        List<String> symbols = new ArrayList<>(Arrays.asList(SYMBOL_SETS[setIndex]));
        Collections.shuffle(symbols, r);
        List<Integer> shifts = new ArrayList<>(Arrays.asList(0, 1, 2));
        Collections.shuffle(shifts, r);

        String[] labels = new String[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                labels[row * 3 + col] = symbols.get((col + shifts.get(row)) % 3);
            }
        }
        int hidden = r.nextInt(9);
        String answer = labels[hidden];
        labels[hidden] = "?";

        List<String> pool = new ArrayList<>(symbols);
        pool.add(EXTRA_SYMBOLS[setIndex]);
        return new PatternPuzzle(d, seed, "LATIN", new Prompt(PromptKey.PATTERN_FIND_MISSING),
                GridVisual.ofLabels(3, 3, labels), labelChoices(r, answer, pool));
    }

    private Puzzle numbers(Difficulty d, long seed, Random r, Rule rule, int min, int max) {
        int[][] rows = new int[3][3];
        int hiddenRow = (d == Difficulty.EASY || d == Difficulty.MEDIUM) ? 2 : r.nextInt(3);
        for (int attempt = 0; attempt < 50; attempt++) {
            for (int i = 0; i < 3; i++) {
                int a = range(r, min, max);
                int b = rule == Rule.DIFF_OF_SQUARES ? range(r, 1, a - 1) : range(r, min, max);
                rows[i][0] = a;
                rows[i][1] = b;
                rows[i][2] = rule.apply(a, b);
            }
            if (!isAmbiguous(rows, hiddenRow, rule)) {
                break;
            }
        }

        int answer = rows[hiddenRow][2];
        String[] labels = new String[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                labels[i * 3 + j] = (i == hiddenRow && j == 2) ? "?" : String.valueOf(rows[i][j]);
            }
        }

        // Distractors: what other plausible rules would produce for the hidden row.
        int a = rows[hiddenRow][0];
        int b = rows[hiddenRow][1];
        List<Integer> distractors = new ArrayList<>();
        for (Rule other : Rule.values()) {
            int v = other.apply(a, b);
            if (other != rule && v != answer && v >= 0 && !distractors.contains(v)) {
                distractors.add(v);
            }
        }
        Collections.shuffle(distractors, r);
        int[] picked = new int[Math.min(3, distractors.size())];
        for (int i = 0; i < picked.length; i++) {
            picked[i] = distractors.get(i);
        }
        return new PatternPuzzle(d, seed, rule.name(), new Prompt(PromptKey.PATTERN_FIND_MISSING),
                GridVisual.ofLabels(3, 3, labels), numericChoices(r, answer, picked));
    }

    /** True if a different rule also explains every visible row but disagrees on the answer. */
    static boolean isAmbiguous(int[][] rows, int hiddenRow, Rule rule) {
        for (Rule other : Rule.values()) {
            if (other == rule) {
                continue;
            }
            boolean fits = true;
            for (int i = 0; i < 3 && fits; i++) {
                if (i != hiddenRow && other.apply(rows[i][0], rows[i][1]) != rows[i][2]) {
                    fits = false;
                }
            }
            if (fits && other.apply(rows[hiddenRow][0], rows[hiddenRow][1]) != rows[hiddenRow][2]) {
                return true;
            }
        }
        return false;
    }
}
