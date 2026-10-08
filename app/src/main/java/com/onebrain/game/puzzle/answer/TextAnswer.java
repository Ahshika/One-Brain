package com.onebrain.game.puzzle.answer;

import java.util.Locale;

public final class TextAnswer implements Answer {

    private final String text;

    public TextAnswer(String text) {
        this.text = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }

    public String getText() {
        return text;
    }
}
