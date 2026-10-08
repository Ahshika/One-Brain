package com.onebrain.game.puzzle.visual;

import com.onebrain.game.puzzle.Prompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A list of localizable statements, e.g. "A is faster than B". */
public final class LinesVisual extends Visual {

    private final List<Prompt> lines;

    public LinesVisual(List<Prompt> lines) {
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public List<Prompt> getLines() {
        return lines;
    }

    @Override
    public <R> R accept(VisualVisitor<R> visitor) {
        return visitor.visitLines(this);
    }
}
