package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.ChoiceAnswer;
import com.onebrain.game.puzzle.visual.Visual;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared behaviour for multiple-choice puzzles: answer validation, remembering
 * wrong picks, and the "remove a wrong option" hint.
 */
public abstract class ChoicePuzzle extends Puzzle implements Hintable {

    private final List<Option> options;
    private final int correctIndex;
    private final Set<Integer> disabled = new LinkedHashSet<>();

    protected ChoicePuzzle(PuzzleCategory category, Difficulty difficulty, long seed,
                           Prompt prompt, Visual visual, ChoiceSet choices) {
        super(category, difficulty, seed, prompt, visual);
        this.options = choices.getOptions();
        this.correctIndex = choices.getCorrectIndex();
    }

    @Override
    public InputType getInputType() {
        return InputType.CHOICE;
    }

    @Override
    protected boolean evaluate(Answer answer) {
        return answer instanceof ChoiceAnswer && ((ChoiceAnswer) answer).getIndex() == correctIndex;
    }

    @Override
    protected void onWrongAnswer(Answer answer) {
        if (answer instanceof ChoiceAnswer) {
            disabled.add(((ChoiceAnswer) answer).getIndex());
        }
    }

    @Override
    public boolean canUseHint() {
        // Always leave at least two options so the puzzle never solves itself.
        return options.size() - disabled.size() > 2;
    }

    @Override
    public Hint useHint() {
        if (!canUseHint()) {
            return null;
        }
        int n = options.size();
        int start = (int) Math.floorMod(getSeed(), (long) n);
        for (int k = 0; k < n; k++) {
            int i = (start + k) % n;
            if (i != correctIndex && !disabled.contains(i)) {
                disabled.add(i);
                registerHintUsed();
                return new Hint(Hint.Kind.ELIMINATE_OPTION, i);
            }
        }
        return null;
    }

    public List<Option> getOptions() {
        return options;
    }

    /** Options removed by hints or by earlier wrong attempts. */
    public Set<Integer> getDisabledOptions() {
        return Collections.unmodifiableSet(disabled);
    }

    /** Exposed for solution reveal after the last attempt and for tests. */
    public int getCorrectIndex() {
        return correctIndex;
    }
}
