package com.onebrain.game.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.onebrain.game.player.Player;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Offline persistence. The whole profile is a small JSON document stored in
 * SharedPreferences and written asynchronously with {@code apply()}, which is
 * fast, crash-safe and included in Android Auto Backup.
 */
public final class SaveManager {

    private static final String TAG = "SaveManager";
    private static final String PREFS = "onebrain_save";
    private static final String KEY_PLAYER = "player";
    private static final String KEY_ACHIEVEMENTS = "achievements";

    private final SharedPreferences prefs;

    public SaveManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void loadPlayer(Player player) {
        String raw = prefs.getString(KEY_PLAYER, null);
        if (raw == null) {
            return;
        }
        try {
            player.fromJson(new JSONObject(raw));
        } catch (JSONException e) {
            Log.e(TAG, "Corrupt save file, starting fresh", e);
        }
    }

    public void savePlayer(Player player) {
        try {
            prefs.edit().putString(KEY_PLAYER, player.toJson().toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Unable to serialize player", e);
        }
    }

    public Set<String> loadAchievements() {
        return new HashSet<>(prefs.getStringSet(KEY_ACHIEVEMENTS, Collections.emptySet()));
    }

    public void saveAchievements(Set<String> unlocked) {
        prefs.edit().putStringSet(KEY_ACHIEVEMENTS, new HashSet<>(unlocked)).apply();
    }
}
