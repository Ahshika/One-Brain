package com.onebrain.game.puzzle.answer;

public final class ChoiceAnswer implements Answer {

    private final int index;

    public ChoiceAnswer(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
