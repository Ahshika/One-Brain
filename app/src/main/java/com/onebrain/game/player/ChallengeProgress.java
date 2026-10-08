package com.onebrain.game.player;

import com.onebrain.game.data.Persistable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/** Challenge Path progress: how far the player got and the best stars per level. */
public final class ChallengeProgress implements Persistable {

    /** Number of cleared levels = index of the highest unlocked level. */
    private int cleared;
    private final TreeMap<Integer, Integer> bestStars = new TreeMap<>();

    public int getClearedCount() {
        return cleared;
    }

    /** Highest level index the player may open. */
    public int getHighestUnlocked() {
        return cleared;
    }

    public boolean isUnlocked(int levelIndex) {
        return levelIndex <= cleared;
    }

    public int getStars(int levelIndex) {
        Integer s = bestStars.get(levelIndex);
        return s == null ? 0 : s;
    }

    public boolean isCleared(int levelIndex) {
        return bestStars.containsKey(levelIndex);
    }

    public int getTotalStars() {
        int total = 0;
        for (int s : bestStars.values()) {
            total += s;
        }
        return total;
    }

    /** @return {@code true} if this is the first time the level was cleared. */
    public boolean recordClear(int levelIndex, int stars) {
        boolean first = !bestStars.containsKey(levelIndex);
        bestStars.put(levelIndex, Math.max(Math.max(1, stars), getStars(levelIndex)));
        if (levelIndex >= cleared) {
            cleared = levelIndex + 1;
        }
        return first;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject stars = new JSONObject();
        for (Map.Entry<Integer, Integer> e : bestStars.entrySet()) {
            stars.put(String.valueOf(e.getKey()), e.getValue());
        }
        return new JSONObject().put("cleared", cleared).put("stars", stars);
    }

    @Override
    public void fromJson(JSONObject json) {
        cleared = Math.max(0, json.optInt("cleared", 0));
        bestStars.clear();
        JSONObject stars = json.optJSONObject("stars");
        if (stars != null) {
            Iterator<String> keys = stars.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                try {
                    bestStars.put(Integer.parseInt(k), stars.optInt(k, 1));
                } catch (NumberFormatException ignored) {
                    // skip corrupt entry
                }
            }
        }
    }
}
