package com.onebrain.game.ui.game.render;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.onebrain.game.R;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.WordPuzzle;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.TextAnswer;
import com.onebrain.game.ui.common.Anim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Anagram input: tap tiles to fill slots; tap a slot to return its letter. */
public final class WordRenderer implements PuzzleRenderer {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WordPuzzle puzzle;
    private Callback callback;
    private char[] letters;
    private TextView[] slots;
    private TextView[] tiles;
    private int[] slotTile;
    private boolean[] slotLocked;
    private LinearLayout slotRow;
    private LinearLayout tileRow;
    private boolean interactive = true;

    @Override
    public void bind(Puzzle p, ViewGroup visualContainer, ViewGroup inputContainer,
                     boolean resumeInteractive, Callback cb) {
        puzzle = (WordPuzzle) p;
        callback = cb;
        Context ctx = visualContainer.getContext();
        float density = ctx.getResources().getDisplayMetrics().density;
        int n = puzzle.getLength();
        letters = puzzle.getLetters();
        slots = new TextView[n];
        tiles = new TextView[n];
        slotTile = new int[n];
        slotLocked = new boolean[n];

        int screen = ctx.getResources().getDisplayMetrics().widthPixels;
        int margin = (int) ((n >= 8 ? 2 : 4) * density);
        int size = (int) Math.min(54 * density, (screen - 48 * density) / n - 2 * margin);

        slotRow = row(ctx);
        for (int i = 0; i < n; i++) {
            final int index = i;
            slotTile[i] = -1;
            TextView slot = letterView(ctx, size);
            slot.setBackgroundResource(R.drawable.bg_slot);
            slot.setOnClickListener(v -> onSlotClick(index));
            slotRow.addView(slot, params(size, margin));
            slots[i] = slot;
        }
        visualContainer.addView(slotRow, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

        LinearLayout column = new LinearLayout(ctx);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        tileRow = row(ctx);
        for (int j = 0; j < n; j++) {
            final int index = j;
            TextView tile = letterView(ctx, size);
            tile.setText(String.valueOf(letters[j]));
            tile.setBackgroundResource(R.drawable.bg_tile);
            tile.setOnClickListener(v -> onTileClick(index));
            tile.setStateListAnimator(android.animation.AnimatorInflater.loadStateListAnimator(ctx, R.animator.press_scale));
            tileRow.addView(tile, params(size, margin));
            tiles[j] = tile;
        }
        column.addView(tileRow);

        LinearLayout controls = row(ctx);
        controls.addView(textButton(ctx, R.string.word_shuffle, v -> shuffle()));
        controls.addView(textButton(ctx, R.string.word_clear, v -> clearUnlocked()));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = (int) (12 * density);
        column.addView(controls, clp);
        inputContainer.addView(column, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int pos = 0; pos < puzzle.getRevealedCount(); pos++) {
            placeHint(pos);
        }
        cb.onReady();
    }

    private static LinearLayout row(Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        return row;
    }

    private static LinearLayout.LayoutParams params(int size, int margin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(margin, margin, margin, margin);
        return lp;
    }

    private static TextView letterView(Context ctx, int size) {
        TextView tv = new TextView(ctx);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.48f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(ContextCompat.getColor(ctx, R.color.ob_text));
        tv.setIncludeFontPadding(false);
        return tv;
    }

    private static MaterialButton textButton(Context ctx, int text, View.OnClickListener l) {
        MaterialButton b = new MaterialButton(ctx, null, com.google.android.material.R.attr.borderlessButtonStyle);
        b.setText(text);
        b.setOnClickListener(l);
        return b;
    }

    private void onTileClick(int tile) {
        if (!interactive || tiles[tile].getVisibility() != View.VISIBLE) {
            return;
        }
        int slot = firstEmptySlot();
        if (slot < 0) {
            return;
        }
        fill(slot, tile);
        if (callback != null) {
            callback.onTap(tiles[tile]);
        }
        submitIfFull();
    }

    private void onSlotClick(int slot) {
        if (!interactive || slotLocked[slot] || slotTile[slot] < 0) {
            return;
        }
        unfill(slot);
        if (callback != null) {
            callback.onTap(slots[slot]);
        }
    }

    private int firstEmptySlot() {
        for (int i = 0; i < slots.length; i++) {
            if (slotTile[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    private void fill(int slot, int tile) {
        slotTile[slot] = tile;
        slots[slot].setText(String.valueOf(letters[tile]));
        tiles[tile].setVisibility(View.INVISIBLE);
        Anim.pop(slots[slot]);
    }

    private void unfill(int slot) {
        int tile = slotTile[slot];
        if (tile >= 0) {
            tiles[tile].setVisibility(View.VISIBLE);
            Anim.pop(tiles[tile]);
        }
        slotTile[slot] = -1;
        slots[slot].setText("");
    }

    private void submitIfFull() {
        if (firstEmptySlot() >= 0 || callback == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int t : slotTile) {
            sb.append(letters[t]);
        }
        interactive = false;
        callback.onAnswer(new TextAnswer(sb.toString()));
    }

    private void clearUnlocked() {
        if (!interactive) {
            return;
        }
        for (int i = 0; i < slots.length; i++) {
            if (!slotLocked[i]) {
                unfill(i);
            }
        }
    }

    private void shuffle() {
        if (!interactive) {
            return;
        }
        List<TextView> order = new ArrayList<>();
        Collections.addAll(order, tiles);
        Collections.shuffle(order);
        tileRow.removeAllViews();
        for (TextView t : order) {
            tileRow.addView(t);
        }
    }

    /** Puts the correct letter at {@code pos} and locks it. */
    private void placeHint(int pos) {
        char target = puzzle.getAnswerLetterAt(pos);
        if (slotTile[pos] >= 0) {
            unfill(pos);
        }
        int tile = -1;
        for (int j = 0; j < tiles.length; j++) {
            if (letters[j] == target && tiles[j].getVisibility() == View.VISIBLE) {
                tile = j;
                break;
            }
        }
        if (tile < 0) {
            // The letter is sitting in another unlocked slot: take it back.
            for (int s = 0; s < slots.length; s++) {
                if (!slotLocked[s] && slotTile[s] >= 0 && letters[slotTile[s]] == target) {
                    tile = slotTile[s];
                    unfill(s);
                    break;
                }
            }
        }
        if (tile < 0) {
            return;
        }
        fill(pos, tile);
        slotLocked[pos] = true;
        slots[pos].setBackgroundResource(R.drawable.bg_slot_locked);
    }

    @Override
    public void showCorrect(Answer answer) {
        interactive = false;
        for (int i = 0; i < slots.length; i++) {
            slots[i].setBackgroundResource(R.drawable.bg_slot_correct);
            final TextView s = slots[i];
            handler.postDelayed(() -> Anim.pop(s), i * 60L);
        }
    }

    @Override
    public void showWrong(Answer answer, boolean willRetry) {
        for (int i = 0; i < slots.length; i++) {
            if (!slotLocked[i]) {
                slots[i].setBackgroundResource(R.drawable.bg_slot_wrong);
            }
        }
        Anim.shake(slotRow);
        if (willRetry) {
            handler.postDelayed(() -> {
                for (int i = 0; i < slots.length; i++) {
                    if (!slotLocked[i]) {
                        slots[i].setBackgroundResource(R.drawable.bg_slot);
                    }
                }
                interactive = true;
                clearUnlocked();
            }, 650);
        }
    }

    @Override
    public void revealSolution() {
        interactive = false;
        String answer = puzzle.getAnswer();
        for (int i = 0; i < slots.length; i++) {
            slots[i].setText(String.valueOf(answer.charAt(i)));
            slots[i].setBackgroundResource(R.drawable.bg_slot_correct);
        }
        for (TextView t : tiles) {
            t.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void applyHint(Hint hint) {
        if (hint.getKind() == Hint.Kind.REVEAL_LETTER) {
            placeHint(hint.getValue());
            submitIfFull();
        }
    }

    @Override
    public void release() {
        handler.removeCallbacksAndMessages(null);
        callback = null;
    }
}
