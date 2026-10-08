package com.onebrain.game.ui.game.render;

import android.view.View;
import android.view.ViewGroup;

import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.answer.Answer;

/**
 * Draws a puzzle and collects the player's answer. One implementation per
 * {@link com.onebrain.game.puzzle.InputType}; the Activity only talks to this
 * interface (polymorphism on the UI side, mirroring the puzzle hierarchy).
 */
public interface PuzzleRenderer {

    interface Callback {
        void onAnswer(Answer answer);

        /** The puzzle is showing something to study; answering is not yet allowed. */
        void onMemorizeStarted(long durationMs);

        /** The puzzle accepts input from now on (start the clock). */
        void onReady();

        void onTap(View source);
    }

    /**
     * @param resumeInteractive true when restoring after a configuration change:
     *                          skip intro phases and restore state from the puzzle.
     */
    void bind(Puzzle puzzle, ViewGroup visualContainer, ViewGroup inputContainer,
              boolean resumeInteractive, Callback callback);

    void showCorrect(Answer answer);

    void showWrong(Answer answer, boolean willRetry);

    /** Shows the right answer after the last attempt was used. */
    void revealSolution();

    void applyHint(Hint hint);

    /** Cancels animations/callbacks; called before the views are discarded. */
    void release();
}
