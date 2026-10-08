package com.onebrain.game.achievement;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.player.Wallet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Defines all achievements, tracks which are unlocked and pays out rewards. */
public final class AchievementManager {

    private final List<Achievement> achievements;
    private final Set<String> unlocked = new LinkedHashSet<>();

    public AchievementManager(Set<String> alreadyUnlocked) {
        this.achievements = Collections.unmodifiableList(createDefinitions());
        if (alreadyUnlocked != null) {
            unlocked.addAll(alreadyUnlocked);
        }
    }

    private static List<Achievement> createDefinitions() {
        List<Achievement> list = new ArrayList<>();
        list.add(new StatAchievement("first_solve", "🎯", 1, 20, PlayerStats::getTotalSolved));
        list.add(new StatAchievement("daily_7", "📅", 7, 100, PlayerStats::getDailySolved));
        list.add(new StatAchievement("daily_30", "🗓️", 30, 300, PlayerStats::getDailySolved));
        list.add(new StatAchievement("solved_30", "🧩", 30, 100, PlayerStats::getTotalSolved));
        list.add(new StatAchievement("solved_100", "🏅", 100, 250, PlayerStats::getTotalSolved));
        list.add(new StatAchievement("solved_500", "👑", 500, 750, PlayerStats::getTotalSolved));
        list.add(new StatAchievement("flawless_10", "💎", 10, 150, PlayerStats::getBestFlawlessRun));
        list.add(new SpeedAchievement("speed_10", "⚡", 10_000L, 100));
        list.add(new StatAchievement("streak_7", "🔥", 7, 150, PlayerStats::getLongestStreak));
        list.add(new StatAchievement("streak_30", "🌋", 30, 500, PlayerStats::getLongestStreak));
        list.add(new StatAchievement("stars_25", "⭐", 25, 150, PlayerStats::getThreeStarCount));
        list.add(new StatAchievement("all_categories", "🌈", PuzzleCategory.values().length, 150,
                PlayerStats::getCategoriesWithSolve));
        list.add(new StatAchievement("expert_10", "🧠", 10, 250,
                s -> s.getSolvedAtDifficulty(Difficulty.EXPERT)));
        list.add(new StatAchievement("endless_20", "♾️", 20, 200, PlayerStats::getBestEndlessRun));
        list.add(new StatAchievement("time_attack_15", "⏱️", 15, 200, PlayerStats::getBestTimeAttackRun));
        list.add(new StatAchievement("weekly_1", "🏆", 1, 150, PlayerStats::getWeeklyCompleted));
        list.add(new StatAchievement("path_10", "🏁", 10, 100, PlayerStats::getChallengeLevelsCleared));
        list.add(new StatAchievement("path_50", "⛰️", 50, 400, PlayerStats::getChallengeLevelsCleared));
        list.add(new StatAchievement("path_100", "👑", 100, 1000, PlayerStats::getChallengeLevelsCleared));
        list.add(new StatAchievement("path_stars_150", "🌟", 150, 300, PlayerStats::getChallengeStars));
        list.add(new StatAchievement("master_10", "🔱", 10, 400,
                s -> s.getSolvedAtDifficulty(Difficulty.MASTER)));
        return list;
    }

    /**
     * Unlocks newly completed achievements and credits their coin rewards.
     *
     * @return achievements unlocked by this call (empty if none).
     */
    public List<Achievement> evaluate(PlayerStats stats, Wallet wallet) {
        List<Achievement> newlyUnlocked = new ArrayList<>();
        for (Achievement a : achievements) {
            if (!unlocked.contains(a.getId()) && a.isCompleted(stats)) {
                unlocked.add(a.getId());
                wallet.add(a.getRewardCoins());
                newlyUnlocked.add(a);
            }
        }
        return newlyUnlocked;
    }

    public List<Achievement> getAll() {
        return achievements;
    }

    public Achievement find(String id) {
        for (Achievement a : achievements) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public boolean isUnlocked(String id) {
        return unlocked.contains(id);
    }

    public int getUnlockedCount() {
        return unlocked.size();
    }

    public Set<String> getUnlockedIds() {
        return Collections.unmodifiableSet(unlocked);
    }

    public void reset() {
        unlocked.clear();
    }
}
