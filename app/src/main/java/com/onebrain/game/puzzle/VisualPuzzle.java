package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.CellsAnswer;
import com.onebrain.game.puzzle.visual.GridVisual;

/** "Odd one out": one tile differs slightly in color or symbol. */
public final class VisualPuzzle extends Puzzle implements Hintable {

    public enum Kind {
        COLOR,
        SYMBOL
    }

    private final Kind kind;
    private final int oddIndex;
    private boolean rowRevealed;

    public VisualPuzzle(Difficulty difficulty, long seed, Kind kind, GridVisual grid, int oddIndex) {
        super(PuzzleCategory.VISUAL, difficulty, seed, new Prompt(PromptKey.VISUAL_FIND_ODD), grid);
        if (oddIndex < 0 || oddIndex >= grid.size()) {
            throw new IllegalArgumentException("Odd index out of range");
        }
        this.kind = kind;
        this.oddIndex = oddIndex;
    }

    @Override
    public InputType getInputType() {
        return InputType.GRID_TAP;
    }

    @Override
    protected boolean evaluate(Answer answer) {
        if (!(answer instanceof CellsAnswer)) {
            return false;
        }
        CellsAnswer cells = (CellsAnswer) answer;
        return cells.getCells().size() == 1 && cells.getCells().contains(oddIndex);
    }

    @Override
    public int getParSeconds() {
        return 4 + getDifficulty().getLevel() * 4;
    }

    @Override
    public boolean canUseHint() {
        return !rowRevealed;
    }

    @Override
    public Hint useHint() {
        if (!canUseHint()) {
            return null;
        }
        rowRevealed = true;
        registerHintUsed();
        return new Hint(Hint.Kind.REVEAL_ROW, getHintRow());
    }

    public int getHintRow() {
        return oddIndex / getGrid().getCols();
    }

    public boolean isRowRevealed() {
        return rowRevealed;
    }

    public GridVisual getGrid() {
        return (GridVisual) getVisual();
    }

    public Kind getKind() {
        return kind;
    }

    public int getOddIndex() {
        return oddIndex;
    }
}
