package com.onebrain.game.puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Options plus the index of the correct one. */
public final class ChoiceSet {

    private final List<Option> options;
    private final int correctIndex;

    public ChoiceSet(List<Option> options, int correctIndex) {
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("A choice needs at least two options");
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Correct index out of range");
        }
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
        this.correctIndex = correctIndex;
    }

    public List<Option> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }
}
