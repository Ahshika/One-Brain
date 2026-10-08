package com.onebrain.game.ui.game.render;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.onebrain.game.R;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.VisualPuzzle;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.CellsAnswer;
import com.onebrain.game.ui.common.Anim;
import com.onebrain.game.ui.view.GridBoardView;

/** Tap the one tile that differs. */
public final class OddOneOutRenderer implements PuzzleRenderer {

    private VisualPuzzle puzzle;
    private Callback callback;
    private GridBoardView board;
    private boolean interactive = true;

    @Override
    public void bind(Puzzle p, ViewGroup visualContainer, ViewGroup inputContainer,
                     boolean resumeInteractive, Callback cb) {
        puzzle = (VisualPuzzle) p;
        callback = cb;
        Context ctx = visualContainer.getContext();
        board = new GridBoardView(ctx);
        board.setGrid(puzzle.getGrid());
        board.setMaxSize(ctx.getResources().getDimensionPixelSize(R.dimen.board_max));
        board.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        board.setOnCellTapListener(this::onCellTap);
        visualContainer.addView(board, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        if (puzzle.isRowRevealed()) {
            dimOtherRows(puzzle.getHintRow());
        }
        cb.onReady();
    }

    private void onCellTap(int index) {
        int state = board.getCellState(index);
        if (!interactive || callback == null || state == GridBoardView.STATE_WRONG
                || state == GridBoardView.STATE_DIMMED) {
            return;
        }
        callback.onTap(board);
        callback.onAnswer(CellsAnswer.single(index));
    }

    private int indexOf(Answer answer) {
        if (answer instanceof CellsAnswer && !((CellsAnswer) answer).getCells().isEmpty()) {
            return ((CellsAnswer) answer).getCells().iterator().next();
        }
        return -1;
    }

    @Override
    public void showCorrect(Answer answer) {
        interactive = false;
        board.setCellState(indexOf(answer), GridBoardView.STATE_CORRECT);
        Anim.pop(board);
    }

    @Override
    public void showWrong(Answer answer, boolean willRetry) {
        board.setCellState(indexOf(answer), GridBoardView.STATE_WRONG);
        Anim.shake(board);
        if (!willRetry) {
            interactive = false;
        }
    }

    @Override
    public void revealSolution() {
        interactive = false;
        board.setCellState(puzzle.getOddIndex(), GridBoardView.STATE_CORRECT);
    }

    @Override
    public void applyHint(Hint hint) {
        if (hint.getKind() == Hint.Kind.REVEAL_ROW) {
            dimOtherRows(hint.getValue());
        }
    }

    private void dimOtherRows(int row) {
        int cols = puzzle.getGrid().getCols();
        for (int i = 0; i < puzzle.getGrid().size(); i++) {
            if (i / cols != row && board.getCellState(i) == GridBoardView.STATE_NONE) {
                board.setCellState(i, GridBoardView.STATE_DIMMED);
            }
        }
    }

    @Override
    public void release() {
        if (board != null) {
            board.setOnCellTapListener(null);
        }
        callback = null;
    }
}
