package com.onebrain.game.puzzle;

/** Clue for a word-puzzle answer: an emoji picture plus the word's theme. Immutable. */
public final class WordClue {

    private final String emoji;
    private final WordTheme theme;

    public WordClue(String emoji, WordTheme theme) {
        if (emoji == null || emoji.isEmpty() || theme == null) {
            throw new IllegalArgumentException("A clue needs an emoji and a theme");
        }
        this.emoji = emoji;
        this.theme = theme;
    }

    public String getEmoji() {
        return emoji;
    }

    public WordTheme getTheme() {
        return theme;
    }
}
