package com.onebrain.game.player;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.data.Persistable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * All progression statistics: totals, streaks, best scores and per-category
 * performance. Mutated only through intention-revealing methods.
 */
public final class PlayerStats implements Persistable {

    private static final int HISTORY_DAYS = 60;

    private int puzzlesPlayed;
    private int puzzlesSolved;
    private int totalMistakes;
    private int threeStarCount;
    private long totalSolveTimeMs;
    private long fastestSolveMs;
    private int flawlessRun;
    private int bestFlawlessRun;
    private final int[] solvedByDifficulty = new int[Difficulty.values().length];

    // Daily / streaks
    private int currentStreak;
    private int longestStreak;
    private int dailyPlayed;
    private int dailySolved;
    private int streakFreezesUsed;
    private long lastDailySolvedDay = -1L;
    private long lastDailyPlayedDay = -1L;
    private final TreeMap<Long, Integer> dailyHistory = new TreeMap<>();

    // In-progress daily (so leaving the app doesn't reset the timer)
    private long dailyProgressDay = -1L;
    private long dailyProgressElapsedMs;
    private int dailyProgressMistakes;

    // Modes
    private final Map<GameMode, Integer> bestScores = new EnumMap<>(GameMode.class);
    private int bestEndlessRun;
    private int bestTimeAttackRun;
    private int weeklyCompleted;
    private int weeklyKey;
    private int weeklyBestScore;
    private boolean weeklyCompletedThisWeek;

    private final Map<PuzzleCategory, CategoryStats> categories = new EnumMap<>(PuzzleCategory.class);
    private final ChallengeProgress challenge = new ChallengeProgress();

    public PlayerStats() {
        for (PuzzleCategory c : PuzzleCategory.values()) {
            categories.put(c, new CategoryStats());
        }
    }

    // ---------------------------------------------------------------- puzzles

    public void recordPuzzle(PuzzleCategory category, Difficulty difficulty, boolean correct,
                             int mistakes, long timeMs, int stars, int score) {
        puzzlesPlayed++;
        totalMistakes += mistakes;
        categories.get(category).record(difficulty, correct, timeMs, score);
        if (correct) {
            puzzlesSolved++;
            totalSolveTimeMs += timeMs;
            solvedByDifficulty[difficulty.ordinal()]++;
            if (fastestSolveMs == 0L || timeMs < fastestSolveMs) {
                fastestSolveMs = timeMs;
            }
            if (stars >= 3) {
                threeStarCount++;
            }
        }
        if (correct && mistakes == 0) {
            flawlessRun++;
            bestFlawlessRun = Math.max(bestFlawlessRun, flawlessRun);
        } else {
            flawlessRun = 0;
        }
    }

    /** @return {@code true} if this is a new personal best for the mode. */
    public boolean submitBestScore(GameMode mode, int score) {
        Integer best = bestScores.get(mode);
        if (best == null || score > best) {
            bestScores.put(mode, score);
            return best != null || score > 0;
        }
        return false;
    }

    public void submitRun(GameMode mode, int solvedInRun) {
        if (mode == GameMode.ENDLESS) {
            bestEndlessRun = Math.max(bestEndlessRun, solvedInRun);
        } else if (mode == GameMode.TIME_ATTACK) {
            bestTimeAttackRun = Math.max(bestTimeAttackRun, solvedInRun);
        }
    }

    // ---------------------------------------------------------------- daily

    /**
     * Applies missed days before {@code today}: spends streak freezes if the player
     * owns enough, otherwise breaks the streak.
     */
    public void refreshStreak(long today, Inventory inventory) {
        if (currentStreak == 0 || lastDailySolvedDay < 0) {
            return;
        }
        long gap = today - lastDailySolvedDay;
        if (gap <= 1) {
            return;
        }
        int missed = (int) Math.min(Integer.MAX_VALUE, gap - 1);
        if (inventory != null && inventory.getStreakFreezes() >= missed) {
            inventory.useStreakFreezes(missed);
            streakFreezesUsed += missed;
            lastDailySolvedDay = today - 1;
        } else {
            currentStreak = 0;
        }
    }

    public void registerDaily(long day, boolean solved, int stars) {
        if (day <= lastDailyPlayedDay) {
            return; // already counted
        }
        lastDailyPlayedDay = day;
        dailyPlayed++;
        dailyHistory.put(day, solved ? Math.max(1, stars) : 0);
        while (dailyHistory.size() > HISTORY_DAYS) {
            dailyHistory.pollFirstEntry();
        }
        if (solved) {
            dailySolved++;
            currentStreak = lastDailySolvedDay == day - 1 ? currentStreak + 1 : 1;
            lastDailySolvedDay = day;
            longestStreak = Math.max(longestStreak, currentStreak);
        } else {
            currentStreak = 0;
        }
        clearDailyProgress();
    }

