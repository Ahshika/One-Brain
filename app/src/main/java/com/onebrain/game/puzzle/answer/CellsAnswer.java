package com.onebrain.game.puzzle.answer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CellsAnswer implements Answer {

    private final Set<Integer> cells;

    public CellsAnswer(Collection<Integer> cells) {
        this.cells = Collections.unmodifiableSet(new HashSet<>(cells));
    }

    public static CellsAnswer single(int cell) {
        return new CellsAnswer(Collections.singleton(cell));
    }

    public Set<Integer> getCells() {
        return cells;
    }
}
