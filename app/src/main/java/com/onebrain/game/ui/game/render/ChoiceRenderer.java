package com.onebrain.game.ui.game.render;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.onebrain.game.R;
import com.onebrain.game.puzzle.ChoicePuzzle;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.Option;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.ChoiceAnswer;
import com.onebrain.game.ui.common.Anim;
import com.onebrain.game.ui.view.GridBoardView;

import java.util.ArrayList;
import java.util.List;

/** Multiple-choice input: a 2-column grid of option cards (text or mini grids). */
public final class ChoiceRenderer implements PuzzleRenderer {

    private final List<MaterialCardView> cards = new ArrayList<>();
    private ChoicePuzzle puzzle;
    private Callback callback;
    private boolean interactive = true;
    private float density;

    @Override
    public void bind(Puzzle p, ViewGroup visualContainer, ViewGroup inputContainer,
                     boolean resumeInteractive, Callback cb) {
        puzzle = (ChoicePuzzle) p;
        callback = cb;
        Context ctx = inputContainer.getContext();
        density = ctx.getResources().getDisplayMetrics().density;

        if (p.getVisual() != null) {
            visualContainer.addView(p.getVisual().accept(new VisualViewFactory(ctx)));
        }

        LinearLayout column = new LinearLayout(ctx);
        column.setOrientation(LinearLayout.VERTICAL);
        List<Option> options = puzzle.getOptions();
        LinearLayout row = null;
        int margin = (int) (6 * density);
        for (int i = 0; i < options.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                column.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            MaterialCardView card = createCard(ctx, options.get(i), i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(margin, margin, margin, margin);
            row.addView(card, lp);
            cards.add(card);
        }
        if (options.size() % 2 == 1 && row != null) {
            View spacer = new View(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 1, 1f);
            lp.setMargins(margin, margin, margin, margin);
            row.addView(spacer, lp);
        }
        inputContainer.addView(column, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int disabled : puzzle.getDisabledOptions()) {
            disable(cards.get(disabled), false);
        }
        cb.onReady();
    }

    private MaterialCardView createCard(Context ctx, Option option, int index) {
        MaterialCardView card = new MaterialCardView(ctx);
        card.setRadius(18 * density);
        card.setCardElevation(0f);
        card.setStrokeWidth((int) (1.5f * density));
        card.setStrokeColor(ContextCompat.getColor(ctx, R.color.ob_stroke));
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.ob_surface));
        card.setClickable(true);
        card.setFocusable(true);
        card.setStateListAnimator(android.animation.AnimatorInflater.loadStateListAnimator(ctx, R.animator.press_scale));

        View content;
        if (option.isGrid()) {
            GridBoardView board = new GridBoardView(ctx);
            board.setGrid(option.getGrid());
            board.setMaxSize((int) (110 * density));
            int pad = (int) (14 * density);
            board.setPadding(pad, pad, pad, pad);
            content = board;
            card.addView(content, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        } else {
            TextView tv = new TextView(ctx);
            tv.setText(option.getText());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(ContextCompat.getColor(ctx, R.color.ob_text));
            tv.setGravity(Gravity.CENTER);
            tv.setTextDirection(View.TEXT_DIRECTION_LTR);
            int pad = (int) (18 * density);
            tv.setPadding(pad, pad, pad, pad);
            content = tv;
            card.addView(content, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            card.setContentDescription(option.getText());
        }
        card.setOnClickListener(v -> {
            if (!interactive || callback == null) {
                return;
            }
            callback.onTap(v);
            callback.onAnswer(new ChoiceAnswer(index));
        });
        return card;
    }

    private void disable(MaterialCardView card, boolean animate) {
        card.setEnabled(false);
        card.setClickable(false);
        if (animate) {
            card.animate().alpha(0.3f).setDuration(250).start();
        } else {
            card.setAlpha(0.3f);
        }
    }

    private MaterialCardView cardFor(Answer answer) {
        if (!(answer instanceof ChoiceAnswer)) {
            return null;
        }
        int i = ((ChoiceAnswer) answer).getIndex();
        return i >= 0 && i < cards.size() ? cards.get(i) : null;
    }

    @Override
    public void showCorrect(Answer answer) {
        interactive = false;
        MaterialCardView card = cardFor(answer);
        if (card != null) {
            highlight(card, R.color.ob_success, R.color.ob_success_soft);
            Anim.pop(card);
        }
    }

    @Override
    public void showWrong(Answer answer, boolean willRetry) {
        MaterialCardView card = cardFor(answer);
        if (card == null) {
            return;
        }
        highlight(card, R.color.ob_error, R.color.ob_error_soft);
        Anim.shake(card);
        if (willRetry) {
            disable(card, true);
        } else {
            interactive = false;
        }
    }

    @Override
    public void revealSolution() {
        interactive = false;
        MaterialCardView card = cards.get(puzzle.getCorrectIndex());
        card.setAlpha(1f);
        highlight(card, R.color.ob_success, R.color.ob_success_soft);
        Anim.pop(card);
    }

    private void highlight(MaterialCardView card, int strokeColor, int fillColor) {
        Context ctx = card.getContext();
        card.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(ctx, strokeColor)));
        card.setStrokeWidth((int) (2.5f * density));
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, fillColor));
    }

    @Override
    public void applyHint(Hint hint) {
        if (hint.getKind() == Hint.Kind.ELIMINATE_OPTION && hint.getValue() < cards.size()) {
            disable(cards.get(hint.getValue()), true);
        }
    }

    @Override
    public void release() {
        for (MaterialCardView c : cards) {
            c.animate().cancel();
            c.setOnClickListener(null);
        }
        cards.clear();
        callback = null;
    }
}
