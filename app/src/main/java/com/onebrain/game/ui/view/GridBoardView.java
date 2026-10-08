package com.onebrain.game.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.onebrain.game.R;
import com.onebrain.game.puzzle.visual.GridVisual;

import java.util.Arrays;

/**
 * Lightweight custom View that draws a {@link GridVisual} (rounded cells with
 * optional labels/colors) on a single Canvas. Used for memory boards, odd-one-out
 * grids, number matrices and spatial shapes. One View instead of dozens of child
 * views keeps layout cheap on low-end devices.
 */
public class GridBoardView extends View {

    public static final int STATE_NONE = 0;
    public static final int STATE_SELECTED = 1;
    public static final int STATE_CORRECT = 2;
    public static final int STATE_WRONG = 3;
    public static final int STATE_DIMMED = 4;
    public static final int STATE_LOCKED = 5;

    public interface OnCellTapListener {
        void onCellTap(int index);
    }

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float gap;
    private final int colorEmpty;
    private final int colorAccent;
    private final int colorCorrect;
    private final int colorWrong;
    private final int colorText;
    private final int colorGold;

    private GridVisual grid;
    private int[] states = new int[0];
    private boolean revealFills = true;
    private int maxSizePx;
    private OnCellTapListener listener;

    // geometry cache
    private float cell;
    private float originX;
    private float originY;

    private int popIndex = -1;
    private float popScale = 1f;
    private ValueAnimator popAnimator;

    public GridBoardView(Context context) {
        this(context, null);
    }

    public GridBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        gap = 6f * density;
        colorEmpty = ContextCompat.getColor(context, R.color.ob_cell_empty);
        colorAccent = ContextCompat.getColor(context, R.color.ob_cell_fill);
        colorCorrect = ContextCompat.getColor(context, R.color.ob_success);
        colorWrong = ContextCompat.getColor(context, R.color.ob_error);
        colorText = ContextCompat.getColor(context, R.color.ob_text);
        colorGold = ContextCompat.getColor(context, R.color.ob_gold);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(3f * density);
        ringPaint.setColor(colorGold);
        maxSizePx = (int) (320 * density);
    }

    public void setGrid(GridVisual grid) {
        this.grid = grid;
        this.states = new int[grid.size()];
        setContentDescription(null);
        requestLayout();
        invalidate();
    }

    public GridVisual getGrid() {
        return grid;
    }

    public void setMaxSize(int px) {
        maxSizePx = px;
        requestLayout();
    }

    /** When false, cells colored with the accent sentinel are drawn empty (memory recall). */
    public void setRevealFills(boolean reveal) {
        revealFills = reveal;
        invalidate();
    }

    public void setCellState(int index, int state) {
        if (index >= 0 && index < states.length) {
            states[index] = state;
            invalidate();
        }
    }

    public int getCellState(int index) {
        return index >= 0 && index < states.length ? states[index] : STATE_NONE;
    }

    public void clearStates() {
        Arrays.fill(states, STATE_NONE);
        invalidate();
    }

    public void setOnCellTapListener(OnCellTapListener listener) {
        this.listener = listener;
        setClickable(listener != null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = maxSizePx;
        }
        width = Math.min(width, maxSizePx + getPaddingLeft() + getPaddingRight());
        int rows = grid != null ? grid.getRows() : 1;
        int cols = grid != null ? grid.getCols() : 1;
        float inner = width - getPaddingLeft() - getPaddingRight();
        float cellSize = (inner - gap * (cols - 1)) / cols;
        int height = (int) Math.ceil(cellSize * rows + gap * (rows - 1)) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    private void computeGeometry() {
        int rows = grid.getRows();
        int cols = grid.getCols();
        float availW = getWidth() - getPaddingLeft() - getPaddingRight();
        float availH = getHeight() - getPaddingTop() - getPaddingBottom();
        float byW = (availW - gap * (cols - 1)) / cols;
        float byH = (availH - gap * (rows - 1)) / rows;
        cell = Math.max(1f, Math.min(byW, byH));
        float totalW = cell * cols + gap * (cols - 1);
        float totalH = cell * rows + gap * (rows - 1);
        originX = getPaddingLeft() + (availW - totalW) / 2f;
        originY = getPaddingTop() + (availH - totalH) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (grid == null) {
            return;
        }
        computeGeometry();
        int cols = grid.getCols();
        float radius = cell * 0.2f;
        for (int i = 0; i < grid.size(); i++) {
            float x = originX + (i % cols) * (cell + gap);
            float y = originY + (i / cols) * (cell + gap);
            float inset = i == popIndex ? cell * (1f - popScale) / 2f : 0f;
            rect.set(x + inset, y + inset, x + cell - inset, y + cell - inset);

            int base = baseColor(i);
            int state = states[i];
            int color;
            int alpha = 255;
            switch (state) {
                case STATE_SELECTED:
                case STATE_LOCKED:
                    color = colorAccent;
                    break;
                case STATE_CORRECT:
                    color = colorCorrect;
                    break;
                case STATE_WRONG:
                    color = colorWrong;
                    break;
                case STATE_DIMMED:
                    color = base;
                    alpha = 70;
                    break;
                default:
                    color = base;
                    break;
            }
            fillPaint.setColor(color);
            fillPaint.setAlpha(alpha);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
            if (state == STATE_LOCKED) {
                canvas.drawRoundRect(rect, radius, radius, ringPaint);
            }

            String label = grid.getLabel(i);
            if (label != null && !label.isEmpty()) {
                float size = cell * (label.length() > 2 ? 0.34f : label.length() == 2 ? 0.42f : 0.52f);
                textPaint.setTextSize(size);
                textPaint.setColor(color == colorEmpty ? colorText : Color.WHITE);
                textPaint.setAlpha(alpha);
                float cy = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f;
                canvas.drawText(label, rect.centerX(), cy, textPaint);
            }
        }
    }

    private int baseColor(int index) {
        int c = grid.getColor(index);
        if (c == GridVisual.COLOR_NONE) {
            return colorEmpty;
        }
        if (c == GridVisual.COLOR_ACCENT) {
            return revealFills ? colorAccent : colorEmpty;
        }
        return c;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (listener == null || !isEnabled() || grid == null) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int index = indexAt(event.getX(), event.getY());
            if (index >= 0) {
                pop(index);
                listener.onCellTap(index);
            }
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int indexAt(float x, float y) {
        computeGeometry();
        int cols = grid.getCols();
        int rows = grid.getRows();
        int col = (int) ((x - originX) / (cell + gap));
        int row = (int) ((y - originY) / (cell + gap));
        if (x < originX || y < originY || col < 0 || row < 0 || col >= cols || row >= rows) {
            return -1;
        }
        return row * cols + col;
    }

    private void pop(int index) {
        if (popAnimator != null) {
            popAnimator.cancel();
        }
        popIndex = index;
        popAnimator = ValueAnimator.ofFloat(0.82f, 1f);
        popAnimator.setDuration(200);
        popAnimator.setInterpolator(new OvershootInterpolator(2f));
        popAnimator.addUpdateListener(a -> {
            popScale = (float) a.getAnimatedValue();
            invalidate();
        });
        popAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (popAnimator != null) {
            popAnimator.cancel();
            popAnimator = null;
        }
        super.onDetachedFromWindow();
    }
}
