package com.onebrain.game.util;

/**
 * Pausable stopwatch driven by an injectable monotonic clock. Pausing while the
 * app is in the background keeps solve times honest and avoids wasted ticks.
 */
public final class Stopwatch {

    public interface TimeSource {
        long now();
    }

    private final TimeSource source;
    private long accumulated;
    private long startedAt = -1L;

    public Stopwatch(TimeSource source) {
        this.source = source;
    }

    public void resume() {
        if (startedAt < 0) {
            startedAt = source.now();
        }
    }

    public void pause() {
        if (startedAt >= 0) {
            accumulated += source.now() - startedAt;
            startedAt = -1L;
        }
    }

    public void reset(long baseMillis) {
        accumulated = Math.max(0L, baseMillis);
        startedAt = -1L;
    }

    public boolean isRunning() {
        return startedAt >= 0;
    }

    public long elapsed() {
        return accumulated + (startedAt >= 0 ? source.now() - startedAt : 0L);
    }
}
