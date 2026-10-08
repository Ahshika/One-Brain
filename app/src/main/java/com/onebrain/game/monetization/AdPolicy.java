package com.onebrain.game.monetization;

/**
 * Keeps interstitials polite. An interstitial may only appear:
 * <ul>
 *   <li>between sessions (never during a puzzle),</li>
 *   <li>after at least {@link #SESSIONS_BETWEEN} completed sessions,</li>
 *   <li>at least {@link #MIN_INTERVAL_MS} after the previous one,</li>
 *   <li>never in the player's first sessions of the install.</li>
 * </ul>
 */
public final class AdPolicy {

    static final int SESSIONS_BETWEEN = 3;
    static final long MIN_INTERVAL_MS = 4 * 60_000L;
    static final int GRACE_SESSIONS = 2;

    private int sessionsSinceLast;
    private int totalSessions;
    private long lastShownAt = Long.MIN_VALUE / 2;

    public void onSessionCompleted() {
        sessionsSinceLast++;
        totalSessions++;
    }

    public boolean shouldShowInterstitial(long now) {
        return totalSessions > GRACE_SESSIONS
                && sessionsSinceLast >= SESSIONS_BETWEEN
                && now - lastShownAt >= MIN_INTERVAL_MS;
    }

    public void onInterstitialShown(long now) {
        sessionsSinceLast = 0;
        lastShownAt = now;
    }
}
