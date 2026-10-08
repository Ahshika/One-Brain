package com.onebrain.game.util;

/**
 * Deterministic seed mixing (SplitMix64). Used so that a given (base seed, index)
 * pair always produces the same puzzle on every device.
 */
public final class SeedUtil {

    private SeedUtil() {
    }

    public static long mix(long seed, long salt) {
        long z = seed + salt * 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public static long randomSeed() {
        return mix(System.nanoTime(), System.currentTimeMillis());
    }
}
