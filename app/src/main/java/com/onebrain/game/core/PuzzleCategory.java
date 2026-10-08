package com.onebrain.game.core;

public enum PuzzleCategory {
    LOGIC,
    MATH,
    MEMORY,
    PATTERN,
    VISUAL,
    SEQUENCE,
    SPATIAL,
    WORD;

    public static PuzzleCategory fromName(String name, PuzzleCategory fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
