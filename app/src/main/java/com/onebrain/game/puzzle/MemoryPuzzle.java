package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.CellsAnswer;
import com.onebrain.game.puzzle.visual.GridVisual;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/** Study highlighted tiles, then tap them from memory. */
public final class MemoryPuzzle extends Puzzle implements Memorizable, Hintable {

    private final int rows;
    private final int cols;
    private final Set<Integer> targets;
    private final long memorizeMillis;
    private final Set<Integer> revealed = new LinkedHashSet<>();

    public MemoryPuzzle(Difficulty difficulty, long seed, int rows, int cols, Set<Integer> targets, long memorizeMillis) {
        super(PuzzleCategory.MEMORY, difficulty, seed, new Prompt(PromptKey.MEMORY_MEMORIZE),
                GridVisual.ofMask(rows, cols, toMask(rows * cols, targets)));
        this.rows = rows;
        this.cols = cols;
        this.targets = Collections.unmodifiableSet(new TreeSet<>(targets));
        this.memorizeMillis = memorizeMillis;
    }

    private static boolean[] toMask(int size, Set<Integer> targets) {
        boolean[] mask = new boolean[size];
        for (int t : targets) {
            mask[t] = true;
        }
        return mask;
    }

    @Override
    public InputType getInputType() {
        return InputType.GRID_SELECT;
    }

    @Override
    protected boolean evaluate(Answer answer) {
        return answer instanceof CellsAnswer && ((CellsAnswer) answer).getCells().equals(targets);
    }

    @Override
    public long getMemorizeMillis() {
        return memorizeMillis;
    }

    @Override
    public Prompt getRecallPrompt() {
        return new Prompt(PromptKey.MEMORY_RECALL, targets.size());
    }

    @Override
    public int getParSeconds() {
        // Par only covers the recall phase.
        return Math.max(6, targets.size() * 2);
    }

    @Override
    public boolean canUseHint() {
        return revealed.size() < targets.size() - 1;
    }

    @Override
    public Hint useHint() {
        if (!canUseHint()) {
            return null;
        }
        for (int t : targets) {
            if (revealed.add(t)) {
                registerHintUsed();
                return new Hint(Hint.Kind.REVEAL_CELL, t);
            }
        }
        return null;
    }

    public GridVisual getGrid() {
        return (GridVisual) getVisual();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTargetCount() {
        return targets.size();
    }

    public Set<Integer> getTargets() {
        return targets;
    }

    public Set<Integer> getRevealedCells() {
        return Collections.unmodifiableSet(revealed);
    }
}