    /** Streak as it should be displayed today (0 if it already lapsed). */
    public int getDisplayStreak(long today, Inventory inventory) {
        if (currentStreak == 0 || lastDailySolvedDay < 0) {
            return 0;
        }
        long missed = today - lastDailySolvedDay - 1;
        if (missed <= 0) {
            return currentStreak;
        }
        return inventory != null && inventory.getStreakFreezes() >= missed ? currentStreak : 0;
    }

    public boolean hasPlayedDaily(long day) {
        return lastDailyPlayedDay == day || dailyHistory.containsKey(day);
    }

    /** Stars for that day (0 = failed), or {@code null} if not played. */
    public Integer getDailyResult(long day) {
        return dailyHistory.get(day);
    }

    public void saveDailyProgress(long day, long elapsedMs, int mistakes) {
        dailyProgressDay = day;
        dailyProgressElapsedMs = elapsedMs;
        dailyProgressMistakes = mistakes;
    }

    public long getDailyProgressElapsed(long day) {
        return dailyProgressDay == day ? dailyProgressElapsedMs : 0L;
    }

    public int getDailyProgressMistakes(long day) {
        return dailyProgressDay == day ? dailyProgressMistakes : 0;
    }

    public boolean hasDailyInProgress(long day) {
        return dailyProgressDay == day;
    }

    public void clearDailyProgress() {
        dailyProgressDay = -1L;
        dailyProgressElapsedMs = 0L;
        dailyProgressMistakes = 0;
    }

    // ---------------------------------------------------------------- weekly

    public void recordWeekly(int week, int score, boolean completed) {
        if (week != weeklyKey) {
            weeklyKey = week;
            weeklyBestScore = 0;
            weeklyCompletedThisWeek = false;
        }
        weeklyBestScore = Math.max(weeklyBestScore, score);
        if (completed && !weeklyCompletedThisWeek) {
            weeklyCompletedThisWeek = true;
            weeklyCompleted++;
        }
    }

    public int getWeeklyBestScore(int week) {
        return week == weeklyKey ? weeklyBestScore : 0;
    }

    public boolean isWeeklyCompleted(int week) {
        return week == weeklyKey && weeklyCompletedThisWeek;
    }

    // ---------------------------------------------------------------- getters

    public int getPuzzlesPlayed() {
        return puzzlesPlayed;
    }

    public int getTotalSolved() {
        return puzzlesSolved;
    }

    public int getTotalMistakes() {
        return totalMistakes;
    }

    public float getAccuracy() {
        return puzzlesPlayed == 0 ? 0f : puzzlesSolved / (float) puzzlesPlayed;
    }

    public long getAverageSolveTimeMs() {
        return puzzlesSolved == 0 ? 0L : totalSolveTimeMs / puzzlesSolved;
    }

    public long getFastestSolveMs() {
        return fastestSolveMs;
    }

    public int getThreeStarCount() {
        return threeStarCount;
    }

    public int getFlawlessRun() {
        return flawlessRun;
    }

    public int getBestFlawlessRun() {
        return bestFlawlessRun;
    }

    public int getSolvedAtDifficulty(Difficulty d) {
        return solvedByDifficulty[d.ordinal()];
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public int getDailyPlayed() {
        return dailyPlayed;
    }

    public int getDailySolved() {
        return dailySolved;
    }

    public int getStreakFreezesUsed() {
        return streakFreezesUsed;
    }

    public long getLastDailyPlayedDay() {
        return lastDailyPlayedDay;
    }

    public long getLastDailySolvedDay() {
        return lastDailySolvedDay;
    }

    public int getBestScore(GameMode mode) {
        Integer v = bestScores.get(mode);
        return v == null ? 0 : v;
    }

    public int getBestEndlessRun() {
        return bestEndlessRun;
    }

    public int getBestTimeAttackRun() {
        return bestTimeAttackRun;
    }

    public int getWeeklyCompleted() {
        return weeklyCompleted;
    }

    public CategoryStats getCategoryStats(PuzzleCategory category) {
        return categories.get(category);
    }

    public ChallengeProgress getChallenge() {
        return challenge;
    }

    public int getChallengeLevelsCleared() {
        return challenge.getClearedCount();
    }

    public int getChallengeStars() {
        return challenge.getTotalStars();
    }

    public int getCategoriesWithSolve() {
        int count = 0;
        for (CategoryStats s : categories.values()) {
            if (s.getSolved() > 0) {
                count++;
            }
        }
        return count;
    }

    void reset() {
        fromJson(new JSONObject());
    }

    // ---------------------------------------------------------------- persistence

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("played", puzzlesPlayed);
        o.put("solved", puzzlesSolved);
        o.put("mistakes", totalMistakes);
        o.put("threeStars", threeStarCount);
        o.put("solveTime", totalSolveTimeMs);
        o.put("fastest", fastestSolveMs);
        o.put("flawless", flawlessRun);
        o.put("bestFlawless", bestFlawlessRun);
        JSONArray byDiff = new JSONArray();
        for (int v : solvedByDifficulty) {
            byDiff.put(v);
        }
        o.put("solvedByDifficulty", byDiff);

        o.put("streak", currentStreak);
        o.put("longestStreak", longestStreak);
        o.put("dailyPlayed", dailyPlayed);
        o.put("dailySolved", dailySolved);
        o.put("freezesUsed", streakFreezesUsed);
        o.put("lastDailySolved", lastDailySolvedDay);
        o.put("lastDailyPlayed", lastDailyPlayedDay);
        JSONObject history = new JSONObject();
        for (Map.Entry<Long, Integer> e : dailyHistory.entrySet()) {
            history.put(String.valueOf(e.getKey()), e.getValue());
        }
        o.put("dailyHistory", history);
        o.put("dailyProgressDay", dailyProgressDay);
        o.put("dailyProgressElapsed", dailyProgressElapsedMs);
        o.put("dailyProgressMistakes", dailyProgressMistakes);

        JSONObject best = new JSONObject();
        for (Map.Entry<GameMode, Integer> e : bestScores.entrySet()) {
            best.put(e.getKey().name(), e.getValue());
        }
        o.put("bestScores", best);
        o.put("bestEndless", bestEndlessRun);
        o.put("bestTimeAttack", bestTimeAttackRun);
        o.put("weeklyCompleted", weeklyCompleted);
        o.put("weeklyKey", weeklyKey);
        o.put("weeklyBest", weeklyBestScore);
        o.put("weeklyDone", weeklyCompletedThisWeek);

        JSONObject cats = new JSONObject();
        for (Map.Entry<PuzzleCategory, CategoryStats> e : categories.entrySet()) {
            cats.put(e.getKey().name(), e.getValue().toJson());
        }
        o.put("categories", cats);
        o.put("challenge", challenge.toJson());
        return o;
    }

