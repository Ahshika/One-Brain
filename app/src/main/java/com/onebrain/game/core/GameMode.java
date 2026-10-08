package com.onebrain.game.core;

public enum GameMode {
    DAILY,
    PRACTICE,
    ENDLESS,
    TIME_ATTACK,
    WEEKLY,
    PACK,
    CHALLENGE;

    public static GameMode fromName(String name, GameMode fallback) {
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
