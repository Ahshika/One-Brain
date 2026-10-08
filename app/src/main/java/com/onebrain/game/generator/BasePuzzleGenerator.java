package com.onebrain.game.generator;

import com.onebrain.game.puzzle.ChoiceSet;
import com.onebrain.game.puzzle.Option;
import com.onebrain.game.puzzle.visual.GridVisual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Shared helpers for generators: ranges, picks and plausible distractors. */
public abstract class BasePuzzleGenerator implements PuzzleGenerator {

    protected static final String MINUS = "−";
    protected static final String TIMES = "×";
    protected static final String DIVIDE = "÷";

    /** Inclusive random range. */
    protected static int range(Random r, int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + r.nextInt(max - min + 1);
    }

    protected static <T> List<T> pick(Random r, T[] source, int count) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, source);
        Collections.shuffle(list, r);
        return new ArrayList<>(list.subList(0, Math.min(count, list.size())));
    }

    /**
     * Builds four unique numeric options. Supplied distractors (common mistakes)
     * are preferred; random near-misses fill any remaining slots.
     */
    protected static ChoiceSet numericChoices(Random r, int correct, int... distractors) {
        Set<Integer> values = new LinkedHashSet<>();
        values.add(correct);
        for (int d : distractors) {
            if (values.size() >= 4) {
                break;
            }
            if (d != correct && (correct < 0 || d >= 0)) {
                values.add(d);
            }
        }
        int spread = Math.max(3, Math.abs(correct) / 6 + 2);
        int guard = 0;
        while (values.size() < 4 && guard++ < 500) {
            int delta = 1 + r.nextInt(spread);
            int candidate = r.nextBoolean() ? correct + delta : correct - delta;
            if (correct >= 0 && candidate < 0) {
                candidate = correct + delta;
            }
            values.add(candidate);
        }
        List<Integer> shuffled = new ArrayList<>(values);
        Collections.shuffle(shuffled, r);
        List<Option> options = new ArrayList<>();
        for (int v : shuffled) {
            options.add(Option.text(String.valueOf(v)));
        }
        return new ChoiceSet(options, shuffled.indexOf(correct));
    }

    /** Correct label plus up to three other distinct labels from the pool. */
    protected static ChoiceSet labelChoices(Random r, String correct, List<String> pool) {
        List<String> others = new ArrayList<>();
        for (String s : pool) {
            if (!s.equals(correct) && !others.contains(s)) {
                others.add(s);
            }
        }
        Collections.shuffle(others, r);
        List<String> chosen = new ArrayList<>();
        chosen.add(correct);
        chosen.addAll(others.subList(0, Math.min(3, others.size())));
        Collections.shuffle(chosen, r);
        List<Option> options = new ArrayList<>();
        for (String s : chosen) {
            options.add(Option.text(s));
        }
        return new ChoiceSet(options, chosen.indexOf(correct));
    }

    /** Correct grid plus distractor grids (caller guarantees uniqueness). */
    protected static ChoiceSet gridChoices(Random r, GridVisual correct, List<GridVisual> distractors) {
        List<GridVisual> all = new ArrayList<>();
        all.add(correct);
        all.addAll(distractors);
        Collections.shuffle(all, r);
        List<Option> options = new ArrayList<>();
        for (GridVisual g : all) {
            options.add(Option.grid(g));
        }
        return new ChoiceSet(options, all.indexOf(correct));
    }
}