    @Override
    public void fromJson(JSONObject o) {
        puzzlesPlayed = o.optInt("played", 0);
        puzzlesSolved = o.optInt("solved", 0);
        totalMistakes = o.optInt("mistakes", 0);
        threeStarCount = o.optInt("threeStars", 0);
        totalSolveTimeMs = o.optLong("solveTime", 0L);
        fastestSolveMs = o.optLong("fastest", 0L);
        flawlessRun = o.optInt("flawless", 0);
        bestFlawlessRun = o.optInt("bestFlawless", 0);
        JSONArray byDiff = o.optJSONArray("solvedByDifficulty");
        for (int i = 0; i < solvedByDifficulty.length; i++) {
            solvedByDifficulty[i] = byDiff != null ? byDiff.optInt(i, 0) : 0;
        }

        currentStreak = o.optInt("streak", 0);
        longestStreak = o.optInt("longestStreak", 0);
        dailyPlayed = o.optInt("dailyPlayed", 0);
        dailySolved = o.optInt("dailySolved", 0);
        streakFreezesUsed = o.optInt("freezesUsed", 0);
        lastDailySolvedDay = o.optLong("lastDailySolved", -1L);
        lastDailyPlayedDay = o.optLong("lastDailyPlayed", -1L);
        dailyHistory.clear();
        JSONObject history = o.optJSONObject("dailyHistory");
        if (history != null) {
            Iterator<String> keys = history.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                try {
                    dailyHistory.put(Long.parseLong(k), history.optInt(k, 0));
                } catch (NumberFormatException ignored) {
                    // skip corrupt entry
                }
            }
        }
        dailyProgressDay = o.optLong("dailyProgressDay", -1L);
        dailyProgressElapsedMs = o.optLong("dailyProgressElapsed", 0L);
        dailyProgressMistakes = o.optInt("dailyProgressMistakes", 0);

        bestScores.clear();
        JSONObject best = o.optJSONObject("bestScores");
        if (best != null) {
            for (GameMode mode : GameMode.values()) {
                if (best.has(mode.name())) {
                    bestScores.put(mode, best.optInt(mode.name(), 0));
                }
            }
        }
        bestEndlessRun = o.optInt("bestEndless", 0);
        bestTimeAttackRun = o.optInt("bestTimeAttack", 0);
        weeklyCompleted = o.optInt("weeklyCompleted", 0);
        weeklyKey = o.optInt("weeklyKey", 0);
        weeklyBestScore = o.optInt("weeklyBest", 0);
        weeklyCompletedThisWeek = o.optBoolean("weeklyDone", false);

        JSONObject cats = o.optJSONObject("categories");
        for (PuzzleCategory c : PuzzleCategory.values()) {
            CategoryStats stats = new CategoryStats();
            JSONObject json = cats != null ? cats.optJSONObject(c.name()) : null;
            if (json != null) {
                stats.fromJson(json);
            }
            categories.put(c, stats);
        }
        JSONObject path = o.optJSONObject("challenge");
        challenge.fromJson(path != null ? path : new JSONObject());
    }
}
