package com.onebrain.game.ui.game.render;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.onebrain.game.R;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.visual.GridVisual;
import com.onebrain.game.puzzle.visual.LinesVisual;
import com.onebrain.game.puzzle.visual.TextVisual;
import com.onebrain.game.puzzle.visual.VisualVisitor;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.ui.view.GridBoardView;

/** Visitor turning model {@code Visual}s into Android views. */
public final class VisualViewFactory implements VisualVisitor<View> {

    private final Context context;
    private final float density;

    public VisualViewFactory(Context context) {
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public View visitText(TextVisual visual) {
        TextView tv = new TextView(context);
        String text = visual.getText();
        boolean multiline = text.indexOf('\n') >= 0;
        int longest = 0;
        for (String line : text.split("\n")) {
            longest = Math.max(longest, line.length());
        }
        float sp = multiline ? (longest > 22 ? 20f : 24f) : (longest > 24 ? 22f : longest > 16 ? 26f : 32f);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        tv.setTextColor(ContextCompat.getColor(context, R.color.ob_text));
        tv.setGravity(Gravity.CENTER);
        tv.setLineSpacing(0f, 1.35f);
        tv.setTextDirection(View.TEXT_DIRECTION_LTR);
        tv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        int pad = (int) (20 * density);
        tv.setPadding(pad, pad, pad, pad);
        tv.setBackgroundResource(R.drawable.bg_panel);
        tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return tv;
    }

    @Override
    public View visitLines(LinesVisual visual) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * density);
        column.setPadding(pad, pad, pad, pad);
        column.setBackgroundResource(R.drawable.bg_panel);
        for (Prompt line : visual.getLines()) {
            TextView tv = new TextView(context);
            tv.setText(UiText.format(context, line));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f);
            tv.setTextColor(ContextCompat.getColor(context, R.color.ob_text));
            tv.setGravity(Gravity.CENTER);
            int v = (int) (6 * density);
            tv.setPadding(0, v, 0, v);
            column.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        column.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return column;
    }

    @Override
    public View visitGrid(GridVisual visual) {
        GridBoardView board = new GridBoardView(context);
        board.setGrid(visual);
        board.setMaxSize(context.getResources().getDimensionPixelSize(R.dimen.board_max) * 3 / 4);
        board.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        board.setLayoutParams(lp);
        return board;
    }
}
