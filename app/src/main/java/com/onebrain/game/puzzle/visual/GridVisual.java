package com.onebrain.game.puzzle.visual;

import java.util.Arrays;

/**
 * A rows × cols board. Each cell has an optional label and a color. Colors are
 * ARGB values, or one of the theme sentinels {@link #COLOR_NONE} (empty cell) and
 * {@link #COLOR_ACCENT} (filled with the theme accent). Real opaque colors always
 * have a non-zero alpha byte, so the sentinels never collide with them.
 */
public final class GridVisual extends Visual {

    public static final int COLOR_NONE = 0;
    public static final int COLOR_ACCENT = 1;

    private final int rows;
    private final int cols;
    private final String[] labels;
    private final int[] colors;

    public GridVisual(int rows, int cols, String[] labels, int[] colors) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Grid must have at least one cell");
        }
        int size = rows * cols;
        if ((labels != null && labels.length != size) || (colors != null && colors.length != size)) {
            throw new IllegalArgumentException("Cell data does not match grid size");
        }
        this.rows = rows;
        this.cols = cols;
        this.labels = labels != null ? labels.clone() : null;
        this.colors = colors != null ? colors.clone() : null;
    }

    public static GridVisual ofMask(int rows, int cols, boolean[] mask) {
        int[] colors = new int[rows * cols];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = mask[i] ? COLOR_ACCENT : COLOR_NONE;
        }
        return new GridVisual(rows, cols, null, colors);
    }

    public static GridVisual ofLabels(int rows, int cols, String[] labels) {
        return new GridVisual(rows, cols, labels, null);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int size() {
        return rows * cols;
    }

    public String getLabel(int index) {
        return labels != null ? labels[index] : null;
    }

    public int getColor(int index) {
        return colors != null ? colors[index] : COLOR_NONE;
    }

    /** Content equality, used to guarantee answer options are distinct. */
    public boolean sameContent(GridVisual other) {
        return other != null && rows == other.rows && cols == other.cols
                && Arrays.equals(labels, other.labels) && Arrays.equals(colors, other.colors);
    }

    @Override
    public <R> R accept(VisualVisitor<R> visitor) {
        return visitor.visitGrid(this);
    }
}
