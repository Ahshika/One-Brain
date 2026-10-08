package com.onebrain.game.achievement;

import com.onebrain.game.player.PlayerStats;

/**
 * Base class for achievements. Subclasses decide how progress is measured;
 * the UI shows {@code progress / target}. Text is resolved by id in the UI layer.
 */
public abstract class Achievement {

    private final String id;
    private final String icon;
    private final int target;
    private final int rewardCoins;

    protected Achievement(String id, String icon, int target, int rewardCoins) {
        this.id = id;
        this.icon = icon;
        this.target = Math.max(1, target);
        this.rewardCoins = rewardCoins;
    }

    /** Raw progress measured from the stats (may exceed the target). */
    protected abstract int measure(PlayerStats stats);

    public final int getProgress(PlayerStats stats) {
        return Math.min(target, Math.max(0, measure(stats)));
    }

    public final boolean isCompleted(PlayerStats stats) {
        return measure(stats) >= target;
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public int getTarget() {
        return target;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }
}
