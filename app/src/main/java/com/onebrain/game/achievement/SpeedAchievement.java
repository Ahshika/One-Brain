package com.onebrain.game.achievement;

import com.onebrain.game.player.PlayerStats;

/** Solve any puzzle faster than a threshold. */
public final class SpeedAchievement extends Achievement {

    private final long thresholdMs;

    public SpeedAchievement(String id, String icon, long thresholdMs, int rewardCoins) {
        super(id, icon, 1, rewardCoins);
        this.thresholdMs = thresholdMs;
    }

    @Override
    protected int measure(PlayerStats stats) {
        long fastest = stats.getFastestSolveMs();
        return fastest > 0 && fastest < thresholdMs ? 1 : 0;
    }

    public long getThresholdMs() {
        return thresholdMs;
    }
}
