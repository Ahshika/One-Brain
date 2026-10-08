package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.MathPuzzle;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.PromptKey;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.visual.TextVisual;

import java.util.Random;

/** Arithmetic and algebra, from missing operands up to small linear systems. */
public final class MathPuzzleGenerator extends BasePuzzleGenerator {

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.MATH;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        switch (difficulty) {
            case EASY:
                return easy(r, seed);
            case MEDIUM:
                return medium(r, seed);
            case HARD:
                return hard(r, seed);
            case EXPERT:
                return expert(r, seed);
            default:
                return master(r, seed);
        }
    }

    private Puzzle master(Random r, long seed) {
        switch (r.nextInt(3)) {
            case 0: {
                // 2x + y = s1, x + 3y = s2  →  x × y
                int x = range(r, 3, 15);
                int y = range(r, 2, 12);
                String lines = "2x + y = " + (2 * x + y) + "\nx + 3y = " + (x + 3 * y) + "\nx " + TIMES + " y = ?";
                int answer = x * y;
                return make(Difficulty.MASTER, seed, r, MathPuzzle.Kind.SYSTEM, PromptKey.MATH_LAST_LINE,
                        lines, answer, (x + 1) * y, x * (y + 1), answer + x + y);
            }
            case 1: {
                // ? × a − b = c × d
                int q = range(r, 6, 19);
                int a = range(r, 3, 9);
                int c = range(r, 3, 9);
                int d = range(r, 2, Math.max(2, (q * a - 1) / c));
                int b = q * a - c * d;
                if (b <= 0) {
                    d = 1;
                    b = q * a - c;
                }
                return make(Difficulty.MASTER, seed, r, MathPuzzle.Kind.MISSING_OPERAND, PromptKey.MATH_FIND_MISSING,
                        "? " + TIMES + " " + a + " " + MINUS + " " + b + " = " + c + " " + TIMES + " " + d,
                        q, q + 1, q - 1, (c * d + b) / Math.max(1, a - 1));
            }
            default: {
                int a = range(r, 12, 29);
                int b = range(r, 11, 19);
                int c = range(r, 12, 29);
                int d = range(r, 3, 9);
                int answer = a * b + c * d;
                return make(Difficulty.MASTER, seed, r, MathPuzzle.Kind.ORDER_OF_OPERATIONS, PromptKey.MATH_EVALUATE,
                        a + " " + TIMES + " " + b + " + " + c + " " + TIMES + " " + d + " = ?",
                        answer, (a * b + c) * d, answer + 10, answer - a);
            }
        }
    }

    private Puzzle easy(Random r, long seed) {
        switch (r.nextInt(3)) {
            case 0: {
                int a = range(r, 5, 30);
                int b = range(r, 3, 25);
                int c = a + b;
                return make(Difficulty.EASY, seed, r, MathPuzzle.Kind.MISSING_OPERAND, PromptKey.MATH_FIND_MISSING,
                        a + " + ? = " + c, b, b + 1, b - 1, b + 10);
            }
            case 1: {
                int b = range(r, 3, 20);
                int c = range(r, 5, 30);
                int a = b + c;
                return make(Difficulty.EASY, seed, r, MathPuzzle.Kind.MISSING_OPERAND, PromptKey.MATH_FIND_MISSING,
                        "? " + MINUS + " " + b + " = " + c, a, Math.abs(c - b), a + 1, a - 2);
            }
            default: {
                int a = range(r, 2, 9);
                int b = range(r, 2, 9);
                return make(Difficulty.EASY, seed, r, MathPuzzle.Kind.MISSING_OPERAND, PromptKey.MATH_FIND_MISSING,
                        a + " " + TIMES + " ? = " + (a * b), b, b + 1, b - 1, a);
            }
        }
    }

    private Puzzle medium(Random r, long seed) {
        switch (r.nextInt(3)) {
            case 0: {
                int a = range(r, 3, 9);
                int b = range(r, 3, 12);
                int c = range(r, 5, 30);
                int answer = a * b + c;
                return make(Difficulty.MEDIUM, seed, r, MathPuzzle.Kind.ORDER_OF_OPERATIONS, PromptKey.MATH_EVALUATE,
                        a + " " + TIMES + " " + b + " + " + c + " = ?", answer, a * (b + c), answer + a, answer - b);
            }
            case 1: {
                int a = range(r, 3, 9);
                int x = range(r, 3, 12);
                int c = range(r, 2, Math.min(20, a * x - 1));
                int e = a * x - c;
                return make(Difficulty.MEDIUM, seed, r, MathPuzzle.Kind.MISSING_OPERAND, PromptKey.MATH_FIND_MISSING,
                        a + " " + TIMES + " ? " + MINUS + " " + c + " = " + e, x, x + 1, x - 1, x + 2);
            }
            default: {
                int a = range(r, 2, 9);
                int x = range(r, 2, 12);
                int b = range(r, 1, 30);
                int c = a * x + b;
                return make(Difficulty.MEDIUM, seed, r, MathPuzzle.Kind.LINEAR_EQUATION, PromptKey.MATH_SOLVE_X,
                        a + "x + " + b + " = " + c, x, x + 1, x - 1, c - b);
            }
        }
    }

    private Puzzle hard(Random r, long seed) {
        switch (r.nextInt(3)) {
            case 0: {
                int a = range(r, 3, 15);
                int b = range(r, 3, 15);
                int c = range(r, 2, 9);
                int d = range(r, 1, Math.min(30, (a + b) * c - 1));
                int answer = (a + b) * c - d;
                return make(Difficulty.HARD, seed, r, MathPuzzle.Kind.ORDER_OF_OPERATIONS, PromptKey.MATH_EVALUATE,
                        "(" + a + " + " + b + ") " + TIMES + " " + c + " " + MINUS + " " + d + " = ?",
                        answer, a + b * c - d, (a + b) * c + d, answer + c);
            }
            case 1: {
                int x = range(r, 2, 12);
                int a = range(r, 4, 9);
                int c = range(r, 1, a - 1);
                int b = range(r, 1, 20);
                int e = (a - c) * x - b;
                if (e <= 0) {
                    b = range(r, 1, (a - c) * x - 1);
                    e = (a - c) * x - b;
                }
                String right = (c == 1 ? "x" : c + "x") + " + " + e;
                return make(Difficulty.HARD, seed, r, MathPuzzle.Kind.LINEAR_EQUATION, PromptKey.MATH_SOLVE_X,
                        a + "x " + MINUS + " " + b + " = " + right, x, x + 1, x - 1, x + 2);
            }
            default: {
                int a = range(r, 8, 15);
                int b = range(r, 2, 7);
                int c = range(r, 2, 7);
                int answer = a * a - b * c;
                return make(Difficulty.HARD, seed, r, MathPuzzle.Kind.ORDER_OF_OPERATIONS, PromptKey.MATH_EVALUATE,
                        a + "² " + MINUS + " " + b + " " + TIMES + " " + c + " = ?",
                        answer, (a * a - b) * c, answer + b, a * 2 - b * c);
            }
        }
    }

    private Puzzle expert(Random r, long seed) {
        switch (r.nextInt(3)) {
            case 0: {
                int x = range(r, 6, 20);
                int y = range(r, 1, x - 1);
                int answer = x * y;
                String lines = "x + y = " + (x + y) + "\nx " + MINUS + " y = " + (x - y) + "\nx " + TIMES + " y = ?";
                return make(Difficulty.EXPERT, seed, r, MathPuzzle.Kind.SYSTEM, PromptKey.MATH_LAST_LINE,
                        lines, answer, (x + y) * (x - y), answer + x, answer - y);
            }
            case 1: {
                int a = range(r, 11, 25);
                int b = range(r, 3, 9);
                int c = range(r, 5, 15);
                int d = range(r, 2, 9);
                int e = range(r, 1, 50);
                if (a * b - c * d + e <= 0) {
                    e += c * d - a * b + range(r, 1, 10);
                }
                int answer = a * b - c * d + e;
                return make(Difficulty.EXPERT, seed, r, MathPuzzle.Kind.ORDER_OF_OPERATIONS, PromptKey.MATH_EVALUATE,
                        a + " " + TIMES + " " + b + " " + MINUS + " " + c + " " + TIMES + " " + d + " + " + e + " = ?",
                        answer, (a * b - c) * d + e, a * b - c * d - e, answer + 10);
            }
            default: {
                int a = range(r, 3, 12);
                int q = range(r, 3, 15);
                int b = range(r, 2, 20);
                int c = q + b;
                int answer = q * a;
                return make(Difficulty.EXPERT, seed, r, MathPuzzle.Kind.MISSING_OPERAND, PromptKey.MATH_FIND_MISSING,
                        "? " + DIVIDE + " " + a + " + " + b + " = " + c, answer, q, (c + b) * a, answer + a);
            }
        }
    }

    private MathPuzzle make(Difficulty d, long seed, Random r, MathPuzzle.Kind kind, PromptKey key,
                            String expression, int answer, int... distractors) {
        return new MathPuzzle(d, seed, kind, new Prompt(key), new TextVisual(expression),
                numericChoices(r, answer, distractors));
    }
}
