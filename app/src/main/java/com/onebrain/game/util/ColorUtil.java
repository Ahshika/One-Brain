package com.onebrain.game.util;

/**
 * Pure-Java HSV to ARGB conversion so puzzle generation stays free of Android
 * dependencies (and therefore unit-testable and identical on every device).
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    public static int hsv(float hue, float saturation, float value) {
        float h = ((hue % 360f) + 360f) % 360f;
        float s = clamp(saturation);
        float v = clamp(value);
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = v - c;
        float r;
        float g;
        float b;
        if (h < 60) {
            r = c; g = x; b = 0;
        } else if (h < 120) {
            r = x; g = c; b = 0;
        } else if (h < 180) {
            r = 0; g = c; b = x;
        } else if (h < 240) {
            r = 0; g = x; b = c;
        } else if (h < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }
        int ri = Math.round((r + m) * 255);
        int gi = Math.round((g + m) * 255);
        int bi = Math.round((b + m) * 255);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    private static float clamp(float f) {
        return Math.max(0f, Math.min(1f, f));
    }
}
