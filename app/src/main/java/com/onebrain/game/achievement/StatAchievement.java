package com.onebrain.game.achievement;

import com.onebrain.game.player.PlayerStats;

/** "Reach N of some counter" achievements, e.g. solve 30 puzzles. */
public final class StatAchievement extends Achievement {

    /** Reads one counter from the stats. */
    public interface StatReader {
        int read(PlayerStats stats);
    }

    private final StatReader reader;

    public StatAchievement(String id, String icon, int target, int rewardCoins, StatReader reader) {
        super(id, icon, target, rewardCoins);
        this.reader = reader;
    }

    @Override
    protected int measure(PlayerStats stats) {
        return reader.read(stats);
    }
}
