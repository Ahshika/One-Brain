package com.onebrain.game.puzzle;

/**
 * The 8 symmetries of a square grid (dihedral group D4) applied to an n×n mask.
 */
public enum ShapeTransform {
    IDENTITY,
    ROTATE_90,
    ROTATE_180,
    ROTATE_270,
    MIRROR_H,
    MIRROR_V,
    ROTATE_90_MIRROR_H,
    ROTATE_90_MIRROR_V;

    public boolean[] apply(boolean[] cells, int n) {
        switch (this) {
            case IDENTITY:
                return cells.clone();
            case ROTATE_90:
                return rotate(cells, n);
            case ROTATE_180:
                return rotate(rotate(cells, n), n);
            case ROTATE_270:
                return rotate(rotate(rotate(cells, n), n), n);
            case MIRROR_H:
                return mirrorH(cells, n);
            case MIRROR_V:
                return mirrorV(cells, n);
            case ROTATE_90_MIRROR_H:
                return mirrorH(rotate(cells, n), n);
            case ROTATE_90_MIRROR_V:
                return mirrorV(rotate(cells, n), n);
            default:
                throw new IllegalStateException();
        }
    }

    /** Clockwise rotation. */
    static boolean[] rotate(boolean[] in, int n) {
        boolean[] out = new boolean[in.length];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                out[r * n + c] = in[(n - 1 - c) * n + r];
            }
        }
        return out;
    }

    /** Left ↔ right. */
    static boolean[] mirrorH(boolean[] in, int n) {
        boolean[] out = new boolean[in.length];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                out[r * n + c] = in[r * n + (n - 1 - c)];
            }
        }
        return out;
    }

    /** Upside down. */
    static boolean[] mirrorV(boolean[] in, int n) {
        boolean[] out = new boolean[in.length];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                out[r * n + c] = in[(n - 1 - r) * n + c];
            }
        }
        return out;
    }
}
