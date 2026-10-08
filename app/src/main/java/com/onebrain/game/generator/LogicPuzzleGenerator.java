package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.LogicPuzzle;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.PromptKey;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.visual.LinesVisual;
import com.onebrain.game.puzzle.visual.TextVisual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Two families of deduction puzzles:
 * symbol algebra (each fruit hides a number) and orderings (who is fastest?).
 */
public final class LogicPuzzleGenerator extends BasePuzzleGenerator {

    private static final String[] FRUITS = {
            "🍎", "🍌", "🍇", "🍒",
            "🍋", "🍉", "🍓", "🥝"
    };
    private static final String[] ANIMALS = {
            "🐱", "🐶", "🦊", "🐼",
            "🐸", "🐵", "🐰", "🐯"
    };

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.LOGIC;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        return r.nextBoolean() ? symbols(difficulty, seed, r) : ordering(difficulty, seed, r);
    }

    private Puzzle symbols(Difficulty d, long seed, Random r) {
        List<String> s = pick(r, FRUITS, 4);
        String a = s.get(0);
        String b = s.get(1);
        String c = s.get(2);
        String e = s.get(3);
        String text;
        int answer;
        int[] distractors;
        switch (d) {
            case EASY: {
                int va = range(r, 2, 9);
                int vb = range(r, 1, 9);
                text = a + " + " + a + " + " + a + " = " + (3 * va) + "\n"
                        + a + " + " + b + " = " + (va + vb) + "\n"
                        + b + " + " + b + " + " + a + " = ?";
                answer = 2 * vb + va;
                distractors = new int[]{2 * va + vb, va + vb, answer + 1};
                break;
            }
            case MEDIUM: {
                int va = range(r, 2, 9);
                int vb = range(r, 2, 9);
                int vc = range(r, 1, 9);
                text = a + " + " + a + " = " + (2 * va) + "\n"
                        + a + " + " + b + " + " + b + " = " + (va + 2 * vb) + "\n"
                        + b + " + " + c + " = " + (vb + vc) + "\n"
                        + a + " + " + b + " " + TIMES + " " + c + " = ?";
                answer = va + vb * vc;
                distractors = new int[]{(va + vb) * vc, va + vb + vc, va * vb + vc};
                break;
            }
            case HARD: {
                int va = range(r, 2, 9);
                int vb = range(r, 2, 9);
                int vc = range(r, 1, vb - 1);
                text = a + " + " + a + " + " + a + " = " + (3 * va) + "\n"
                        + a + " " + TIMES + " " + b + " = " + (va * vb) + "\n"
                        + b + " " + MINUS + " " + c + " = " + (vb - vc) + "\n"
                        + a + " " + TIMES + " " + c + " + " + b + " = ?";
                answer = va * vc + vb;
                distractors = new int[]{va * (vc + vb), va * vc, va * vb + vc};
                break;
            }
            case MASTER: {
                int va = range(r, 3, 9);
                int vb = range(r, 2, 9);
                int vc = range(r, 2, 9);
                int vd = range(r, 1, vc - 1 > 0 ? vc - 1 : 1);
                text = a + " " + TIMES + " " + a + " = " + (va * va) + "\n"
                        + a + " + " + b + " + " + b + " = " + (va + 2 * vb) + "\n"
                        + b + " " + TIMES + " " + c + " = " + (vb * vc) + "\n"
                        + c + " " + MINUS + " " + e + " = " + (vc - vd) + "\n"
                        + a + " " + TIMES + " " + b + " " + MINUS + " " + c + " + " + e + " = ?";
                answer = va * vb - vc + vd;
                if (answer <= 0) {
                    answer = va * vb + vc + vd;
                    text = text.substring(0, text.lastIndexOf('\n') + 1)
                            + a + " " + TIMES + " " + b + " + " + c + " + " + e + " = ?";
                }
                distractors = new int[]{va * (vb - vc) + vd, answer + vd, va * vb + vc - vd};
                break;
            }
            default: {
                int va = range(r, 6, 14);
                int vb = range(r, 1, va - 1);
                int vc = range(r, 2, 9);
                // Keep the final answer positive.
                int vd = range(r, 1, Math.min(9, va + vb * vc - 1));
                text = a + " + " + b + " = " + (va + vb) + "\n"
                        + a + " " + MINUS + " " + b + " = " + (va - vb) + "\n"
                        + b + " " + TIMES + " " + c + " = " + (vb * vc) + "\n"
                        + c + " + " + e + " + " + e + " = " + (vc + 2 * vd) + "\n"
                        + a + " + " + b + " " + TIMES + " " + c + " " + MINUS + " " + e + " = ?";
                answer = va + vb * vc - vd;
                distractors = new int[]{(va + vb) * vc - vd, va + vb * vc + vd, va + vb + vc - vd};
                break;
            }
        }
        return new LogicPuzzle(d, seed, LogicPuzzle.Kind.SYMBOL_ALGEBRA, new Prompt(PromptKey.LOGIC_SYMBOLS),
                new TextVisual(text), numericChoices(r, answer, distractors));
    }

    private Puzzle ordering(Difficulty d, long seed, Random r) {
        int n;
        int redundant;
        switch (d) {
            case EASY:
                n = 3;
                redundant = 0;
                break;
            case MEDIUM:
                n = 4;
                redundant = 0;
                break;
            case HARD:
                n = 5;
                redundant = 1;
                break;
            case EXPERT:
                n = 6;
                redundant = 2;
                break;
            default:
                n = 7;
                redundant = 3;
                break;
        }
        // order.get(0) is the fastest.
        List<String> order = pick(r, ANIMALS, n);
        List<Prompt> statements = new ArrayList<>();
        for (int i = 0; i < n - 1; i++) {
            statements.add(compare(r, order.get(i), order.get(i + 1)));
        }
        for (int k = 0; k < redundant; k++) {
            int i = r.nextInt(n - 2);
            int j = range(r, i + 2, n - 1);
            statements.add(compare(r, order.get(i), order.get(j)));
        }
        Collections.shuffle(statements, r);

        PromptKey question;
        String answer;
        boolean askFast = r.nextBoolean();
        if (d == Difficulty.EASY || d == Difficulty.MEDIUM) {
            question = askFast ? PromptKey.LOGIC_FASTEST : PromptKey.LOGIC_SLOWEST;
            answer = askFast ? order.get(0) : order.get(n - 1);
        } else {
            question = askFast ? PromptKey.LOGIC_SECOND_FASTEST : PromptKey.LOGIC_SECOND_SLOWEST;
            answer = askFast ? order.get(1) : order.get(n - 2);
        }
        return new LogicPuzzle(d, seed, LogicPuzzle.Kind.ORDERING, new Prompt(question),
                new LinesVisual(statements), labelChoices(r, answer, order));
    }

    /** "faster beats slower", phrased randomly as either "X is faster" or "Y is slower". */
    private static Prompt compare(Random r, String faster, String slower) {
        return r.nextBoolean()
                ? new Prompt(PromptKey.LOGIC_STMT_FASTER, faster, slower)
                : new Prompt(PromptKey.LOGIC_STMT_SLOWER, slower, faster);
    }
}
