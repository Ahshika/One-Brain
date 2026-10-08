package com.onebrain.game.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onebrain.game.R;
import com.onebrain.game.databinding.ViewStatTileBinding;

/** Helpers to build simple sectioned screens (stats, shop) programmatically. */
public final class Sections {

    private Sections() {
    }

    public static void header(ViewGroup parent, int text) {
        Context ctx = parent.getContext();
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextAppearance(R.style.TextAppearance_OneBrain_Section);
        float d = ctx.getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (20 * d);
        lp.bottomMargin = (int) (6 * d);
        parent.addView(tv, lp);
    }

    /** Adds a row of two stat tiles. */
    public static void statRow(ViewGroup parent, String value1, int label1, String value2, int label2) {
        Context ctx = parent.getContext();
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(tile(row, value1, label1), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tile(row, value2, label2), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(row);
    }

    private static android.view.View tile(ViewGroup parent, String value, int label) {
        ViewStatTileBinding b = ViewStatTileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        b.textValue.setText(value);
        b.textLabel.setText(label);
        return b.getRoot();
    }
}
