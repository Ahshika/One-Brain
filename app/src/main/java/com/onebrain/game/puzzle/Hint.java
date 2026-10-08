package com.onebrain.game.puzzle;

/** Result of using a hint. Interpretation of {@link #getValue()} depends on the kind. */
public final class Hint {

    public enum Kind {
        /** value = option index to remove. */
        ELIMINATE_OPTION,
        /** value = cell index revealed. */
        REVEAL_CELL,
        /** value = row index that contains the answer. */
        REVEAL_ROW,
        /** value = letter position revealed. */
        REVEAL_LETTER
    }

    private final Kind kind;
    private final int value;

    public Hint(Kind kind, int value) {
        this.kind = kind;
        this.value = value;
    }

    public Kind getKind() {
        return kind;
    }

    public int getValue() {
        return value;
    }
}
