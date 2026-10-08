package com.onebrain.game.ui.game.render;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.onebrain.game.R;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.MemoryPuzzle;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.CellsAnswer;
import com.onebrain.game.ui.common.Anim;
import com.onebrain.game.ui.view.GridBoardView;

import java.util.LinkedHashSet;
import java.util.Set;

/** Memorize → recall flow. A wrong attempt replays the pattern before retrying. */
public final class MemoryRenderer implements PuzzleRenderer {

    private static final long PROGRESS_MAX = 1000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<Integer> selected = new LinkedHashSet<>();
    private final Set<Integer> locked = new LinkedHashSet<>();
    private MemoryPuzzle puzzle;
    private Callback callback;
    private GridBoardView board;
    private LinearProgressIndicator progress;
    private ValueAnimator countdown;
    private boolean interactive;

    @Override
    public void bind(Puzzle p, ViewGroup visualContainer, ViewGroup inputContainer,
                     boolean resumeInteractive, Callback cb) {
        puzzle = (MemoryPuzzle) p;
        callback = cb;
        Context ctx = visualContainer.getContext();

        board = new GridBoardView(ctx);
        board.setGrid(puzzle.getGrid());
        board.setMaxSize(ctx.getResources().getDimensionPixelSize(R.dimen.board_max));
        board.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        board.setOnCellTapListener(this::onCellTap);
        visualContainer.addView(board, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

        progress = new LinearProgressIndicator(ctx);
        progress.setMax((int) PROGRESS_MAX);
        progress.setTrackCornerRadius((int) (4 * ctx.getResources().getDisplayMetrics().density));
        inputContainer.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        for (int cell : puzzle.getRevealedCells()) {
            lock(cell);
        }
        if (resumeInteractive) {
            startRecall();
        } else {
            startMemorize();
        }
    }

    private void startMemorize() {
        interactive = false;
        board.clearStates();
        selected.clear();
        for (int cell : locked) {
            selected.add(cell);
            board.setCellState(cell, GridBoardView.STATE_LOCKED);
        }
        board.setRevealFills(true);
        progress.setVisibility(View.VISIBLE);
        long duration = puzzle.getMemorizeMillis();
        if (callback != null) {
            callback.onMemorizeStarted(duration);
        }
        countdown = ValueAnimator.ofInt((int) PROGRESS_MAX, 0);
        countdown.setDuration(duration);
        countdown.setInterpolator(new LinearInterpolator());
        countdown.addUpdateListener(a -> progress.setProgress((Integer) a.getAnimatedValue()));
        countdown.start();
        handler.postDelayed(this::startRecall, duration);
    }

    private void startRecall() {
        board.setRevealFills(false);
        progress.setVisibility(View.INVISIBLE);
        interactive = true;
        if (callback != null) {
            callback.onReady();
        }
    }

    private void onCellTap(int index) {
        if (!interactive || locked.contains(index)) {
            return;
        }
        if (selected.remove(index)) {
            board.setCellState(index, GridBoardView.STATE_NONE);
        } else {
            selected.add(index);
            board.setCellState(index, GridBoardView.STATE_SELECTED);
        }
        if (callback != null) {
            callback.onTap(board);
        }
        submitIfComplete();
    }

    private void submitIfComplete() {
        if (selected.size() == puzzle.getTargetCount() && callback != null) {
            interactive = false;
            callback.onAnswer(new CellsAnswer(selected));
        }
    }

    private void lock(int cell) {
        locked.add(cell);
        selected.add(cell);
        board.setCellState(cell, GridBoardView.STATE_LOCKED);
    }

    @Override
    public void showCorrect(Answer answer) {
        interactive = false;
        for (int cell : selected) {
            board.setCellState(cell, GridBoardView.STATE_CORRECT);
        }
        Anim.pop(board);
    }

    @Override
    public void showWrong(Answer answer, boolean willRetry) {
        interactive = false;
        for (int cell : selected) {
            if (!locked.contains(cell)) {
                board.setCellState(cell, GridBoardView.STATE_WRONG);
            }
        }
        Anim.shake(board);
        if (willRetry) {
            handler.postDelayed(this::startMemorize, 700);
        }
    }

    @Override
    public void revealSolution() {
        interactive = false;
        board.clearStates();
        for (int cell : puzzle.getTargets()) {
            board.setCellState(cell, GridBoardView.STATE_CORRECT);
        }
    }

    @Override
    public void applyHint(Hint hint) {
        if (hint.getKind() != Hint.Kind.REVEAL_CELL) {
            return;
        }
        lock(hint.getValue());
        Anim.pop(board);
        submitIfComplete();
    }

    @Override
    public void release() {
        handler.removeCallbacksAndMessages(null);
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        if (board != null) {
            board.setOnCellTapListener(null);
        }
        callback = null;
    }
}
