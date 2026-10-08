package com.onebrain.game.player;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.data.Persistable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Per-category performance, broken down by difficulty for adaptive difficulty. */
public final class CategoryStats implements Persistable {

    private static final int LEVELS = Difficulty.values().length;

    private final int[] played = new int[LEVELS];
    private final int[] solved = new int[LEVELS];
    private long totalSolveTimeMs;
    private int bestScore;

    void record(Difficulty difficulty, boolean correct, long timeMs, int score) {
        int i = difficulty.ordinal();
        played[i]++;
        if (correct) {
            solved[i]++;
            totalSolveTimeMs += timeMs;
            bestScore = Math.max(bestScore, score);
        }
    }

    public int getPlayed() {
        return sum(played);
    }

    public int getSolved() {
        return sum(solved);
    }

    public int getPlayed(Difficulty d) {
        return played[d.ordinal()];
    }

    public int getSolved(Difficulty d) {
        return solved[d.ordinal()];
    }

    public float getAccuracy() {
        int p = getPlayed();
        return p == 0 ? 0f : getSolved() / (float) p;
    }

    public float getAccuracy(Difficulty d) {
        int p = getPlayed(d);
        return p == 0 ? 0f : getSolved(d) / (float) p;
    }

    public long getAverageSolveTimeMs() {
        int s = getSolved();
        return s == 0 ? 0L : totalSolveTimeMs / s;
    }

    public int getBestScore() {
        return bestScore;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int v : values) {
            total += v;
        }
        return total;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONArray p = new JSONArray();
        JSONArray s = new JSONArray();
        for (int i = 0; i < LEVELS; i++) {
            p.put(played[i]);
            s.put(solved[i]);
        }
        return new JSONObject().put("played", p).put("solved", s)
                .put("time", totalSolveTimeMs).put("best", bestScore);
    }

    @Override
    public void fromJson(JSONObject json) {
        JSONArray p = json.optJSONArray("played");
        JSONArray s = json.optJSONArray("solved");
        for (int i = 0; i < LEVELS; i++) {
            played[i] = p != null ? p.optInt(i, 0) : 0;
            solved[i] = s != null ? s.optInt(i, 0) : 0;
        }
        totalSolveTimeMs = json.optLong("time", 0L);
        bestScore = json.optInt("best", 0);
    }
}
