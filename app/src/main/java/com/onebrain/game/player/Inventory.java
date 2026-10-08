package com.onebrain.game.player;

import com.onebrain.game.data.Persistable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Everything the player owns: purchased entitlements, puzzle packs, power-ups
 * and pack progress.
 */
public final class Inventory implements Persistable {

    public static final int MAX_STREAK_FREEZES = 3;
    public static final int MAX_FREE_COIN_ADS_PER_DAY = 5;

    private boolean adsRemoved;
    private boolean premium;
    private final Set<String> ownedPacks = new HashSet<>();
    private final Map<String, Integer> packProgress = new HashMap<>();
    private int streakFreezes;
    private long freeCoinsDay = -1L;
    private int freeCoinsCount;

    public boolean isAdsRemoved() {
        return adsRemoved;
    }

    public void setAdsRemoved(boolean adsRemoved) {
        this.adsRemoved = adsRemoved;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean ownsPack(String packId) {
        return ownedPacks.contains(packId);
    }

    public void addPack(String packId) {
        ownedPacks.add(packId);
    }

    public Set<String> getOwnedPacks() {
        return Collections.unmodifiableSet(ownedPacks);
    }

    /** Number of completed levels in a pack (= index of the next level to play). */
    public int getPackProgress(String packId) {
        Integer v = packProgress.get(packId);
        return v == null ? 0 : v;
    }

    public void setPackProgress(String packId, int completedLevels) {
        packProgress.put(packId, Math.max(getPackProgress(packId), completedLevels));
    }

    public int getStreakFreezes() {
        return streakFreezes;
    }

    public boolean addStreakFreeze() {
        if (streakFreezes >= MAX_STREAK_FREEZES) {
            return false;
        }
        streakFreezes++;
        return true;
    }

    public void useStreakFreezes(int count) {
        streakFreezes = Math.max(0, streakFreezes - count);
    }

    public int getFreeCoinAdsLeft(long today) {
        return today == freeCoinsDay ? Math.max(0, MAX_FREE_COIN_ADS_PER_DAY - freeCoinsCount)
                : MAX_FREE_COIN_ADS_PER_DAY;
    }

    public void registerFreeCoinAd(long today) {
        if (today != freeCoinsDay) {
            freeCoinsDay = today;
            freeCoinsCount = 0;
        }
        freeCoinsCount++;
    }

    /** Clears gameplay progress but keeps everything that was paid for. */
    void resetProgress() {
        packProgress.clear();
        streakFreezes = 0;
        freeCoinsDay = -1L;
        freeCoinsCount = 0;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject progress = new JSONObject();
        for (Map.Entry<String, Integer> e : packProgress.entrySet()) {
            progress.put(e.getKey(), e.getValue());
        }
        return new JSONObject()
                .put("adsRemoved", adsRemoved)
                .put("premium", premium)
                .put("packs", new JSONArray(ownedPacks))
                .put("packProgress", progress)
                .put("streakFreezes", streakFreezes)
                .put("freeCoinsDay", freeCoinsDay)
                .put("freeCoinsCount", freeCoinsCount);
    }

    @Override
    public void fromJson(JSONObject json) {
        adsRemoved = json.optBoolean("adsRemoved", false);
        premium = json.optBoolean("premium", false);
        ownedPacks.clear();
        JSONArray packs = json.optJSONArray("packs");
        if (packs != null) {
            for (int i = 0; i < packs.length(); i++) {
                String id = packs.optString(i, null);
                if (id != null) {
                    ownedPacks.add(id);
                }
            }
        }
        packProgress.clear();
        JSONObject progress = json.optJSONObject("packProgress");
        if (progress != null) {
            Iterator<String> keys = progress.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                packProgress.put(k, progress.optInt(k, 0));
            }
        }
        streakFreezes = Math.min(MAX_STREAK_FREEZES, Math.max(0, json.optInt("streakFreezes", 0)));
        freeCoinsDay = json.optLong("freeCoinsDay", -1L);
        freeCoinsCount = json.optInt("freeCoinsCount", 0);
    }
}
