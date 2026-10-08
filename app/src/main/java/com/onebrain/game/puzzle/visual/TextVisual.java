package com.onebrain.game.puzzle.visual;

/** Language-neutral text such as "3x + 7 = 31" or "2, 4, 8, 16, ?". */
public final class TextVisual extends Visual {

    private final String text;

    public TextVisual(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public <R> R accept(VisualVisitor<R> visitor) {
        return visitor.visitText(this);
    }
}
