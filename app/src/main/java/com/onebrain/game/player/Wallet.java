package com.onebrain.game.player;

import com.onebrain.game.data.Persistable;

import org.json.JSONException;
import org.json.JSONObject;

/** In-game soft currency (coins). Earned by playing, spent on hints and unlocks. */
public final class Wallet implements Persistable {

    public static final int STARTING_COINS = 100;

    private int coins = STARTING_COINS;
    private long lifetimeEarned;

    public int getCoins() {
        return coins;
    }

    public long getLifetimeEarned() {
        return lifetimeEarned;
    }

    public void add(int amount) {
        if (amount <= 0) {
            return;
        }
        coins += amount;
        lifetimeEarned += amount;
    }

    public boolean canAfford(int amount) {
        return coins >= amount;
    }

    /** @return {@code true} if the coins were deducted. */
    public boolean spend(int amount) {
        if (amount < 0 || coins < amount) {
            return false;
        }
        coins -= amount;
        return true;
    }

    void reset() {
        coins = STARTING_COINS;
        lifetimeEarned = 0;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return new JSONObject().put("coins", coins).put("lifetime", lifetimeEarned);
    }

    @Override
    public void fromJson(JSONObject json) {
        coins = Math.max(0, json.optInt("coins", STARTING_COINS));
        lifetimeEarned = json.optLong("lifetime", 0L);
    }
}
