package com.onebrain.game.player;

import com.onebrain.game.data.Persistable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The player profile, composed of a {@link Wallet}, {@link PlayerStats} and an
 * {@link Inventory} (composition over inheritance).
 */
public final class Player implements Persistable {

    static final int SAVE_VERSION = 1;

    private final Wallet wallet = new Wallet();
    private final PlayerStats stats = new PlayerStats();
    private final Inventory inventory = new Inventory();

    public Wallet getWallet() {
        return wallet;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public Inventory getInventory() {
        return inventory;
    }

    /** Resets gameplay progress while keeping purchases. */
    public void resetProgress() {
        wallet.reset();
        stats.reset();
        inventory.resetProgress();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("version", SAVE_VERSION)
                .put("wallet", wallet.toJson())
                .put("stats", stats.toJson())
                .put("inventory", inventory.toJson());
    }

    @Override
    public void fromJson(JSONObject json) {
        JSONObject w = json.optJSONObject("wallet");
        if (w != null) {
            wallet.fromJson(w);
        }
        JSONObject s = json.optJSONObject("stats");
        if (s != null) {
            stats.fromJson(s);
        }
        JSONObject i = json.optJSONObject("inventory");
        if (i != null) {
            inventory.fromJson(i);
        }
    }
}
